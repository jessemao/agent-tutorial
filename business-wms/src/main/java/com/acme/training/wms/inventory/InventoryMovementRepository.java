package com.acme.training.wms.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Optional<InventoryMovement> findByOperationTypeAndIdempotencyKey(
            String operationType, String idempotencyKey);

    long countByReferenceNo(String referenceNo);

    @Query("select new com.acme.training.wms.inventory.InventoryTransferTaskView("
            + "o.referenceNo, o.skuId, o.warehouseId, o.locationId, i.locationId, o.quantity, o.createdAt) "
            + "from InventoryMovement o, InventoryMovement i "
            + "where o.operationType = 'TRANSFER_OUT' and i.operationType = 'TRANSFER_IN' "
            + "and o.idempotencyKey = i.idempotencyKey order by o.createdAt desc")
    List<InventoryTransferTaskView> findTransferTasks();
}
