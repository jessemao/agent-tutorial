package com.acme.training;

import com.acme.training.platform.audit.AuditRecorder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class T03TransferAtomicityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditRecorder auditRecorder;

    @Test
    void rollsBackMovementsWhenARequiredDownstreamActionFailsAndAllowsRetry() throws Exception {
        receive(9411L, 10, "receive-t03-9411");
        reset(auditRecorder);
        doThrow(new IllegalStateException("injected audit failure"))
                .when(auditRecorder).record("TRANSFER", "INVENTORY", "TR-T03-9411");

        assertThrows(Exception.class,
                () -> transfer(9411L, "transfer-t03-9411", "TR-T03-9411"));
        balance(9411L, 1).andExpect(jsonPath("$.data.availableQuantity").value(10));
        balance(9411L, 2).andExpect(jsonPath("$.data.availableQuantity").value(0));

        reset(auditRecorder);
        transfer(9411L, "transfer-t03-9411", "TR-T03-9411").andExpect(status().isOk());
        mockMvc.perform(get("/api/wms/inventory/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].transferNo").value("TR-T03-9411"));
        verify(auditRecorder, times(1)).record("TRANSFER", "INVENTORY", "TR-T03-9411");
    }

    private void receive(Long skuId, long quantity, String key) throws Exception {
        mockMvc.perform(post("/api/wms/inventory/receive").header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + key + "\",\"referenceNo\":\"RC-" + skuId
                        + "\",\"skuId\":" + skuId + ",\"warehouseId\":1,\"locationId\":1,\"quantity\":"
                        + quantity + "}"))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions transfer(Long skuId, String key, String no)
            throws Exception {
        return mockMvc.perform(post("/api/wms/inventory/transfer").header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + key + "\",\"transferNo\":\"" + no
                        + "\",\"skuId\":" + skuId
                        + ",\"warehouseId\":1,\"sourceLocationId\":1,\"targetLocationId\":2,\"quantity\":4}"));
    }

    private org.springframework.test.web.servlet.ResultActions balance(Long skuId, long locationId) throws Exception {
        return mockMvc.perform(get("/api/wms/inventory/balance")
                .param("skuId", String.valueOf(skuId)).param("warehouseId", "1")
                .param("locationId", String.valueOf(locationId)));
    }

}
