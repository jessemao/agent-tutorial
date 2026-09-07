package com.acme.training.wms.web;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class CreateInboundRequest {

    @NotBlank
    private String orderNo;

    @NotNull
    private Long skuId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private Long locationId;

    @Positive
    private long plannedQuantity;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public long getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(long plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }
}
