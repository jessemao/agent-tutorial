package com.acme.training;

import com.acme.training.platform.audit.AuditRecorder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class T02InboundRollbackIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuditRecorder auditRecorder;

    @Test
    void auditFailureRollsBackTheBatchAndInventory() throws Exception {
        long id = create("IN-T02-ROLLBACK", 3203);
        doThrow(new IllegalStateException("audit unavailable"))
                .when(auditRecorder).record(eq("RECEIVE"), eq("INBOUND"), eq("IN-T02-ROLLBACK"));
        assertThrows(NestedServletException.class, () ->
                mockMvc.perform(post("/api/wms/inbounds/{id}/receive", id).header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"t02-rollback\",\"quantity\":4}")));
        mockMvc.perform(get("/api/wms/inbounds/{id}", id))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(0));
        mockMvc.perform(get("/api/wms/inventory/balance").param("skuId", "3203")
                        .param("warehouseId", "1").param("locationId", "1"))
                .andExpect(jsonPath("$.data.availableQuantity").value(0));
    }

    @Test
    void concurrentReplayReturnsTheOriginalSnapshot() throws Exception {
        long id = create("IN-T02-CONCURRENT-REPLAY", 3207);
        CountDownLatch firstAtAudit = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        doAnswer(invocation -> {
            firstAtAudit.countDown();
            assertTrue(releaseFirst.await(10, TimeUnit.SECONDS));
            return null;
        }).when(auditRecorder).record(eq("RECEIVE"), eq("INBOUND"), eq("IN-T02-CONCURRENT-REPLAY"));

        ExecutorService workers = Executors.newFixedThreadPool(2);
        Future<org.springframework.test.web.servlet.MvcResult> first = workers.submit(() -> receive(id, "t02-concurrent-replay"));
        try {
            assertTrue(firstAtAudit.await(10, TimeUnit.SECONDS));
            Future<org.springframework.test.web.servlet.MvcResult> replay = workers.submit(() -> receive(id, "t02-concurrent-replay"));
            assertThrows(TimeoutException.class, () -> replay.get(1, TimeUnit.SECONDS));
            releaseFirst.countDown();
            assertEquals(200, first.get(10, TimeUnit.SECONDS).getResponse().getStatus());
            org.springframework.test.web.servlet.MvcResult replayResult = replay.get(10, TimeUnit.SECONDS);
            assertEquals(200, replayResult.getResponse().getStatus());
            assertEquals(4, objectMapper.readTree(replayResult.getResponse().getContentAsString())
                    .path("data").path("receivedQuantity").asLong());
        } finally {
            releaseFirst.countDown();
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void safeReplayDoesNotRepeatInboundAudit() throws Exception {
        long id = create("IN-T02-AUDIT-REPLAY", 3208);
        receive(id, "t02-audit-first");
        mockMvc.perform(post("/api/wms/inbounds/{id}/receive", id).header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"t02-audit-final\",\"quantity\":6}"))
                .andExpect(status().isOk());
        receive(id, "t02-audit-first");

        verify(auditRecorder, times(2)).record(eq("RECEIVE"), eq("INBOUND"), eq("IN-T02-AUDIT-REPLAY"));
    }

    private org.springframework.test.web.servlet.MvcResult receive(long id, String key) throws Exception {
        return mockMvc.perform(post("/api/wms/inbounds/{id}/receive", id).header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"" + key + "\",\"quantity\":4}"))
                .andReturn();
    }

    private long create(String orderNo, long skuId) throws Exception {
        String body = mockMvc.perform(post("/api/wms/inbounds").header("X-Operator", "trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"" + orderNo + "\",\"skuId\":" + skuId
                                + ",\"warehouseId\":1,\"locationId\":1,\"plannedQuantity\":10}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }
}
