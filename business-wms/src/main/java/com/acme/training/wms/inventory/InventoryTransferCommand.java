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
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
        if (transferNo == null || transferNo.trim().isEmpty()) {
            throw new IllegalArgumentException("transferNo is required");
        }
        if (skuId == null || warehouseId == null || sourceLocationId == null || targetLocationId == null) {
            throw new IllegalArgumentException("inventory dimension is required");
        }
        if (sourceLocationId.equals(targetLocationId)) {
            throw new IllegalArgumentException("source and target locations must be different");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        this.idempotencyKey = idempotencyKey;
        this.transferNo = transferNo;
        this.skuId = skuId;
        this.warehouseId = warehouseId;
        this.sourceLocationId = sourceLocationId;
        this.targetLocationId = targetLocationId;
        this.quantity = quantity;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getTransferNo() { return transferNo; }
    public Long getSkuId() { return skuId; }
    public Long getWarehouseId() { return warehouseId; }
    public Long getSourceLocationId() { return sourceLocationId; }
    public Long getTargetLocationId() { return targetLocationId; }
    public long getQuantity() { return quantity; }
}
