package com.acme.training.wms.web;

import com.acme.training.platform.web.ApiResponse;
import com.acme.training.wms.inventory.InventoryBalanceView;
import com.acme.training.wms.inventory.InventoryCommand;
import com.acme.training.wms.inventory.InventoryOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/wms/inventory")
public class InventoryController {

    private final InventoryOperations inventoryOperations;

    public InventoryController(InventoryOperations inventoryOperations) {
        this.inventoryOperations = inventoryOperations;
    }

    @PostMapping("/receive")
    public ApiResponse<InventoryBalanceView> receive(@Valid @RequestBody InventoryRequest request) {
        return ApiResponse.success(inventoryOperations.receive(toCommand(request)));
    }

    @GetMapping("/balance")
    public ApiResponse<InventoryBalanceView> balance(@RequestParam Long skuId,
                                                     @RequestParam Long warehouseId,
                                                     @RequestParam Long locationId) {
        return ApiResponse.success(inventoryOperations.getBalance(skuId, warehouseId, locationId));
    }

    private InventoryCommand toCommand(InventoryRequest request) {
        return new InventoryCommand(request.getIdempotencyKey(), request.getReferenceNo(),
                request.getSkuId(), request.getWarehouseId(), request.getLocationId(), request.getQuantity());
    }
}
