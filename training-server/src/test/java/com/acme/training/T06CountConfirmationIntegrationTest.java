package com.acme.training;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class T06CountConfirmationIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper json;

    @Test
    void changedBookReturnsLatestDifferenceAndOpaqueTokenThenConfirms() throws Exception {
        String run = UUID.randomUUID().toString();
        JsonNode submitted = submitted(run, 8);
        receive(run + "-change", 2);

        MvcResult conflict = approve(submitted, run + "-first", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_DIFFERENCE_CHANGED"))
                .andExpect(jsonPath("$.data.current.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.approvalLines[0].approvalBookTotal").value(12))
                .andExpect(jsonPath("$.data.approvalLines[0].difference").value(-4))
                .andReturn();
        String token = json.readTree(conflict.getResponse().getContentAsString()).path("data").path("confirmationToken").asText();
        assertFalse(token.isEmpty());
        assertFalse(token.contains("303"));

        approve(submitted, run + "-confirm", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.lines[0].approvalBookTotal").value(12))
                .andExpect(jsonPath("$.data.lines[0].difference").value(-4))
                .andExpect(jsonPath("$.data.lines[0].availableAfter").value(8));
    }

    @Test
    void staleTokenReturnsFreshFactsAndMustBeConfirmedAgain() throws Exception {
        String run = UUID.randomUUID().toString();
        JsonNode submitted = submitted(run, 9);
        receive(run + "-first-change", 1);
        String firstToken = conflictToken(approve(submitted, run + "-preview-one", null)
                .andExpect(status().isConflict()).andReturn());
        receive(run + "-second-change", 1);

        MvcResult stale = approve(submitted, run + "-stale", firstToken)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_COUNT_CONFIRMATION_STALE"))
                .andExpect(jsonPath("$.data.approvalLines[0].approvalBookTotal").value(12))
                .andExpect(jsonPath("$.data.approvalLines[0].difference").value(-3))
                .andReturn();
        String secondToken = conflictToken(stale);
        assertNotEquals(firstToken, secondToken);

        approve(submitted, run + "-confirm-two", secondToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
        mockMvc.perform(get("/api/wms/inventory/balance?skuId=303&warehouseId=1&locationId=1"))
                .andExpect(jsonPath("$.data.availableQuantity").value(9));
    }

    private JsonNode submitted(String run, long countedTotal) throws Exception {
        JsonNode created = response(post("/api/wms/inventory-counts").header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"create-" + run + "\",\"warehouseId\":1,\"lines\":[{\"skuId\":303,\"locationId\":1}]}"));
        JsonNode saved = response(put("/api/wms/inventory-counts/{id}", created.path("id").asLong()).header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"save-" + run + "\",\"expectedVersion\":" + created.path("version").asLong() + ",\"lines\":[{\"skuId\":303,\"locationId\":1,\"countedTotal\":" + countedTotal + "}]}"));
        return response(post("/api/wms/inventory-counts/{id}/transitions", created.path("id").asLong()).header("X-Operator", "counter-a")
                .contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"SUBMIT\",\"idempotencyKey\":\"submit-" + run + "\",\"expectedVersion\":" + saved.path("version").asLong() + "}"));
    }

    private org.springframework.test.web.servlet.ResultActions approve(JsonNode submitted, String key, String token) throws Exception {
        String tokenField = token == null ? "" : ",\"confirmationToken\":\"" + token + "\"";
        return mockMvc.perform(post("/api/wms/inventory-counts/{id}/approval", submitted.path("id").asLong())
                .header("X-Operator", "reviewer-b").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + key + "\",\"expectedVersion\":" + submitted.path("version").asLong() + tokenField + "}"));
    }

    private void receive(String run, long quantity) throws Exception {
        mockMvc.perform(post("/api/wms/inventory/receive").header("X-Operator", "receiver-c")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"receive-" + run + "\",\"referenceNo\":\"RC-" + run + "\",\"skuId\":303,\"warehouseId\":1,\"locationId\":1,\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk());
    }

    private JsonNode response(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private String conflictToken(MvcResult result) throws Exception {
        String token = json.readTree(result.getResponse().getContentAsString()).path("data").path("confirmationToken").asText();
        assertTrue(token.length() >= 32);
        return token;
    }
}
