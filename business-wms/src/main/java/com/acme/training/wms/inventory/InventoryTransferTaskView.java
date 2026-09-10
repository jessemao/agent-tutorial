package com.acme.training.wms.inventory;

import java.time.Instant;

public final class InventoryTransferTaskView {
    private final String transferNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long sourceLocationId;
    private final Long targetLocationId;
    private final long quantity;
    private final Instant createdAt;

    public InventoryTransferTaskView(String transferNo, Long skuId, Long warehouseId,
                                     Long sourceLocationId, Long targetLocationId,
                                     long quantity, Instant createdAt) {
        this.transferNo = transferNo;
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.sourceLocationId = sourceLocationId;
        this.targetLocationId = targetLocationId;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public String getTransferNo() { return transferNo; }
    public Long getSkuId() { return skuId; }
    public Long getWarehouseId() { return warehouseId; }
    public Long getSourceLocationId() { return sourceLocationId; }
    public Long getTargetLocationId() { return targetLocationId; }
    public long getQuantity() { return quantity; }
    public Instant getCreatedAt() { return createdAt; }
}
