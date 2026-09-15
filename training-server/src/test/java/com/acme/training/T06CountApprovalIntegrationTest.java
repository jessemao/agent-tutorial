package com.acme.training;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class T06CountApprovalIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper json;

    @Test
    void independentReviewerApprovesAndReconcilesPhysicalTotal() throws Exception {
        JsonNode submitted = submitted("approve-positive", 14);

        approve(submitted, "approve-positive-key", "reviewer-b")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.approvedBy").value("reviewer-b"))
                .andExpect(jsonPath("$.data.lines[0].approvalBookTotal").value(10))
                .andExpect(jsonPath("$.data.lines[0].difference").value(4))
                .andExpect(jsonPath("$.data.lines[0].availableAfter").value(14))
                .andExpect(jsonPath("$.data.lines[0].reservedAfter").value(0));

        balance().andExpect(jsonPath("$.data.availableQuantity").value(14));
    }

    @Test
    void creatorOrSubmitterCannotApprove() throws Exception {
        JsonNode submitted = submitted("self-approval", 10);

        approve(submitted, "self-approval-key", "counter-a")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_SELF_APPROVAL"));

        detail(submitted).andExpect(jsonPath("$.data.status").value("SUBMITTED"));
        balance().andExpect(jsonPath("$.data.availableQuantity").value(10));
    }

    @Test
    void countedTotalBelowReservedRollsBackApprovalAndInventory() throws Exception {
        reserveSix("below-reserved");
        JsonNode submitted = submitted("below-reserved", 5);

        approve(submitted, "below-reserved-key", "reviewer-b")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_BELOW_RESERVED"));

        detail(submitted).andExpect(jsonPath("$.data.status").value("SUBMITTED"));
        balance().andExpect(jsonPath("$.data.availableQuantity").value(4))
                .andExpect(jsonPath("$.data.reservedQuantity").value(6));
    }

    @Test
    void approvalIsIdempotentAndNewKeyCannotRepeatIt() throws Exception {
        JsonNode submitted = submitted("approve-idempotent", 12);

        approve(submitted, "approve-same-key", "reviewer-b").andExpect(status().isOk());
        approve(submitted, "approve-same-key", "reviewer-b")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lines[0].availableAfter").value(12));
        approve(submitted, "approve-other-key", "reviewer-b")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_INVALID_STATE"));
        balance().andExpect(jsonPath("$.data.availableQuantity").value(12));
    }

    @Test
    void approvalRequiresOperatorAndRejectsChangedBookTotal() throws Exception {
        JsonNode submitted = submitted("changed-book", 12);
        mockMvc.perform(post("/api/wms/inventory/receive")
                        .header("X-Operator", "receiver-c")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"changed-book-receive\",\"referenceNo\":\"RC-CHANGED\",\"skuId\":303,\"warehouseId\":1,\"locationId\":1,\"quantity\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wms/inventory-counts/{id}/approval", submitted.path("id").asLong())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"missing-operator\",\"expectedVersion\":" + submitted.path("version").asLong() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLATFORM_OPERATOR_REQUIRED"));
        approve(submitted, "changed-book-approve", "reviewer-b")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_DIFFERENCE_CHANGED"));
        detail(submitted).andExpect(jsonPath("$.data.status").value("SUBMITTED"));
        balance().andExpect(jsonPath("$.data.availableQuantity").value(11));
    }

    @Test
    void zeroCountCreatesAZeroBalanceForAValidMissingDimension() throws Exception {
        JsonNode submitted = submittedForDimension("zero-missing", 304, 2, 0);

        approve(submitted, "zero-missing-approve", "reviewer-b")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.lines[0].difference").value(0))
                .andExpect(jsonPath("$.data.lines[0].availableAfter").value(0));
        mockMvc.perform(get("/api/wms/inventory/balance?skuId=304&warehouseId=1&locationId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(0));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void failureOnSecondLineRollsBackEarlierAdjustment() throws Exception {
        receive(304, 2, 10, "atomic-seed");
        reserve(304, 2, 6, "atomic-reserve");
        JsonNode submitted = submittedTwoLines("atomic-approval", 14, 5);

        approve(submitted, "atomic-approval-key", "reviewer-b")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_BELOW_RESERVED"));

        balance().andExpect(jsonPath("$.data.availableQuantity").value(10));
        mockMvc.perform(get("/api/wms/inventory/balance?skuId=304&warehouseId=1&locationId=2"))
                .andExpect(jsonPath("$.data.availableQuantity").value(4))
                .andExpect(jsonPath("$.data.reservedQuantity").value(6));
        detail(submitted).andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    private JsonNode submitted(String prefix, long countedTotal) throws Exception {
        return submittedForDimension(prefix, 303, 1, countedTotal);
    }

    private JsonNode submittedForDimension(String prefix, long skuId, long locationId, long countedTotal) throws Exception {
        String createdBody = mockMvc.perform(post("/api/wms/inventory-counts")
                        .header("X-Operator", "counter-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"" + prefix + "-create\",\"warehouseId\":1,\"lines\":[{\"skuId\":" + skuId + ",\"locationId\":" + locationId + "}]}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode created = json.readTree(createdBody).path("data");
        long id = created.path("id").asLong();
        String savedBody = mockMvc.perform(put("/api/wms/inventory-counts/{id}", id)
                        .header("X-Operator", "counter-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"" + prefix + "-save\",\"expectedVersion\":" + created.path("version").asLong()
                                + ",\"lines\":[{\"skuId\":" + skuId + ",\"locationId\":" + locationId + ",\"countedTotal\":" + countedTotal + "}]}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode saved = json.readTree(savedBody).path("data");
        String submittedBody = mockMvc.perform(post("/api/wms/inventory-counts/{id}/transitions", id)
                        .header("X-Operator", "counter-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"SUBMIT\",\"idempotencyKey\":\"" + prefix + "-submit\",\"expectedVersion\":" + saved.path("version").asLong() + "}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(submittedBody).path("data");
    }

    private ResultActions approve(JsonNode submitted, String key, String operator) throws Exception {
        return mockMvc.perform(post("/api/wms/inventory-counts/{id}/approval", submitted.path("id").asLong())
                .header("X-Operator", operator)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + key + "\",\"expectedVersion\":" + submitted.path("version").asLong() + "}"));
    }

    private ResultActions detail(JsonNode count) throws Exception {
        return mockMvc.perform(get("/api/wms/inventory-counts/{id}", count.path("id").asLong()));
    }

    private ResultActions balance() throws Exception {
        return mockMvc.perform(get("/api/wms/inventory/balance?skuId=303&warehouseId=1&locationId=1"));
    }

    private JsonNode submittedTwoLines(String prefix, long firstCounted, long secondCounted) throws Exception {
        JsonNode created = response(post("/api/wms/inventory-counts").header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"" + prefix + "-create\",\"warehouseId\":1,\"lines\":[{\"skuId\":303,\"locationId\":1},{\"skuId\":304,\"locationId\":2}]}"));
        JsonNode saved = response(put("/api/wms/inventory-counts/{id}", created.path("id").asLong()).header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"" + prefix + "-save\",\"expectedVersion\":" + created.path("version").asLong() + ",\"lines\":[{\"skuId\":303,\"locationId\":1,\"countedTotal\":" + firstCounted + "},{\"skuId\":304,\"locationId\":2,\"countedTotal\":" + secondCounted + "}]}"));
        return response(post("/api/wms/inventory-counts/{id}/transitions", created.path("id").asLong()).header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"SUBMIT\",\"idempotencyKey\":\"" + prefix + "-submit\",\"expectedVersion\":" + saved.path("version").asLong() + "}"));
    }

    private JsonNode response(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        String body = mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).path("data");
    }

    private void receive(long skuId, long locationId, long quantity, String prefix) throws Exception {
        mockMvc.perform(post("/api/wms/inventory/receive").header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"" + prefix + "\",\"referenceNo\":\"RC-" + prefix + "\",\"skuId\":" + skuId + ",\"warehouseId\":1,\"locationId\":" + locationId + ",\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk());
    }

    private void reserve(long skuId, long locationId, long quantity, String prefix) throws Exception {
        String created = mockMvc.perform(post("/api/wms/shipments").header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"orderNo\":\"SO-" + prefix + "\",\"skuId\":" + skuId + ",\"warehouseId\":1,\"locationId\":" + locationId + ",\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long shipmentId = json.readTree(created).path("data").path("id").asLong();
        mockMvc.perform(post("/api/wms/shipments/{id}/reserve", shipmentId).header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"reserve-" + prefix + "\"}"))
                .andExpect(status().isOk());
    }

    private void reserveSix(String prefix) throws Exception {
        reserve(303, 1, 6, prefix);
    }
}
