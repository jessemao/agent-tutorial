package com.acme.training;

import com.acme.training.platform.error.PlatformException;
import com.acme.training.wms.inventory.InventoryBalanceView;
import com.acme.training.wms.inventory.InventoryCommand;
import com.acme.training.wms.inventory.InventoryCountAdjustmentCommand;
import com.acme.training.wms.inventory.InventoryOperations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryConcurrencyTest {

    @Autowired
    private InventoryOperations inventory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ordinaryReadersDoNotBlockEachOtherUntilTransactionCommit() throws Exception {
        asOperator(() -> inventory.receive(new InventoryCommand("reader-initial", "RC-901", 901L, 1L, 1L, 10)));
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch firstRead = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Future<?> holder = workers.submit(() -> new TransactionTemplate(transactionManager).execute(status -> {
            assertEquals(10, inventory.getBalance(901L, 1L, 1L).getAvailableQuantity());
            firstRead.countDown();
            await(release);
            return null;
        }));
        try {
            assertTrue(firstRead.await(10, TimeUnit.SECONDS), "first reader did not start");
            Future<InventoryBalanceView> reader = workers.submit(() -> inventory.getExistingBalance(901L, 1L, 1L));
            assertEquals(10, reader.get(1, TimeUnit.SECONDS).getAvailableQuantity());
        } finally {
            release.countDown();
            try {
                holder.get(10, TimeUnit.SECONDS);
            } finally {
                workers.shutdownNow();
                assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS), "workers did not stop");
            }
        }
    }

    @Test
    void concurrentReceiveReplaysAfterTheFirstTransactionCommits() throws Exception {
        InventoryCommand command = new InventoryCommand("concurrent-receive", "RC-902", 902L, 1L, 1L, 7);
        InventoryBalanceView replay = overlappingTransactions(() -> inventory.receive(command));
        assertEquals(7, replay.getAvailableQuantity());
        assertEquals(7, inventory.getBalance(902L, 1L, 1L).getAvailableQuantity());
    }

    @Test
    void concurrentCountAdjustmentReplaysAfterTheFirstTransactionCommits() throws Exception {
        asOperator(() -> inventory.receive(new InventoryCommand("count-initial", "RC-904", 904L, 1L, 1L, 10)));
        InventoryCountAdjustmentCommand command = new InventoryCountAdjustmentCommand(
                "concurrent-count", "CT-904", 904L, 1L, 1L, 10, 0, 8);
        InventoryBalanceView replay = overlappingTransactions(() -> inventory.adjustFromCount(command));
        assertEquals(8, replay.getAvailableQuantity());
        assertEquals(8, inventory.getBalance(904L, 1L, 1L).getAvailableQuantity());
    }

    @Test
    void countReplayRejectsChangedAbsoluteQuantitiesEvenWhenTheDeltaMatches() throws Exception {
        asOperator(() -> inventory.receive(new InventoryCommand("count-conflict-initial", "RC-905", 905L, 1L, 1L, 10)));
        asOperator(() -> inventory.adjustFromCount(new InventoryCountAdjustmentCommand(
                "count-conflict", "CT-905", 905L, 1L, 1L, 10, 0, 8)));
        PlatformException differentTotals = assertThrows(PlatformException.class, () ->
                asOperator(() -> inventory.adjustFromCount(new InventoryCountAdjustmentCommand(
                        "count-conflict", "CT-905", 905L, 1L, 1L, 11, 0, 9))));
        assertEquals("WMS_IDEMPOTENCY_CONFLICT", differentTotals.getCode());
        PlatformException differentReserved = assertThrows(PlatformException.class, () ->
                asOperator(() -> inventory.adjustFromCount(new InventoryCountAdjustmentCommand(
                        "count-conflict", "CT-905", 905L, 1L, 1L, 10, 1, 8))));
        assertEquals("WMS_IDEMPOTENCY_CONFLICT", differentReserved.getCode());
        assertEquals(8, inventory.getBalance(905L, 1L, 1L).getAvailableQuantity());
    }

    private <T> T overlappingTransactions(Supplier<T> action) throws Exception {
        return overlappingTransactions(action, action);
    }

    @Test
    void concurrentSameKeyAtDifferentLocationsReturnsConflictAndRollsBackLoser() throws Exception {
        InventoryCommand first = new InventoryCommand("cross-location-key", "RC-906", 906L, 1L, 1L, 7);
        InventoryCommand conflicting = new InventoryCommand("cross-location-key", "RC-906", 906L, 1L, 2L, 9);
        ExecutionException failure = assertThrows(ExecutionException.class, () ->
                overlappingTransactions(() -> inventory.receive(first), () -> inventory.receive(conflicting)));
        assertTrue(failure.getCause() instanceof PlatformException, "conflict must use the business error contract");
        assertEquals("WMS_IDEMPOTENCY_CONFLICT", ((PlatformException) failure.getCause()).getCode());
        assertEquals(7, inventory.getBalance(906L, 1L, 1L).getAvailableQuantity());
        assertEquals(0, inventory.getBalance(906L, 1L, 2L).getAvailableQuantity());
    }

    private <T> T overlappingTransactions(Supplier<T> firstAction, Supplier<T> secondAction) throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch firstApplied = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Future<T> first = workers.submit(() -> asOperator(() ->
                new TransactionTemplate(transactionManager).execute(status -> {
                    T result = firstAction.get();
                    firstApplied.countDown();
                    await(release);
                    return result;
                })));
        try {
            assertTrue(firstApplied.await(10, TimeUnit.SECONDS), "first command did not complete");
            Future<T> second = workers.submit(() -> asOperator(() -> {
                secondStarted.countDown();
                return secondAction.get();
            }));
            assertTrue(secondStarted.await(10, TimeUnit.SECONDS), "second command did not start");
            // Hold the first commit while the duplicate enters the public command boundary.
            assertThrows(TimeoutException.class, () -> second.get(1, TimeUnit.SECONDS));
            release.countDown();
            first.get(10, TimeUnit.SECONDS);
            return second.get(10, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS), "workers did not stop");
        }
    }

    @Test
    void concurrentCrossLocationConflictUsesTheExistingHttpErrorContract() throws Exception {
        InventoryCommand first = new InventoryCommand("cross-http-key", "RC-907", 907L, 1L, 1L, 7);
        overlappingTransactions(() -> inventory.receive(first), () -> {
            try {
                mockMvc.perform(post("/api/wms/inventory/receive")
                        .header("X-Operator", "trainer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idempotencyKey\":\"cross-http-key\",\"referenceNo\":\"RC-907\","
                                + "\"skuId\":907,\"warehouseId\":1,\"locationId\":2,\"quantity\":9}"))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.code").value("WMS_IDEMPOTENCY_CONFLICT"));
                return null;
            } catch (Exception exception) {
                throw new IllegalStateException("HTTP regression failed", exception);
            }
        });
        assertEquals(7, inventory.getBalance(907L, 1L, 1L).getAvailableQuantity());
        assertEquals(0, inventory.getBalance(907L, 1L, 2L).getAvailableQuantity());
    }

    @Test
    void unrelatedDatabaseIntegrityErrorsAreNotDisguisedAsIdempotencyConflicts() {
        String tooLongKey = new String(new char[81]).replace('\0', 'x');
        assertThrows(DataIntegrityViolationException.class, () -> asOperator(() ->
                inventory.receive(new InventoryCommand(tooLongKey, "RC-908", 908L, 1L, 1L, 7))));
        assertEquals(0, inventory.getBalance(908L, 1L, 1L).getAvailableQuantity());
    }

    private <T> T asOperator(Callable<T> action) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Operator", "trainer");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        try {
            return action.call();
        } finally {
            attributes.requestCompleted();
            RequestContextHolder.resetRequestAttributes();
        }
    }

    private void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "transaction release timed out");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("test worker interrupted", exception);
        }
    }
}
