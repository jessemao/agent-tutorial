package com.acme.training.wms.outbound;

import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.error.PlatformException;
import com.acme.training.platform.idempotency.IdempotencyDecision;
import com.acme.training.platform.idempotency.IdempotencyGuard;
import com.acme.training.wms.inventory.InventoryCommand;
import com.acme.training.wms.inventory.InventoryOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ShipmentService implements ShipmentOperations {

    private final ShipmentOrderRepository shipmentRepository;
    private final InventoryOperations inventoryOperations;
    private final AuditRecorder auditRecorder;
    private final IdempotencyGuard idempotencyGuard;

    ShipmentService(ShipmentOrderRepository shipmentRepository,
                    InventoryOperations inventoryOperations,
                    AuditRecorder auditRecorder,
                    IdempotencyGuard idempotencyGuard) {
        this.shipmentRepository = shipmentRepository;
        this.inventoryOperations = inventoryOperations;
        this.auditRecorder = auditRecorder;
        this.idempotencyGuard = idempotencyGuard;
    }

    @Override
    @Transactional
    public ShipmentView create(CreateShipment command) {
        ShipmentOrder order = shipmentRepository.save(new ShipmentOrder(command.getOrderNo(),
                command.getSkuId(), command.getWarehouseId(), command.getLocationId(), command.getQuantity()));
        audit("CREATE", order);
        return new ShipmentView(order);
    }

    @Override
    @Transactional
    public ShipmentView reserve(Long shipmentId, String idempotencyKey) {
        requireIdempotencyKey(idempotencyKey);
        ShipmentOrder order = locked(shipmentId);
        if (order.getStatus() == ShipmentStatus.RESERVED) {
            if (!order.matchesReservation(idempotencyKey)) {
                throw idempotencyConflict("shipment was already reserved by another request");
            }
            return new ShipmentView(order);
        }
        order.markReserved(idempotencyKey);
        inventoryOperations.reserve(inventoryCommand(order, idempotencyKey));
        audit("RESERVE", order);
        return new ShipmentView(order);
    }

    @Override
    @Transactional
    public ShipmentView ship(Long shipmentId, String idempotencyKey) {
        requireIdempotencyKey(idempotencyKey);
        ShipmentOrder order = locked(shipmentId);
        if (order.getStatus() == ShipmentStatus.SHIPPED) {
            if (!order.matchesShipment(idempotencyKey)) {
                throw idempotencyConflict("shipment was already shipped by another request");
            }
            return new ShipmentView(order);
        }
        order.markShipped(idempotencyKey);
        inventoryOperations.ship(inventoryCommand(order, idempotencyKey));
        audit("SHIP", order);
        return new ShipmentView(order);
    }

    @Override
    @Transactional
    public ShipmentView cancel(Long shipmentId, String idempotencyKey, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new PlatformException("WMS_CANCEL_REASON_REQUIRED", "cancel reason is required");
        }
        String normalizedReason = reason.trim();
        ShipmentOrder order = locked(shipmentId);
        if (order.getStatus() == ShipmentStatus.CANCELLED) {
            if (idempotencyGuard.decide(true, order.matchesCancellation(idempotencyKey, normalizedReason),
                    "WMS_IDEMPOTENCY_CONFLICT",
                    "shipment was already cancelled by another request") == IdempotencyDecision.SAFE_REPLAY) {
                return new ShipmentView(order);
            }
        }
        boolean releaseInventory = order.getStatus() == ShipmentStatus.RESERVED;
        order.cancel(idempotencyKey, normalizedReason);
        if (releaseInventory) {
            inventoryOperations.release(inventoryCommand(order, idempotencyKey));
        }
        audit("CANCEL", order);
        return new ShipmentView(order);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentView get(Long shipmentId) {
        return new ShipmentView(shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new PlatformException("WMS_SHIPMENT_NOT_FOUND", "shipment order does not exist")));
    }

    private ShipmentOrder locked(Long shipmentId) {
        return shipmentRepository.findLockedById(shipmentId)
                .orElseThrow(() -> new PlatformException("WMS_SHIPMENT_NOT_FOUND", "shipment order does not exist"));
    }

    private InventoryCommand inventoryCommand(ShipmentOrder order, String idempotencyKey) {
        return new InventoryCommand(idempotencyKey, order.getOrderNo(), order.getSkuId(),
                order.getWarehouseId(), order.getLocationId(), order.getQuantity());
    }

    private void audit(String action, ShipmentOrder order) {
        auditRecorder.record(action, "SHIPMENT", order.getOrderNo());
    }

    private void requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new PlatformException("WMS_IDEMPOTENCY_REQUIRED", "idempotency key is required");
        }
    }

    private PlatformException idempotencyConflict(String message) {
        return new PlatformException("WMS_IDEMPOTENCY_CONFLICT", message);
    }
}
