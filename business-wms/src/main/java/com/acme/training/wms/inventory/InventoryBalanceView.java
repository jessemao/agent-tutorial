package com.acme.training.wms.inventory;

public final class InventoryBalanceView {

    private final Long skuId;
    private final Long warehouseId;
    private final Long locationId;
    private final long availableQuantity;
    private final long reservedQuantity;

    public InventoryBalanceView(Long skuId, Long warehouseId, Long locationId,
                                long availableQuantity, long reservedQuantity) {
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
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

    public long getAvailableQuantity() {
        return availableQuantity;
    }

    public long getReservedQuantity() {
        return reservedQuantity;
    }
}
