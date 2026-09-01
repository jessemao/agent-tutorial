package com.acme.training.wms.inventory;

import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.error.PlatformException;
import com.acme.training.platform.idempotency.IdempotencyDecision;
import com.acme.training.platform.idempotency.IdempotencyGuard;
import com.acme.training.wms.masterdata.StorageLocation;
import com.acme.training.wms.masterdata.StorageLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Consumer;

@Service
class InventoryService implements InventoryOperations {

    private final InventoryBalanceRepository balanceRepository;
    private final InventoryMovementRepository movementRepository;
    private final StorageLocationRepository locationRepository;
    private final AuditRecorder auditRecorder;
    private final IdempotencyGuard idempotencyGuard;

    InventoryService(InventoryBalanceRepository balanceRepository,
                     InventoryMovementRepository movementRepository,
                     StorageLocationRepository locationRepository,
                     AuditRecorder auditRecorder,
                     IdempotencyGuard idempotencyGuard) {
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
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
        InventoryCommand sourceCommand = transferCommand(command, command.getSourceLocationId());
        InventoryCommand targetCommand = transferCommand(command, command.getTargetLocationId());
        // Read both movement records only after taking the ordered location locks.
        lockAndValidateTransferLocations(command);
        Optional<InventoryTransferView> replay = transferReplay(command, sourceCommand, targetCommand);
        if (replay.isPresent()) {
            return replay.get();
        }
        InventoryBalance source = balanceRepository.findLockedBySkuIdAndWarehouseIdAndLocationId(
                        command.getSkuId(), command.getWarehouseId(), command.getSourceLocationId())
                .orElseThrow(() -> new PlatformException("WMS_INVENTORY_NOT_FOUND", "source inventory does not exist"));
        InventoryBalance target = balanceRepository.findLockedBySkuIdAndWarehouseIdAndLocationId(
                        command.getSkuId(), command.getWarehouseId(), command.getTargetLocationId())
                .orElseGet(() -> new InventoryBalance(command.getSkuId(), command.getWarehouseId(),
                        command.getTargetLocationId()));

        source.moveOut(command.getQuantity());
        target.moveIn(command.getQuantity());
        balanceRepository.save(source);
        balanceRepository.save(target);
        recordMovement(new InventoryMovement("TRANSFER_OUT", sourceCommand, source));
        recordMovement(new InventoryMovement("TRANSFER_IN", targetCommand, target));
        auditRecorder.record("TRANSFER", "INVENTORY", command.getTransferNo());
        return new InventoryTransferView(command.getTransferNo(), source.view(), target.view());
    }

    @Override
    @Transactional
    public InventoryBalanceView adjustFromCount(InventoryCountAdjustmentCommand command) {
        InventoryBalance balance = balanceRepository.findLockedBySkuIdAndWarehouseIdAndLocationId(
                        command.getSkuId(), command.getWarehouseId(), command.getLocationId())
                .orElseThrow(() -> new PlatformException("WMS_INVENTORY_NOT_FOUND",
                        "inventory balance does not exist"));
        Optional<InventoryMovement> previous = movementRepository
                .findByOperationTypeAndIdempotencyKey("COUNT_ADJUST", command.getIdempotencyKey());
        boolean sameRequest = previous.isPresent() && previous.get().matches(command);
        if (idempotencyGuard.decide(previous.isPresent(), sameRequest,
                "WMS_IDEMPOTENCY_CONFLICT",
                "idempotency key was already used with different count data")
                == IdempotencyDecision.SAFE_REPLAY) {
            return getBalance(command.getSkuId(), command.getWarehouseId(), command.getLocationId());
        }

        if (balance.getTotalQuantity() != command.getExpectedTotalQuantity()
                || balance.getReservedQuantity() != command.getExpectedReservedQuantity()) {
            throw new PlatformException("WMS_COUNT_SNAPSHOT_STALE",
                    "inventory changed after the count was created; recount is required");
        }

        balance.adjustToCountedTotal(command.getCountedTotalQuantity());
        balanceRepository.save(balance);
        recordMovement(new InventoryMovement(command, balance));
        auditRecorder.record("COUNT_ADJUST", "INVENTORY", command.getCountNo());
        return balance.view();
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
        try {
            movementRepository.saveAndFlush(movement);
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException) {
                    String name = ((ConstraintViolationException) cause).getConstraintName();
                    // H2 reports the backing index plus a description; MySQL reports the named key.
                    if (name != null && name.replace("\"", "").matches(
                            "(?is)(?:\\w+\\.)?uk_movement_idempotency(?:_index_\\w+)?(?: on .*|$)")) {
                        PlatformException conflict = new PlatformException("WMS_IDEMPOTENCY_CONFLICT",
                                "idempotency key was concurrently used with conflicting inventory data");
                        conflict.initCause(exception);
                        throw conflict; // Do not continue in the failed transaction; roll back the entire action.
                    }
                }
            }
            throw exception;
        }
    }

    private InventoryBalance newBalance(InventoryCommand command, boolean createIfMissing) {
        if (!createIfMissing) {
            throw new PlatformException("WMS_INVENTORY_NOT_FOUND", "inventory balance does not exist");
        }
        return new InventoryBalance(command.getSkuId(), command.getWarehouseId(), command.getLocationId());
    }

    private void lockAndValidateTransferLocations(InventoryTransferCommand command) {
        Long first = Math.min(command.getSourceLocationId(), command.getTargetLocationId());
        Long second = Math.max(command.getSourceLocationId(), command.getTargetLocationId());
        validateTransferLocation(locationRepository.findLockedById(first)
                .orElseThrow(() -> new PlatformException("WMS_LOCATION_NOT_FOUND", "storage location does not exist")), command);
        validateTransferLocation(locationRepository.findLockedById(second)
                .orElseThrow(() -> new PlatformException("WMS_LOCATION_NOT_FOUND", "storage location does not exist")), command);
    }

    private void validateTransferLocation(StorageLocation location, InventoryTransferCommand command) {
        if (!location.isEnabled() || !location.getWarehouseId().equals(command.getWarehouseId())) {
            throw new PlatformException("WMS_INVALID_LOCATION",
                    "both transfer locations must be enabled and belong to the warehouse");
        }
    }

    private InventoryCommand transferCommand(InventoryTransferCommand command, Long locationId) {
        return new InventoryCommand(command.getIdempotencyKey(), command.getTransferNo(), command.getSkuId(),
                command.getWarehouseId(), locationId, command.getQuantity());
    }

    private Optional<InventoryTransferView> transferReplay(InventoryTransferCommand command,
                                                          InventoryCommand source, InventoryCommand target) {
        Optional<InventoryMovement> previousOut = movementRepository
                .findByOperationTypeAndIdempotencyKey("TRANSFER_OUT", command.getIdempotencyKey());
        Optional<InventoryMovement> previousIn = movementRepository
                .findByOperationTypeAndIdempotencyKey("TRANSFER_IN", command.getIdempotencyKey());
        boolean exists = previousOut.isPresent() || previousIn.isPresent();
        boolean same = previousOut.isPresent() && previousIn.isPresent()
                && previousOut.get().matches(source) && previousIn.get().matches(target);
        if (idempotencyGuard.decide(exists, same, "WMS_IDEMPOTENCY_CONFLICT",
                "idempotency key was already used with different transfer data")
                == IdempotencyDecision.SAFE_REPLAY) {
            return Optional.of(new InventoryTransferView(command.getTransferNo(),
                    previousOut.get().result(), previousIn.get().result()));
        }
        return Optional.empty();
    }
}
