package com.acme.training.wms.stocktake;

import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.error.PlatformException;
import com.acme.training.platform.idempotency.IdempotencyDecision;
import com.acme.training.platform.idempotency.IdempotencyGuard;
import com.acme.training.wms.inventory.InventoryBalanceView;
import com.acme.training.wms.inventory.InventoryCountAdjustmentCommand;
import com.acme.training.wms.inventory.InventoryOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class StocktakeService implements StocktakeOperations {

    private final StocktakeRepository stocktakeRepository;
    private final InventoryOperations inventoryOperations;
    private final IdempotencyGuard idempotencyGuard;
    private final AuditRecorder auditRecorder;

    StocktakeService(StocktakeRepository stocktakeRepository,
                     InventoryOperations inventoryOperations,
                     IdempotencyGuard idempotencyGuard,
                     AuditRecorder auditRecorder) {
        this.stocktakeRepository = stocktakeRepository;
        this.inventoryOperations = inventoryOperations;
        this.idempotencyGuard = idempotencyGuard;
        this.auditRecorder = auditRecorder;
    }

    @Override
    @Transactional
    public StocktakeView create(CreateStocktake command) {
        InventoryBalanceView balance = inventoryOperations.getExistingBalance(
                command.getSkuId(), command.getWarehouseId(), command.getLocationId());
        if (command.getCountedTotalQuantity() < balance.getReservedQuantity()) {
            throw new PlatformException("WMS_COUNT_BELOW_RESERVED",
                    "counted total quantity cannot be lower than reserved quantity");
        }
        long snapshotTotal = balance.getAvailableQuantity() + balance.getReservedQuantity();
        Stocktake stocktake = stocktakeRepository.save(
                new Stocktake(command, snapshotTotal, balance.getReservedQuantity()));
        auditRecorder.record("CREATE", "STOCKTAKE", command.getCountNo());
        return new StocktakeView(stocktake);
    }

    @Override
    @Transactional
    public StocktakeView approve(Long stocktakeId, String idempotencyKey) {
        Stocktake stocktake = locked(stocktakeId);
        if (stocktake.getStatus() == StocktakeStatus.APPROVED) {
            if (idempotencyGuard.decide(true, stocktake.matchesApproval(idempotencyKey),
                    "WMS_IDEMPOTENCY_CONFLICT", "stocktake was already approved by another request")
                    == IdempotencyDecision.SAFE_REPLAY) {
                return new StocktakeView(stocktake);
            }
        }

        inventoryOperations.adjustFromCount(new InventoryCountAdjustmentCommand(
                idempotencyKey, stocktake.getCountNo(), stocktake.getSkuId(), stocktake.getWarehouseId(),
                stocktake.getLocationId(), stocktake.getSnapshotTotalQuantity(),
                stocktake.getSnapshotReservedQuantity(), stocktake.getCountedTotalQuantity()));
        stocktake.markApproved(idempotencyKey);
        return new StocktakeView(stocktake);
    }

    @Override
    @Transactional(readOnly = true)
    public StocktakeView get(Long stocktakeId) {
        return new StocktakeView(stocktakeRepository.findById(stocktakeId)
                .orElseThrow(() -> new PlatformException("WMS_STOCKTAKE_NOT_FOUND",
                        "stocktake does not exist")));
    }

    private Stocktake locked(Long stocktakeId) {
        return stocktakeRepository.findLockedById(stocktakeId)
                .orElseThrow(() -> new PlatformException("WMS_STOCKTAKE_NOT_FOUND",
                        "stocktake does not exist"));
    }
}
