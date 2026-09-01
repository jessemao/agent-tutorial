package com.acme.training.wms.stocktake;

public final class StocktakeView {

    private final Long id;
    private final String countNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long locationId;
    private final long snapshotTotalQuantity;
    private final long snapshotReservedQuantity;
    private final long countedTotalQuantity;
    private final StocktakeStatus status;

    StocktakeView(Stocktake stocktake) {
        this.id = stocktake.getId();
        this.countNo = stocktake.getCountNo();
        this.skuId = stocktake.getSkuId();
        this.warehouseId = stocktake.getWarehouseId();
        this.locationId = stocktake.getLocationId();
        this.snapshotTotalQuantity = stocktake.getSnapshotTotalQuantity();
        this.snapshotReservedQuantity = stocktake.getSnapshotReservedQuantity();
        this.countedTotalQuantity = stocktake.getCountedTotalQuantity();
        this.status = stocktake.getStatus();
    }

    public Long getId() { return id; }
    public String getCountNo() { return countNo; }
    public Long getSkuId() { return skuId; }
    public Long getWarehouseId() { return warehouseId; }
    public Long getLocationId() { return locationId; }
    public long getSnapshotTotalQuantity() { return snapshotTotalQuantity; }
    public long getSnapshotReservedQuantity() { return snapshotReservedQuantity; }
    public long getCountedTotalQuantity() { return countedTotalQuantity; }
    public StocktakeStatus getStatus() { return status; }
}
