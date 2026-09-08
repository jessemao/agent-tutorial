package com.acme.training.wms.inbound;

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
@Table(name = "wms_inbound_order")
class InboundOrder {

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

    @Column(name = "planned_quantity", nullable = false)
    private long plannedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private long receivedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private InboundStatus status;

    @Column(name = "receipt_idempotency_key", length = 80)
    private String receiptIdempotencyKey;

    @Version
    private long version;

    protected InboundOrder() {
    }

    InboundOrder(CreateInbound command) {
        this.orderNo = command.getOrderNo();
        this.skuId = command.getSkuId();
        this.warehouseId = command.getWarehouseId();
        this.locationId = command.getLocationId();
        this.plannedQuantity = command.getPlannedQuantity();
        this.status = InboundStatus.CREATED;
    }

    void receive(long quantity) {
        require(status != InboundStatus.RECEIVED, "WMS_INBOUND_STATE", "inbound order has already been received");
        require(quantity <= plannedQuantity - receivedQuantity, "WMS_INBOUND_OVER_RECEIPT",
                "received quantity exceeds the remaining planned quantity");
        receivedQuantity += quantity;
        status = receivedQuantity == plannedQuantity ? InboundStatus.RECEIVED : InboundStatus.PARTIALLY_RECEIVED;
    }

    boolean matchesLegacyReceipt(String idempotencyKey, long quantity) {
        return status == InboundStatus.RECEIVED
                && receiptIdempotencyKey != null
                && receiptIdempotencyKey.equals(idempotencyKey)
                && receivedQuantity == quantity;
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

    long getPlannedQuantity() {
        return plannedQuantity;
    }

    long getReceivedQuantity() {
        return receivedQuantity;
    }

    InboundStatus getStatus() {
        return status;
    }

    String getReceiptIdempotencyKey() {
        return receiptIdempotencyKey;
    }
}
