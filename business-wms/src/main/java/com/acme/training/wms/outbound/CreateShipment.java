package com.acme.training.wms.outbound;

public final class CreateShipment {

    private final String orderNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long locationId;
    private final long quantity;

    public CreateShipment(String orderNo, Long skuId, Long warehouseId, Long locationId, long quantity) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new IllegalArgumentException("orderNo is required");
        }
        if (skuId == null || warehouseId == null || locationId == null || quantity <= 0) {
            throw new IllegalArgumentException("inventory dimension and positive quantity are required");
        }
        this.orderNo = orderNo;
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.quantity = quantity;
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

    public long getQuantity() {
        return quantity;
    }
}
