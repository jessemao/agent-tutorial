package com.acme.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void receiveOverflowIsRejectedWithoutChangingInventory() throws Exception {
        receive(801L, 1L, Long.MAX_VALUE, "overflow-initial").andExpect(status().isOk());
        receive(801L, 1L, 1, "overflow-extra")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("WMS_INVENTORY_OVERFLOW"));
        balance(801L, 1L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(Long.MAX_VALUE))
                .andExpect(jsonPath("$.data.reservedQuantity").value(0));
    }

    @Test
    void receiveCannotOverflowTotalInventoryWhenSomeQuantityIsReserved() throws Exception {
        receive(805L, 1L, Long.MAX_VALUE, "total-initial").andExpect(status().isOk());
        String response = mockMvc.perform(post("/api/wms/shipments")
                .header("X-Operator", "trainer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderNo\":\"SO-805\",\"skuId\":805,\"warehouseId\":1,\"locationId\":1,\"quantity\":1}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long shipmentId = objectMapper.readTree(response).path("data").path("id").asLong();
        mockMvc.perform(post("/api/wms/shipments/{id}/reserve", shipmentId)
                .header("X-Operator", "trainer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"total-reserve\"}"))
                .andExpect(status().isOk());
        receive(805L, 1L, 1, "total-extra")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_INVENTORY_OVERFLOW"));
        balance(805L, 1L)
                .andExpect(jsonPath("$.data.availableQuantity").value(Long.MAX_VALUE - 1))
                .andExpect(jsonPath("$.data.reservedQuantity").value(1));
    }

    private ResultActions receive(long sku, long location, long quantity, String key) throws Exception {
        return mockMvc.perform(post("/api/wms/inventory/receive")
                .header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + key + "\",\"referenceNo\":\"RC-" + sku
                        + "\",\"skuId\":" + sku + ",\"warehouseId\":1,\"locationId\":" + location
                        + ",\"quantity\":" + quantity + "}"));
    }

    private ResultActions balance(long sku, long location) throws Exception {
        return mockMvc.perform(get("/api/wms/inventory/balance")
                .param("skuId", Long.toString(sku)).param("warehouseId", "1")
                .param("locationId", Long.toString(location)));
    }
}
