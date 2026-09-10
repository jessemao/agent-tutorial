package com.acme.training.wms.inventory;

import java.util.List;

/**
 * Legacy public seam used by the T04 starting point.
 * It intentionally keeps transfer operations separate from other inventory changes.
 */
public interface InventoryTransferOperations {

    InventoryTransferView transfer(InventoryTransferCommand command);

    List<InventoryTransferTaskView> listTransferTasks();
}
