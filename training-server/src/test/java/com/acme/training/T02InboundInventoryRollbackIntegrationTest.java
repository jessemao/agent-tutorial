package com.acme.training;

import com.acme.training.wms.inventory.InventoryOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class T02InboundInventoryRollbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private InventoryOperations inventoryOperations;

    @Test
    void inventoryFailureRollsBackOrderReceiptBalanceAndMovement() throws Exception {
        long id = create();
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw new IllegalStateException("inventory unavailable after write");
        }).when(inventoryOperations).receive(any());

        assertThrows(NestedServletException.class, () -> receive(id));
        mockMvc.perform(get("/api/wms/inbounds/{id}", id))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(0));
        mockMvc.perform(get("/api/wms/inventory/balance").param("skuId", "3209")
                        .param("warehouseId", "1").param("locationId", "1"))
                .andExpect(jsonPath("$.data.availableQuantity").value(0));

        doCallRealMethod().when(inventoryOperations).receive(any());
        receive(id);
        mockMvc.perform(get("/api/wms/inbounds/{id}", id))
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(4));
        mockMvc.perform(get("/api/wms/inventory/balance").param("skuId", "3209")
                        .param("warehouseId", "1").param("locationId", "1"))
                .andExpect(jsonPath("$.data.availableQuantity").value(4));
    }

    private long create() throws Exception {
        String body = mockMvc.perform(post("/api/wms/inbounds").header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"IN-T02-INVENTORY-ROLLBACK\",\"skuId\":3209,"
                                + "\"warehouseId\":1,\"locationId\":1,\"plannedQuantity\":10}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    private void receive(long id) throws Exception {
        mockMvc.perform(post("/api/wms/inbounds/{id}/receive", id).header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"t02-inventory-rollback\",\"quantity\":4}"))
                .andExpect(status().isOk());
    }
}
