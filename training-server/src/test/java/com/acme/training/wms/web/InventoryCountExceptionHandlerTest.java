package com.acme.training.wms.web;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryCountExceptionHandlerTest {
    @Test
    void unknownDatabaseConstraintIsNotDisguisedAsScopeConflict() {
        InventoryCountExceptionHandler handler = new InventoryCountExceptionHandler();
        DataIntegrityViolationException failure = new DataIntegrityViolationException(
                "write failed", new RuntimeException("uk_count_no"));

        assertThrows(DataIntegrityViolationException.class, () -> handler.conflict(failure));
    }
}
