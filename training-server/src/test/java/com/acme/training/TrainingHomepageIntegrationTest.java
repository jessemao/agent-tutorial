package com.acme.training;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainingHomepageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rootForwardsToStaticHomepage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void staticHomepageBootstrapsSouthAdminReactApplication() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("south-admin-react")))
                .andExpect(content().string(containsString("id=\"root\"")))
                .andExpect(content().string(containsString("/assets/app.js")))
                .andExpect(content().string(not(containsString("id=\"run-all-button\""))));
    }

    @Test
    void compiledReactApplicationUsesApprovedShipmentAndInventoryApis() throws Exception {
        mockMvc.perform(get("/assets/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/wms/shipments")))
                .andExpect(content().string(containsString("/cancel")))
                .andExpect(content().string(containsString("/api/wms/inventory/balance")));
    }
}
