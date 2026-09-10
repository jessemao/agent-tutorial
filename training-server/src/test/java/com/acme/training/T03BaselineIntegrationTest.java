package com.acme.training;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class T03BaselineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void initialSourceInventoryStillWorks() throws Exception {
        mockMvc.perform(get("/api/wms/inventory/balance")
                        .param("skuId", "303").param("warehouseId", "1").param("locationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(10));
    }
}
