package com.acme.training.wms.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Optional<InventoryMovement> findByOperationTypeAndIdempotencyKey(
            String operationType, String idempotencyKey);

    long countByReferenceNo(String referenceNo);
}
