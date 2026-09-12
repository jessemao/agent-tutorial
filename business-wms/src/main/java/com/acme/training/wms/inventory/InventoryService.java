package com.acme.training.wms.inventory;

import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.error.PlatformException;
import com.acme.training.platform.idempotency.IdempotencyDecision;
import com.acme.training.platform.idempotency.IdempotencyGuard;
import com.acme.training.wms.masterdata.StorageLocation;
import com.acme.training.wms.masterdata.StorageLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Consumer;

@Service
class InventoryService implements InventoryOperations {

    private final InventoryBalanceRepository balanceRepository;
    private final InventoryMovementRepository movementRepository;
    private final InventoryMovementStore movementStore;
    private final StorageLocationRepository locationRepository;
    private final AuditRecorder auditRecorder;
    private final IdempotencyGuard idempotencyGuard;

    InventoryService(InventoryBalanceRepository balanceRepository,
                     InventoryMovementRepository movementRepository,
                     InventoryMovementStore movementStore,
                     StorageLocationRepository locationRepository,
                     AuditRecorder auditRecorder,
                     IdempotencyGuard idempotencyGuard) {
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.movementStore = movementStore;
        this.locationRepository = locationRepository;
        this.auditRecorder = auditRecorder;
        this.idempotencyGuard = idempotencyGuard;
    }

    @Override
    @Transactional
    public InventoryBalanceView receive(InventoryCommand command) {
        return change("RECEIVE", command, new Consumer<InventoryBalance>() {
            @Override
            public void accept(InventoryBalance balance) {
                balance.receive(command.getQuantity());
            }
        }, true);
    }

    @Override
    @Transactional
    public InventoryBalanceView reserve(InventoryCommand command) {
        return change("RESERVE", command, new Consumer<InventoryBalance>() {
            @Override
            public void accept(InventoryBalance balance) {
                balance.reserve(command.getQuantity());
            }
        }, false);
    }

    @Override
    @Transactional
    public InventoryBalanceView release(InventoryCommand command) {
        return change("RELEASE", command, new Consumer<InventoryBalance>() {
            @Override
            public void accept(InventoryBalance balance) {
                balance.release(command.getQuantity());
            }
        }, false);
    }

    @Override
    @Transactional
    public InventoryBalanceView ship(InventoryCommand command) {
        return change("SHIP", command, new Consumer<InventoryBalance>() {
            @Override
            public void accept(InventoryBalance balance) {
                balance.ship(command.getQuantity());
            }
        }, false);
    }

