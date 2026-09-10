package com.acme.training.wms.inventory;

public final class InventoryTransferCommand {

    private final String idempotencyKey;
    private final String transferNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long sourceLocationId;
    private final Long targetLocationId;
    private final long quantity;

    public InventoryTransferCommand(String idempotencyKey, String transferNo, Long skuId, Long warehouseId,
                                    Long sourceLocationId, Long targetLocationId, long quantity) {
        this.idempotencyKey = idempotencyKey;
        this.transferNo = transferNo;
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.sourceLocationId = sourceLocationId;
        this.targetLocationId = targetLocationId;
        this.quantity = quantity;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getTransferNo() {
        return transferNo;
    }

    public Long getSkuId() {
        return skuId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Long getSourceLocationId() {
        return sourceLocationId;
    }

    public Long getTargetLocationId() {
        return targetLocationId;
    }

    public long getQuantity() {
        return quantity;
    }
}
