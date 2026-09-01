package com.acme.training.wms.inventory;

/**
 * The only public seam for changing inventory.
 * Each command is atomic and idempotent by operation type plus idempotency key.
 */
public interface InventoryOperations {

    InventoryBalanceView receive(InventoryCommand command);

    InventoryBalanceView reserve(InventoryCommand command);

    InventoryBalanceView release(InventoryCommand command);

    InventoryBalanceView ship(InventoryCommand command);

    InventoryTransferView transfer(InventoryTransferCommand command);

    InventoryBalanceView adjustFromCount(InventoryCountAdjustmentCommand command);

    /** Reads a balance without requesting an exclusive lock; missing dimensions return zero quantities. */
    InventoryBalanceView getBalance(Long skuId, Long warehouseId, Long locationId);

    /** Reads without an exclusive lock; missing dimensions raise WMS_INVENTORY_NOT_FOUND. */
    InventoryBalanceView getExistingBalance(Long skuId, Long warehouseId, Long locationId);
}
