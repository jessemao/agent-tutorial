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
class WmsFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cancellingReservedShipmentReleasesInventory() throws Exception {
        receive(101L, 10, "receive-101");
        Long shipmentId = createShipment("SO-101", 101L, 6);

        action(shipmentId, "reserve", "reserve-101", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESERVED"));

        action(shipmentId, "cancel", "cancel-101", "客户取消")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        mockMvc.perform(get("/api/wms/inventory/balance")
                        .param("skuId", "101")
                        .param("warehouseId", "1")
                        .param("locationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(10))
                .andExpect(jsonPath("$.data.reservedQuantity").value(0));
    }

    @Test
    void repeatedReceiveWithSamePayloadIsIdempotent() throws Exception {
        receive(102L, 7, "receive-102");
        receive(102L, 7, "receive-102");

        mockMvc.perform(get("/api/wms/inventory/balance")
                        .param("skuId", "102")
                        .param("warehouseId", "1")
                        .param("locationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(7));
    }

    @Test
    void reserveRejectsInsufficientInventoryAndRollsBackOrderState() throws Exception {
        receive(103L, 3, "receive-103");
        Long shipmentId = createShipment("SO-103", 103L, 5);

        action(shipmentId, "reserve", "reserve-103", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_INSUFFICIENT_AVAILABLE"));

        mockMvc.perform(get("/api/wms/shipments/{id}", shipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    void mutatingRequestRequiresOperator() throws Exception {
        mockMvc.perform(post("/api/wms/inventory/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventoryJson(104L, 1, "receive-104")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLATFORM_OPERATOR_REQUIRED"));
    }

    @Test
    void createdShipmentCanBeCancelledWithoutChangingInventory() throws Exception {
        Long shipmentId = createShipment("SO-105", 105L, 6);

        action(shipmentId, "cancel", "cancel-105", "customer changed plan")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancelReason").value("customer changed plan"));

        mockMvc.perform(get("/api/wms/inventory/balance")
                        .param("skuId", "105")
                        .param("warehouseId", "1")
                        .param("locationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(0))
                .andExpect(jsonPath("$.data.reservedQuantity").value(0));
    }

    @Test
    void repeatedCancellationRequiresSameIdempotencyKeyAndPayload() throws Exception {
        receive(106L, 10, "receive-106");
        Long shipmentId = createShipment("SO-106", 106L, 6);
        action(shipmentId, "reserve", "reserve-106", null).andExpect(status().isOk());

        action(shipmentId, "cancel", "cancel-106", "customer cancelled")
                .andExpect(status().isOk());
        action(shipmentId, "cancel", "cancel-106", "customer cancelled")
                .andExpect(status().isOk());
        action(shipmentId, "cancel", "another-cancel-106", "customer cancelled")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_IDEMPOTENCY_CONFLICT"));

        mockMvc.perform(get("/api/wms/inventory/balance")
                        .param("skuId", "106")
                        .param("warehouseId", "1")
                        .param("locationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(10))
                .andExpect(jsonPath("$.data.reservedQuantity").value(0));
    }

    @Test
    void shippedShipmentCannotBeCancelled() throws Exception {
        receive(107L, 10, "receive-107");
        Long shipmentId = createShipment("SO-107", 107L, 6);
        action(shipmentId, "reserve", "reserve-107", null).andExpect(status().isOk());
        action(shipmentId, "ship", "ship-107", null).andExpect(status().isOk());

        action(shipmentId, "cancel", "cancel-107", "too late")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_SHIPMENT_STATE"));

        mockMvc.perform(get("/api/wms/shipments/{id}", shipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));
    }

    @Test
    void cancellationRequiresReasonAtRequestBoundary() throws Exception {
        Long shipmentId = createShipment("SO-108", 108L, 1);

        action(shipmentId, "cancel", "cancel-108", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private void receive(Long skuId, long quantity, String key) throws Exception {
        mockMvc.perform(post("/api/wms/inventory/receive")
                        .header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventoryJson(skuId, quantity, key)))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions balance(Long skuId) throws Exception {
        return mockMvc.perform(get("/api/wms/inventory/balance")
                .param("skuId", skuId.toString()).param("warehouseId", "1").param("locationId", "1"));
    }

    private Long createShipment(String orderNo, Long skuId, long quantity) throws Exception {
        String response = mockMvc.perform(post("/api/wms/shipments")
                        .header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"" + orderNo + "\",\"skuId\":" + skuId
                                + ",\"warehouseId\":1,\"locationId\":1,\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return body.path("data").path("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions action(
            Long shipmentId, String action, String key, String reason) throws Exception {
        String reasonJson = reason == null ? "" : ",\"reason\":\"" + reason + "\"";
        return mockMvc.perform(post("/api/wms/shipments/{id}/{action}", shipmentId, action)
                .header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + key + "\"" + reasonJson + "}"));
    }

    private String inventoryJson(Long skuId, long quantity, String key) {
        return "{\"idempotencyKey\":\"" + key + "\",\"referenceNo\":\"RC-" + skuId
                + "\",\"skuId\":" + skuId + ",\"warehouseId\":1,\"locationId\":1,\"quantity\":"
                + quantity + "}";
    }
}
