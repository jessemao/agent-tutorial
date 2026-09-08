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
class T02InboundBaselineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullPlannedQuantityCanBeReceivedOnce() throws Exception {
        long skuId = 2201L;
        long inboundId = createInbound("IN-T02-FULL", skuId, 10);

        receive(inboundId, 10, "receive-t02-full")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(10));

        inventory(skuId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(10));
    }

    @Test
    void partialQuantityIsReceivedAndImmediatelyAvailable() throws Exception {
        long skuId = 2202L;
        long inboundId = createInbound("IN-T02-PARTIAL", skuId, 10);

        receive(inboundId, 4, "receive-t02-partial")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(4));

        mockMvc.perform(get("/api/wms/inbounds/{id}", inboundId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(4));

        inventory(skuId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(4))
                .andExpect(jsonPath("$.data.reservedQuantity").value(0));
    }

    private long createInbound(String orderNo, long skuId, long plannedQuantity) throws Exception {
        String response = mockMvc.perform(post("/api/wms/inbounds")
                        .header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"" + orderNo + "\",\"skuId\":" + skuId
                                + ",\"warehouseId\":1,\"locationId\":1,\"plannedQuantity\":"
                                + plannedQuantity + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.path("data").path("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions receive(long inboundId, long quantity, String key)
            throws Exception {
        return mockMvc.perform(post("/api/wms/inbounds/{id}/receive", inboundId)
                .header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + key + "\",\"quantity\":" + quantity + "}"));
    }

    private org.springframework.test.web.servlet.ResultActions inventory(long skuId) throws Exception {
        return mockMvc.perform(get("/api/wms/inventory/balance")
                .param("skuId", String.valueOf(skuId))
                .param("warehouseId", "1")
                .param("locationId", "1"));
    }
}
