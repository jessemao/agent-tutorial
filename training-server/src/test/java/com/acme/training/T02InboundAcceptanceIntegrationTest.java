package com.acme.training;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class T02InboundAcceptanceIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void receivesTenAsFourThenSixAndSafelyReplaysTheFirstBatch() throws Exception {
        long id = create("IN-T02-ACCEPT", 3201);
        receive(id, 4, "t02-batch-1").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(4));
        receive(id, 6, "t02-batch-2").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(10));
        receive(id, 4, "t02-batch-1").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(4));
        inventory(3201).andExpect(jsonPath("$.data.availableQuantity").value(10));
    }

    @Test
    void rejectsOverReceiptAndIdempotencyConflictWithoutChangingTotals() throws Exception {
        long id = create("IN-T02-BOUNDARY", 3202);
        receive(id, 4, "t02-boundary-1").andExpect(status().isOk());
        receive(id, 7, "t02-boundary-2").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_INBOUND_OVER_RECEIPT"));
        receive(id, 5, "t02-boundary-1").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_IDEMPOTENCY_CONFLICT"));
        mockMvc.perform(get("/api/wms/inbounds/{id}", id))
                .andExpect(jsonPath("$.data.receivedQuantity").value(4));
        inventory(3202).andExpect(jsonPath("$.data.availableQuantity").value(4));
    }

    @Test
    void rejectsInvalidQuantitiesAndNewReceiptsAfterCompletion() throws Exception {
        long id = create("IN-T02-VALIDATION", 3204);
        receive(id, 0, "t02-zero").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        receive(id, -1, "t02-negative").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        receive(id, 10, "t02-complete").andExpect(status().isOk());
        receive(id, 1, "t02-after-complete").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_INBOUND_STATE"));
        inventory(3204).andExpect(jsonPath("$.data.availableQuantity").value(10));
    }

    @Test
    void rejectsAnIdempotencyKeyReusedForAnotherInbound() throws Exception {
        long first = create("IN-T02-FIRST", 3205);
        long second = create("IN-T02-SECOND", 3206);
        receive(first, 4, "t02-global-key").andExpect(status().isOk());
        receive(second, 4, "t02-global-key").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_IDEMPOTENCY_CONFLICT"));
        inventory(3205).andExpect(jsonPath("$.data.availableQuantity").value(4));
        inventory(3206).andExpect(jsonPath("$.data.availableQuantity").value(0));
    }

    private long create(String orderNo, long sku) throws Exception {
        String body = mockMvc.perform(post("/api/wms/inbounds").header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON).content("{\"orderNo\":\"" + orderNo
                        + "\",\"skuId\":" + sku + ",\"warehouseId\":1,\"locationId\":1,\"plannedQuantity\":10}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions receive(long id, long quantity, String key) throws Exception {
        return mockMvc.perform(post("/api/wms/inbounds/{id}/receive", id).header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"" + key
                        + "\",\"quantity\":" + quantity + "}"));
    }

    private org.springframework.test.web.servlet.ResultActions inventory(long sku) throws Exception {
        return mockMvc.perform(get("/api/wms/inventory/balance").param("skuId", String.valueOf(sku))
                .param("warehouseId", "1").param("locationId", "1"));
    }
}
