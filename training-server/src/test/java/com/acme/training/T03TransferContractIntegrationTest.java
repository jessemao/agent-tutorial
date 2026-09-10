package com.acme.training;

import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.error.PlatformException;
import com.acme.training.wms.inventory.InventoryOperations;
import com.acme.training.wms.inventory.InventoryTransferCommand;
import com.acme.training.wms.inventory.InventoryTransferOperations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class T03TransferContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryOperations inventoryOperations;

    @Autowired
    private InventoryTransferOperations inventoryTransferOperations;

    @MockBean
    private AuditRecorder auditRecorder;

    @Test
    void rejectsInvalidQuantityAndSameLocation() throws Exception {
        transfer(9401L, 1, 1, 0, "invalid-zero", "TR-T03-9401")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        transfer(9401L, 1, 1, -1, "invalid-negative", "TR-T03-9402")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        transfer(9401L, 1, 1, 1, "invalid-same-location", "TR-T03-9403")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        verify(auditRecorder, never()).record("TRANSFER", "INVENTORY", "TR-T03-9403");
    }

    @Test
    void rejectsInvalidTransfersAtTheModuleBoundary() {
        PlatformException negative = assertThrows(PlatformException.class, () -> inventoryTransferOperations.transfer(
                new InventoryTransferCommand("module-negative", "TR-MODULE-NEGATIVE", 9401L, 1L, 1L, 2L, -1)));
        PlatformException sameLocation = assertThrows(PlatformException.class, () -> inventoryTransferOperations.transfer(
                new InventoryTransferCommand("module-same", "TR-MODULE-SAME", 9401L, 1L, 1L, 1L, 1)));

        assertEquals("INVALID_REQUEST", negative.getCode());
        assertEquals("INVALID_REQUEST", sameLocation.getCode());
    }

    @Test
    void safelyReplaysTheFirstTransferSnapshot() throws Exception {
        receive(9402L, 10, "receive-t03-9402");
        reset(auditRecorder);
        transfer(9402L, 1, 2, 4, "transfer-t03-9402", "TR-T03-9402").andExpect(status().isOk());
        receive(9402L, 2, "receive-more-t03-9402");
        transfer(9402L, 1, 2, 4, "transfer-t03-9402", "TR-T03-9402")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source.availableQuantity").value(6))
                .andExpect(jsonPath("$.data.target.availableQuantity").value(4));
        balance(9402L, 1).andExpect(jsonPath("$.data.availableQuantity").value(8));
        balance(9402L, 2).andExpect(jsonPath("$.data.availableQuantity").value(4));
        verify(auditRecorder, times(1)).record("TRANSFER", "INVENTORY", "TR-T03-9402");
    }

    @Test
    void rejectsAnIdempotencyKeyWithDifferentPayload() throws Exception {
        receive(9403L, 10, "receive-t03-9403");
        transfer(9403L, 1, 2, 4, "transfer-t03-9403", "TR-T03-9403").andExpect(status().isOk());
        reset(auditRecorder);
        transfer(9403L, 1, 2, 3, "transfer-t03-9403", "TR-T03-9403")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_IDEMPOTENCY_CONFLICT"));
        balance(9403L, 1).andExpect(jsonPath("$.data.availableQuantity").value(6));
        balance(9403L, 2).andExpect(jsonPath("$.data.availableQuantity").value(4));
        verify(auditRecorder, never()).record("TRANSFER", "INVENTORY", "TR-T03-9403");
    }

    @Test
    void returnsStableLocationAndInventoryErrors() throws Exception {
        transfer(9404L, 1, 9999, 1, "missing-location", "TR-T03-9404")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_LOCATION_NOT_FOUND"));
        transfer(9404L, 1, 3, 1, "disabled-location", "TR-T03-9405")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_INVALID_LOCATION"));
        transfer(9404L, 1, 4, 1, "cross-warehouse", "TR-T03-9406")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_INVALID_LOCATION"));
        transfer(9404L, 1, 2, 1, "missing-inventory", "TR-T03-9407")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WMS_INVENTORY_NOT_FOUND"));
    }

    @Test
    void rollsBackInsufficientInventoryAndAllowsRetry() throws Exception {
        receive(9405L, 1, 2, "receive-t03-9405");
        reset(auditRecorder);
        transfer(9405L, 1, 2, 4, "transfer-t03-9405", "TR-T03-9405")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_INSUFFICIENT_AVAILABLE"));
        balance(9405L, 1).andExpect(jsonPath("$.data.availableQuantity").value(2));
        balance(9405L, 2).andExpect(jsonPath("$.data.availableQuantity").value(0));
        verify(auditRecorder, never()).record("TRANSFER", "INVENTORY", "TR-T03-9405");
        receive(9405L, 1, 3, "receive-more-t03-9405");
        transfer(9405L, 1, 2, 4, "transfer-t03-9405", "TR-T03-9405")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source.availableQuantity").value(1))
                .andExpect(jsonPath("$.data.target.availableQuantity").value(4));
    }

    @Test
    void rollsBackTargetOverflow() throws Exception {
        receive(9406L, 1, 10, "receive-source-t03-9406");
        receive(9406L, 2, Long.MAX_VALUE, "receive-target-t03-9406");
        reset(auditRecorder);
        transfer(9406L, 1, 2, 1, "transfer-t03-9406", "TR-T03-9406")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WMS_INVENTORY_OVERFLOW"));
        balance(9406L, 1).andExpect(jsonPath("$.data.availableQuantity").value(10));
        balance(9406L, 2).andExpect(jsonPath("$.data.availableQuantity").value(Long.MAX_VALUE));
        verify(auditRecorder, never()).record("TRANSFER", "INVENTORY", "TR-T03-9406");
    }

    private void receive(Long skuId, long quantity, String idempotencyKey) throws Exception {
        receive(skuId, 1, quantity, idempotencyKey);
    }

    private void receive(Long skuId, long locationId, long quantity, String idempotencyKey) throws Exception {
        mockMvc.perform(post("/api/wms/inventory/receive")
                        .header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"" + idempotencyKey
                                + "\",\"referenceNo\":\"RC-" + skuId + "\",\"skuId\":" + skuId
                                + ",\"warehouseId\":1,\"locationId\":" + locationId
                                + ",\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions balance(Long skuId, long locationId) throws Exception {
        return mockMvc.perform(get("/api/wms/inventory/balance")
                .param("skuId", String.valueOf(skuId))
                .param("warehouseId", "1")
                .param("locationId", String.valueOf(locationId)));
    }

    private org.springframework.test.web.servlet.ResultActions transfer(
            Long skuId, long sourceLocationId, long targetLocationId, long quantity,
            String idempotencyKey, String transferNo) throws Exception {
        return mockMvc.perform(post("/api/wms/inventory/transfer")
                .header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + idempotencyKey + "\",\"transferNo\":\"" + transferNo
                        + "\",\"skuId\":" + skuId + ",\"warehouseId\":1,\"sourceLocationId\":"
                        + sourceLocationId + ",\"targetLocationId\":" + targetLocationId
                        + ",\"quantity\":" + quantity + "}"));
    }

}
