package com.acme.training.wms.count;

import com.acme.training.wms.inventory.CountReconciliationCommand;
import com.acme.training.wms.inventory.CountReconciliationResult;
import com.acme.training.wms.inventory.InventoryBalanceView;
import java.util.List;
import org.springframework.stereotype.Component;

/** Coordinates command completion and the cross-cutting evidence it must leave behind. */
@Component
final class CountCommandSupport {
    private final CountActionPersistence actions;
    private final CountRuntime runtime;

    CountCommandSupport(CountActionPersistence actions, CountRuntime runtime) {
        this.actions = actions;
        this.runtime = runtime;
    }

    InventoryCountView replay(CountAction action, String key, String fingerprint) {
        return actions.replay(action, key, fingerprint);
    }

    void complete(CountAction action, String key, Long countId, String fingerprint,
                  InventoryCountView result, String countNumber) {
        actions.saveReceipt(action, key, countId, fingerprint, result);
        runtime.record(action, countNumber);
    }

    String confirmedFacts(String token, Long countId, long countVersion) {
        return actions.confirmedFacts(token, countId, countVersion);
    }

    void savePreview(CountApprovalPreview preview) {
        actions.savePreview(preview);
    }

    void recordApprovalFailure(Long countId, String failureCode) {
        actions.recordApprovalFailure(countId, failureCode);
    }

    InventoryBalanceView balance(Long skuId, Long warehouseId, Long locationId) {
        return runtime.balance(skuId, warehouseId, locationId);
    }

    long totalQuantity(Long skuId, Long warehouseId, Long locationId) {
        InventoryBalanceView balance = balance(skuId, warehouseId, locationId);
        try {
            return Math.addExact(balance.getAvailableQuantity(), balance.getReservedQuantity());
        } catch (ArithmeticException failure) {
            throw new com.acme.training.platform.error.PlatformException(
                    "WMS_INVENTORY_OVERFLOW", "库存数量超出范围");
        }
    }

    List<CountReconciliationResult> reconcile(CountReconciliationCommand command) {
        return runtime.reconcile(command);
    }

    String requiredOperatorId() {
        return runtime.requiredOperatorId();
    }

    void recordApprovalConflict(String countNumber) {
        runtime.recordApprovalConflict(countNumber);
    }
}
