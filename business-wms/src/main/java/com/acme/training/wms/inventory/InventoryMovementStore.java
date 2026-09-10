package com.acme.training.wms.inventory;

import com.acme.training.platform.error.PlatformException;
import com.acme.training.wms.PersistenceConstraints;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
class InventoryMovementStore {

    private final InventoryMovementRepository repository;

    InventoryMovementStore(InventoryMovementRepository repository) {
        this.repository = repository;
    }

    void save(InventoryMovement movement) {
        try {
            repository.saveAndFlush(movement);
        } catch (DataIntegrityViolationException exception) {
            if (PersistenceConstraints.hasName(exception, "uk_movement_idempotency")) {
                PlatformException conflict = new PlatformException("WMS_IDEMPOTENCY_CONFLICT",
                        "idempotency key was concurrently used with conflicting inventory data");
                conflict.initCause(exception);
                throw conflict;
            }
            throw exception;
        }
    }

}
