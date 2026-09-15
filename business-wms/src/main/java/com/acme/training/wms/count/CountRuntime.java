package com.acme.training.wms.count;

import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.operator.CurrentOperator;
import com.acme.training.wms.inventory.InventoryOperations;
import com.acme.training.wms.inventory.InventoryBalanceView;
import com.acme.training.wms.inventory.CountReconciliationCommand;
import com.acme.training.wms.inventory.CountReconciliationResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
final class CountRuntime {
    private final InventoryOperations inventory;
    private final CurrentOperator operator;
    private final AuditRecorder audit;

    CountRuntime(InventoryOperations inventory, CurrentOperator operator, AuditRecorder audit) {
        this.inventory = inventory;
        this.operator = operator;
        this.audit = audit;
    }

    InventoryBalanceView balance(Long skuId, Long warehouseId, Long locationId) {
        return inventory.getBalance(skuId, warehouseId, locationId);
    }

    List<CountReconciliationResult> reconcile(CountReconciliationCommand command) {
        return inventory.reconcileCount(command);
    }

    String requiredOperatorId() {
        return operator.requiredOperatorId();
    }

    void record(CountAction action, String countNumber) {
        audit.record(action.name(), "INVENTORY_COUNT", countNumber);
    }

    void recordApprovalConflict(String countNumber) {
        audit.record("APPROVAL_CONFLICT", "INVENTORY_COUNT", countNumber);
    }
}
