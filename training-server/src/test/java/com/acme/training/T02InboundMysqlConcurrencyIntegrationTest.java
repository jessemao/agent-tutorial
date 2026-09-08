package com.acme.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "mysql-verification")
class T02InboundMysqlConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void concurrentFinalReceiptsNeverExceedThePlan() throws Exception {
        long id = create();
        receive(id, "t02-mysql-first", 4);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<MvcResult> first = workers.submit(() -> receiveAfter(start, id, "t02-mysql-final-a", 6));
            Future<MvcResult> second = workers.submit(() -> receiveAfter(start, id, "t02-mysql-final-b", 6));
            start.countDown();
            List<Integer> statuses = Arrays.asList(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS))
                    .stream().map(result -> result.getResponse().getStatus()).sorted().collect(Collectors.toList());
            assertEquals(Arrays.asList(200, 409), statuses);
        } finally {
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
        }
        mockMvc.perform(get("/api/wms/inbounds/{id}", id))
                .andExpect(jsonPath("$.data.receivedQuantity").value(10))
                .andExpect(jsonPath("$.data.status").value("RECEIVED"));
        mockMvc.perform(get("/api/wms/inventory/balance").param("skuId", "3210")
                        .param("warehouseId", "1").param("locationId", "1"))
                .andExpect(jsonPath("$.data.availableQuantity").value(10));
    }

    private MvcResult receiveAfter(CountDownLatch start, long id, String key, long quantity) throws Exception {
        assertTrue(start.await(10, TimeUnit.SECONDS));
        return receive(id, key, quantity);
    }

    private MvcResult receive(long id, String key, long quantity) throws Exception {
        return mockMvc.perform(post("/api/wms/inbounds/{id}/receive", id).header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"" + key + "\",\"quantity\":" + quantity + "}"))
                .andReturn();
    }

    private long create() throws Exception {
        String body = mockMvc.perform(post("/api/wms/inbounds").header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"IN-T02-MYSQL-CONCURRENCY\",\"skuId\":3210,"
                                + "\"warehouseId\":1,\"locationId\":1,\"plannedQuantity\":10}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }
}
