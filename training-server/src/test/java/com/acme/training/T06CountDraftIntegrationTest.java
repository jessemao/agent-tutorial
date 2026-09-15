package com.acme.training;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class T06CountDraftIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void userCreatesAndQueriesAMultiLineCountDraft() throws Exception {
        String response = create("create-count-1", "["
                + "{\"skuId\":303,\"locationId\":1},"
                + "{\"skuId\":304,\"locationId\":2}]")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.countNo", startsWith("IC-")))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.warehouse.code").value("WH-SH"))
                .andExpect(jsonPath("$.data.lines", hasSize(2)))
                .andExpect(jsonPath("$.data.lines[0].sku.code").value("SKU-303"))
                .andExpect(jsonPath("$.data.lines[0].location.code").value("A01-01-01"))
                .andExpect(jsonPath("$.data.lines[0].creationBookTotal").value(10))
                .andReturn().getResponse().getContentAsString();

        long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).path("data").path("id").asLong();

        mockMvc.perform(get("/api/wms/inventory-counts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.lines[1].sku.name").value("培训商品 304"))
                .andExpect(jsonPath("$.data.lines[1].creationBookTotal").value(0));

        mockMvc.perform(get("/api/wms/inventory-counts").param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id").value(id));
    }

    @Test
    void userSelectsMasterDataByBusinessValues() throws Exception {
        mockMvc.perform(get("/api/wms/master-data/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("WH-SH"));
        mockMvc.perform(get("/api/wms/master-data/skus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("SKU-303"));
        mockMvc.perform(get("/api/wms/master-data/locations").param("warehouseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].code").value("A01-01-01"));
    }

    @Test
    void duplicateOrInvalidDimensionsRejectTheWholeDraft() throws Exception {
        create("duplicate-lines", "["
                + "{\"skuId\":303,\"locationId\":1},"
                + "{\"skuId\":303,\"locationId\":1}]")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        create("disabled-location", "[{\"skuId\":303,\"locationId\":3}]")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_INVALID_LOCATION"));

        mockMvc.perform(get("/api/wms/inventory-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)));
    }

    @Test
    void activeInventoryDimensionBelongsToOnlyOneDraft() throws Exception {
        create("first-active", "[{\"skuId\":303,\"locationId\":1}]")
                .andExpect(status().isOk());

        create("second-active", "[{\"skuId\":303,\"locationId\":1}]")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_SCOPE_CONFLICT"));

        mockMvc.perform(get("/api/wms/inventory-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    void createRequestIsIdempotentAndConflictingReuseIsRejected() throws Exception {
        String first=create("same-create", "[{\"skuId\":303,\"locationId\":1}]").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(first).path("data").path("id").asLong();
        mockMvc.perform(put("/api/wms/inventory-counts/{id}",id).header("X-Operator","counter-a").contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"move-create-forward\",\"expectedVersion\":1,\"lines\":[{\"skuId\":303,\"locationId\":1,\"countedTotal\":1}]}" )).andExpect(status().isOk());
        create("same-create", "[{\"skuId\":303,\"locationId\":1}]")
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("DRAFT")).andExpect(jsonPath("$.data.version").value(1));
        create("same-create", "[{\"skuId\":304,\"locationId\":2}]")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_IDEMPOTENCY_CONFLICT"));

        mockMvc.perform(get("/api/wms/inventory-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    void damagedIdempotencySnapshotReturnsAStableFailure() throws Exception {
        create("damaged-receipt", "[{\"skuId\":303,\"locationId\":1}]")
                .andExpect(status().isOk());
        jdbc.update(
                "update wms_count_action_receipt set result_snapshot=? "
                        + "where action_type=? and idempotency_key=?",
                "not-base64", "CREATE", "damaged-receipt");

        create("damaged-receipt", "[{\"skuId\":303,\"locationId\":1}]")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_RECEIPT_INVALID"));
    }

    private org.springframework.test.web.servlet.ResultActions create(String key, String lines) throws Exception {
        return mockMvc.perform(post("/api/wms/inventory-counts")
                .header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + key + "\",\"warehouseId\":1,\"lines\":" + lines + "}"));
    }
}
