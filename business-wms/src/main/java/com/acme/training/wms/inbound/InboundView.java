package com.acme.training.wms.inbound;

public final class InboundView {

    private final Long id;
    private final String orderNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long locationId;
    private final long plannedQuantity;
    private final long receivedQuantity;
    private final InboundStatus status;

    InboundView(InboundOrder order) {
        this(order, order.getReceivedQuantity(), order.getStatus());
    }

    InboundView(InboundOrder order, long receivedQuantity, InboundStatus status) {
        this.id = order.getId();
        this.orderNo = order.getOrderNo();
        this.skuId = order.getSkuId();
        this.warehouseId = order.getWarehouseId();
        this.locationId = order.getLocationId();
        this.plannedQuantity = order.getPlannedQuantity();
        this.receivedQuantity = receivedQuantity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getSkuId() {
        return skuId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public long getPlannedQuantity() {
        return plannedQuantity;
    }

    public long getReceivedQuantity() {
        return receivedQuantity;
    }

    public InboundStatus getStatus() {
        return status;
    }
}
