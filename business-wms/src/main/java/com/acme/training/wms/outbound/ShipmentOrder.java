package com.acme.training.wms.outbound;

import com.acme.training.platform.error.PlatformException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "wms_shipment_order")
class ShipmentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(nullable = false)
    private long quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Column(name = "cancel_idempotency_key", length = 80)
    private String cancelIdempotencyKey;

    @Column(name = "reserve_idempotency_key", length = 80)
    private String reserveIdempotencyKey;

    @Column(name = "ship_idempotency_key", length = 80)
    private String shipIdempotencyKey;

    @Version
    private long version;

    protected ShipmentOrder() {
    }

    ShipmentOrder(String orderNo, Long skuId, Long warehouseId, Long locationId, long quantity) {
        this.orderNo = orderNo;
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.quantity = quantity;
        this.status = ShipmentStatus.CREATED;
    }

    void markReserved(String idempotencyKey) {
        require(status == ShipmentStatus.CREATED, "WMS_SHIPMENT_STATE",
                "only a created shipment can reserve inventory");
        status = ShipmentStatus.RESERVED;
        reserveIdempotencyKey = idempotencyKey;
    }

    void markShipped(String idempotencyKey) {
        require(status == ShipmentStatus.RESERVED, "WMS_SHIPMENT_STATE",
                "only a reserved shipment can be shipped");
        status = ShipmentStatus.SHIPPED;
        shipIdempotencyKey = idempotencyKey;
    }

    void cancel(String idempotencyKey, String reason) {
        require(status == ShipmentStatus.CREATED || status == ShipmentStatus.RESERVED,
                "WMS_SHIPMENT_STATE", "shipped or cancelled shipment cannot be cancelled");
        status = ShipmentStatus.CANCELLED;
        cancelReason = reason;
        cancelIdempotencyKey = idempotencyKey;
    }

    boolean matchesCancellation(String idempotencyKey, String reason) {
        return status == ShipmentStatus.CANCELLED
                && cancelIdempotencyKey != null
                && cancelIdempotencyKey.equals(idempotencyKey)
                && cancelReason.equals(reason);
    }

    boolean matchesReservation(String idempotencyKey) {
        return status == ShipmentStatus.RESERVED && sameKey(reserveIdempotencyKey, idempotencyKey);
    }

    boolean matchesShipment(String idempotencyKey) {
        return status == ShipmentStatus.SHIPPED && sameKey(shipIdempotencyKey, idempotencyKey);
    }

    private boolean sameKey(String recordedKey, String requestedKey) {
        return recordedKey != null && recordedKey.equals(requestedKey);
    }

    private void require(boolean condition, String code, String message) {
        if (!condition) {
            throw new PlatformException(code, message);
        }
    }

    Long getId() {
        return id;
    }

    String getOrderNo() {
        return orderNo;
    }

    Long getSkuId() {
        return skuId;
    }

    Long getWarehouseId() {
        return warehouseId;
    }

    Long getLocationId() {
        return locationId;
    }

    long getQuantity() {
        return quantity;
    }

    ShipmentStatus getStatus() {
        return status;
    }

    String getCancelReason() {
        return cancelReason;
    }
}
