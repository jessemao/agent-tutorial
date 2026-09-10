package com.acme.training.wms.inventory;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Compatibility adapter retained at the T04 starting point.
 * T04 evaluates whether this extra public boundary should continue to exist.
 */
@Service
class LegacyInventoryTransferService implements InventoryTransferOperations {

    private final InventoryService inventoryService;

    LegacyInventoryTransferService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public InventoryTransferView transfer(InventoryTransferCommand command) {
        return inventoryService.transfer(command);
    }

    @Override
    public List<InventoryTransferTaskView> listTransferTasks() {
        return inventoryService.listTransferTasks();
    }
}
