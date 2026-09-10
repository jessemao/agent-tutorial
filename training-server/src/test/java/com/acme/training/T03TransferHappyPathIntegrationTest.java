package com.acme.training;

import com.acme.training.platform.audit.AuditRecorder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class T03TransferHappyPathIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditRecorder auditRecorder;

    @Test
    void movesAvailableInventoryToAnEmptyTargetLocation() throws Exception {
        receive(9301L, 10, "receive-t03-9301");
        reset(auditRecorder);

        transfer(9301L, 4, "transfer-t03-9301", "TR-T03-9301")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transferNo").value("TR-T03-9301"))
                .andExpect(jsonPath("$.data.source.locationId").value(1))
                .andExpect(jsonPath("$.data.source.availableQuantity").value(6))
                .andExpect(jsonPath("$.data.source.reservedQuantity").value(0))
                .andExpect(jsonPath("$.data.target.locationId").value(2))
                .andExpect(jsonPath("$.data.target.availableQuantity").value(4))
                .andExpect(jsonPath("$.data.target.reservedQuantity").value(0));

        verify(auditRecorder, times(1)).record("TRANSFER", "INVENTORY", "TR-T03-9301");

        mockMvc.perform(get("/api/wms/inventory/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transferNo").value("TR-T03-9301"))
                .andExpect(jsonPath("$.data[0].sourceLocationId").value(1))
                .andExpect(jsonPath("$.data[0].targetLocationId").value(2))
                .andExpect(jsonPath("$.data[0].quantity").value(4));
    }

    @Test
    void movesAllAvailableInventory() throws Exception {
        receive(9302L, 10, "receive-t03-9302");

        transfer(9302L, 10, "transfer-t03-9302", "TR-T03-9302")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source.availableQuantity").value(0))
                .andExpect(jsonPath("$.data.target.availableQuantity").value(10));
    }

    private void receive(Long skuId, long quantity, String idempotencyKey) throws Exception {
        mockMvc.perform(post("/api/wms/inventory/receive")
                        .header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"" + idempotencyKey
                                + "\",\"referenceNo\":\"RC-" + skuId + "\",\"skuId\":" + skuId
                                + ",\"warehouseId\":1,\"locationId\":1,\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions transfer(
            Long skuId, long quantity, String idempotencyKey, String transferNo) throws Exception {
        return mockMvc.perform(post("/api/wms/inventory/transfer")
                .header("X-Operator", "trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idempotencyKey\":\"" + idempotencyKey + "\",\"transferNo\":\"" + transferNo
                        + "\",\"skuId\":" + skuId + ",\"warehouseId\":1,\"sourceLocationId\":1,"
                        + "\"targetLocationId\":2,\"quantity\":" + quantity + "}"));
    }
}
