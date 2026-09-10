package com.acme.training.wms.inventory;

import com.acme.training.platform.error.PlatformException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@Table(name = "wms_inventory_balance", uniqueConstraints =
        @UniqueConstraint(name = "uk_inventory_dimension",
                columnNames = {"sku_id", "warehouse_id", "location_id"}))
class InventoryBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "available_quantity", nullable = false)
    private long availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private long reservedQuantity;

    @Version
    private long version;

    protected InventoryBalance() {
    }

    InventoryBalance(Long skuId, Long warehouseId, Long locationId) {
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
    }

    void receive(long quantity) {
        // Capacity includes reserved stock: releasing it later must remain representable too.
        require(quantity <= Long.MAX_VALUE - availableQuantity - reservedQuantity, "WMS_INVENTORY_OVERFLOW",
                "inventory quantity exceeds the supported range");
        availableQuantity += quantity;
    }

    void reserve(long quantity) {
        require(availableQuantity >= quantity, "WMS_INSUFFICIENT_AVAILABLE", "available inventory is insufficient");
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    void release(long quantity) {
        require(reservedQuantity >= quantity, "WMS_INSUFFICIENT_RESERVED", "reserved inventory is insufficient");
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    void ship(long quantity) {
        require(reservedQuantity >= quantity, "WMS_INSUFFICIENT_RESERVED", "reserved inventory is insufficient");
        reservedQuantity -= quantity;
    }

    void transferOut(long quantity) {
        require(availableQuantity >= quantity, "WMS_INSUFFICIENT_AVAILABLE", "available inventory is insufficient");
        availableQuantity -= quantity;
    }

    void transferIn(long quantity) {
        require(quantity <= Long.MAX_VALUE - availableQuantity - reservedQuantity, "WMS_INVENTORY_OVERFLOW",
                "inventory quantity exceeds the supported range");
        availableQuantity += quantity;
    }

    void adjustToCountedTotal(long countedTotalQuantity) {
        require(countedTotalQuantity >= reservedQuantity, "WMS_COUNT_BELOW_RESERVED",
                "counted total quantity cannot be lower than reserved quantity");
        availableQuantity = countedTotalQuantity - reservedQuantity;
    }

    long getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    private void require(boolean condition, String code, String message) {
        if (!condition) {
            throw new PlatformException(code, message);
        }
    }

    InventoryBalanceView view() {
        return new InventoryBalanceView(skuId, warehouseId, locationId,
                availableQuantity, reservedQuantity);
    }

    long getAvailableQuantity() {
        return availableQuantity;
    }

    long getReservedQuantity() {
        return reservedQuantity;
    }
}
