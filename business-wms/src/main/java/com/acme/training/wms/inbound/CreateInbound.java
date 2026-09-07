package com.acme.training.wms.inbound;

public final class CreateInbound {

    private final String orderNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long locationId;
    private final long plannedQuantity;

    public CreateInbound(String orderNo, Long skuId, Long warehouseId, Long locationId, long plannedQuantity) {
        this.orderNo = orderNo;
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.plannedQuantity = plannedQuantity;
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
}