    @Override
    @Transactional
    public InventoryTransferView transfer(InventoryTransferCommand command) {
        requireValidTransfer(command);
        Long firstLocationId = Math.min(command.getSourceLocationId(), command.getTargetLocationId());
        Long secondLocationId = Math.max(command.getSourceLocationId(), command.getTargetLocationId());
        StorageLocation firstLocation = lockLocation(firstLocationId);
        StorageLocation secondLocation = lockLocation(secondLocationId);
        requireTransferLocation(firstLocation, command.getWarehouseId());
        requireTransferLocation(secondLocation, command.getWarehouseId());

        Optional<InventoryMovement> previousOut = movementRepository
                .findByOperationTypeAndIdempotencyKey("TRANSFER_OUT", command.getIdempotencyKey());
        Optional<InventoryMovement> previousIn = movementRepository
                .findByOperationTypeAndIdempotencyKey("TRANSFER_IN", command.getIdempotencyKey());
        boolean previousExists = previousOut.isPresent() || previousIn.isPresent();
        boolean sameRequest = previousOut.isPresent() && previousIn.isPresent()
                && previousOut.get().matches(command, command.getSourceLocationId())
                && previousIn.get().matches(command, command.getTargetLocationId());
        if (idempotencyGuard.decide(previousExists, sameRequest,
                "WMS_IDEMPOTENCY_CONFLICT",
                "idempotency key was already used with different transfer data")
                == IdempotencyDecision.SAFE_REPLAY) {
            return new InventoryTransferView(command.getTransferNo(),
                    previousOut.get().result(), previousIn.get().result());
        }

        Optional<InventoryBalance> firstBalance = balanceRepository.findLockedBySkuIdAndWarehouseIdAndLocationId(
                command.getSkuId(), command.getWarehouseId(), firstLocationId);
        Optional<InventoryBalance> secondBalance = balanceRepository.findLockedBySkuIdAndWarehouseIdAndLocationId(
                command.getSkuId(), command.getWarehouseId(), secondLocationId);
        Optional<InventoryBalance> sourceBalance = command.getSourceLocationId().equals(firstLocationId)
                ? firstBalance : secondBalance;
        Optional<InventoryBalance> targetBalance = command.getTargetLocationId().equals(firstLocationId)
                ? firstBalance : secondBalance;
        InventoryBalance source = sourceBalance
                .orElseThrow(() -> new PlatformException("WMS_INVENTORY_NOT_FOUND",
                        "inventory balance does not exist"));
        InventoryBalance target = targetBalance
                .orElseGet(() -> new InventoryBalance(command.getSkuId(), command.getWarehouseId(),
                        command.getTargetLocationId()));

        source.transferOut(command.getQuantity());
        target.transferIn(command.getQuantity());
        balanceRepository.save(source);
        balanceRepository.save(target);
        recordMovement(new InventoryMovement("TRANSFER_OUT", command, command.getSourceLocationId(), source));
        recordMovement(new InventoryMovement("TRANSFER_IN", command, command.getTargetLocationId(), target));
        auditRecorder.record("TRANSFER", "INVENTORY", command.getTransferNo());
        return new InventoryTransferView(command.getTransferNo(), source.view(), target.view());
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<InventoryTransferTaskView> listTransferTasks() {
        return movementRepository.findTransferTasks();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryBalanceView getBalance(Long skuId, Long warehouseId, Long locationId) {
        return balanceRepository.findBySkuIdAndWarehouseIdAndLocationId(skuId, warehouseId, locationId)
                .map(InventoryBalance::view)
                .orElse(new InventoryBalanceView(skuId, warehouseId, locationId, 0, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryBalanceView getExistingBalance(Long skuId, Long warehouseId, Long locationId) {
        return balanceRepository.findBySkuIdAndWarehouseIdAndLocationId(skuId, warehouseId, locationId)
                .map(InventoryBalance::view)
                .orElseThrow(() -> new PlatformException("WMS_INVENTORY_NOT_FOUND",
                        "inventory balance does not exist"));
    }

    private InventoryBalanceView change(String operationType, InventoryCommand command,
                                        Consumer<InventoryBalance> change, boolean createIfMissing) {
        // Serialize commands at the location before reading the committed idempotency record.
        StorageLocation location = locationRepository.findLockedById(command.getLocationId())
                .orElseThrow(() -> new PlatformException("WMS_LOCATION_NOT_FOUND", "storage location does not exist"));
        Optional<InventoryMovement> previous = movementRepository
                .findByOperationTypeAndIdempotencyKey(operationType, command.getIdempotencyKey());
        boolean sameRequest = previous.isPresent() && previous.get().matches(command);
        if (idempotencyGuard.decide(previous.isPresent(), sameRequest,
                "WMS_IDEMPOTENCY_CONFLICT",
                "idempotency key was already used with different request data")
                == IdempotencyDecision.SAFE_REPLAY) {
            return getBalance(command.getSkuId(), command.getWarehouseId(), command.getLocationId());
        }

        if (!location.isEnabled() || !location.getWarehouseId().equals(command.getWarehouseId())) {
            throw new PlatformException("WMS_INVALID_LOCATION", "storage location is unavailable or belongs to another warehouse");
        }

        InventoryBalance balance = balanceRepository
                .findLockedBySkuIdAndWarehouseIdAndLocationId(command.getSkuId(), command.getWarehouseId(), command.getLocationId())
                .orElseGet(() -> newBalance(command, createIfMissing));
        change.accept(balance);
        balanceRepository.save(balance);
        recordMovement(new InventoryMovement(operationType, command, balance));

        auditRecorder.record(operationType, "INVENTORY", command.getReferenceNo());
        return balance.view();
    }

    private void recordMovement(InventoryMovement movement) {
        movementStore.save(movement);
    }

    private StorageLocation lockLocation(Long locationId) {
        return locationRepository.findLockedById(locationId)
                .orElseThrow(() -> new PlatformException("WMS_LOCATION_NOT_FOUND",
                        "storage location does not exist"));
    }

    private void requireTransferLocation(StorageLocation location, Long warehouseId) {
        if (!location.isEnabled() || !location.getWarehouseId().equals(warehouseId)) {
            throw new PlatformException("WMS_INVALID_LOCATION",
                    "storage location is unavailable or belongs to another warehouse");
        }
    }

    private void requireValidTransfer(InventoryTransferCommand command) {
        if (command == null || command.getQuantity() <= 0
                || command.getSourceLocationId() == null || command.getTargetLocationId() == null
                || command.getSourceLocationId().equals(command.getTargetLocationId())) {
            throw new PlatformException("INVALID_REQUEST",
                    "quantity must be positive and source and target locations must differ");
        }
    }

    private InventoryBalance newBalance(InventoryCommand command, boolean createIfMissing) {
        if (!createIfMissing) {
            throw new PlatformException("WMS_INVENTORY_NOT_FOUND", "inventory balance does not exist");
        }
        return new InventoryBalance(command.getSkuId(), command.getWarehouseId(), command.getLocationId());
    }

}
