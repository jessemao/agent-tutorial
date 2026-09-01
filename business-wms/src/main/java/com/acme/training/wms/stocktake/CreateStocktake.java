package com.acme.training.wms.stocktake;

public final class CreateStocktake {

    private final String countNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long locationId;
    private final long countedTotalQuantity;

    public CreateStocktake(String countNo, Long skuId, Long warehouseId,
                           Long locationId, long countedTotalQuantity) {
        this.countNo = countNo;
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.countedTotalQuantity = countedTotalQuantity;
    }

    public String getCountNo() { return countNo; }
    public Long getSkuId() { return skuId; }
    public Long getWarehouseId() { return warehouseId; }
    public Long getLocationId() { return locationId; }
    public long getCountedTotalQuantity() { return countedTotalQuantity; }
}
