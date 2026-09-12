package com.acme.training.wms.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "wms_inventory_movement", uniqueConstraints =
        @UniqueConstraint(name = "uk_movement_idempotency",
                columnNames = {"operation_type", "idempotency_key"}))
class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type", nullable = false, length = 20)
    private String operationType;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @Column(name = "reference_no", nullable = false, length = 64)
    private String referenceNo;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(nullable = false)
    private long quantity;

    @Column(name = "available_after", nullable = false)
    private long availableAfter;

    @Column(name = "reserved_after", nullable = false)
    private long reservedAfter;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected InventoryMovement() {
    }

    InventoryMovement(String operationType, InventoryCommand command, InventoryBalance balance) {
        this.operationType = operationType;
        this.idempotencyKey = command.getIdempotencyKey();
        this.referenceNo = command.getReferenceNo();
        this.skuId = command.getSkuId();
        this.warehouseId = command.getWarehouseId();
        this.locationId = command.getLocationId();
        this.quantity = command.getQuantity();
        this.availableAfter = balance.getAvailableQuantity();
        this.reservedAfter = balance.getReservedQuantity();
        this.createdAt = Instant.now();
    }

    InventoryMovement(String operationType, InventoryTransferCommand command,
                      Long locationId, InventoryBalance balance) {
        this.operationType = operationType;
        this.idempotencyKey = command.getIdempotencyKey();
        this.referenceNo = command.getTransferNo();
        this.skuId = command.getSkuId();
        this.warehouseId = command.getWarehouseId();
        this.locationId = locationId;
        this.quantity = command.getQuantity();
        this.availableAfter = balance.getAvailableQuantity();
        this.reservedAfter = balance.getReservedQuantity();
        this.createdAt = Instant.now();
    }

    InventoryBalanceView result() {
        return new InventoryBalanceView(skuId, warehouseId, locationId, availableAfter, reservedAfter);
    }

    boolean matches(InventoryCommand command) {
        return referenceNo.equals(command.getReferenceNo())
                && skuId.equals(command.getSkuId())
                && warehouseId.equals(command.getWarehouseId())
                && locationId.equals(command.getLocationId())
                && quantity == command.getQuantity();
    }

    boolean matches(InventoryTransferCommand command, Long expectedLocationId) {
        return referenceNo.equals(command.getTransferNo())
                && skuId.equals(command.getSkuId())
                && warehouseId.equals(command.getWarehouseId())
                && locationId.equals(expectedLocationId)
                && quantity == command.getQuantity();
    }
}
