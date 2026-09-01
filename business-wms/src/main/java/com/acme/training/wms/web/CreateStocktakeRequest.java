package com.acme.training.wms.web;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

public class CreateStocktakeRequest {

    @NotBlank
    private String countNo;

    @NotNull
    private Long skuId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private Long locationId;

    @PositiveOrZero
    private long countedTotalQuantity;

    public String getCountNo() { return countNo; }
    public void setCountNo(String countNo) { this.countNo = countNo; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public long getCountedTotalQuantity() { return countedTotalQuantity; }
    public void setCountedTotalQuantity(long countedTotalQuantity) {
        this.countedTotalQuantity = countedTotalQuantity;
    }
}
