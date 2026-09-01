package com.acme.training.wms.inventory;

public final class InventoryCommand {

    private final String idempotencyKey;
    private final String referenceNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long locationId;
    private final long quantity;

    public InventoryCommand(String idempotencyKey, String referenceNo, Long skuId,
                            Long warehouseId, Long locationId, long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        this.idempotencyKey = required(idempotencyKey, "idempotencyKey");
        this.referenceNo = required(referenceNo, "referenceNo");
        this.skuId = required(skuId, "skuId");
        this.warehouseId = required(warehouseId, "warehouseId");
        this.locationId = required(locationId, "locationId");
        this.quantity = quantity;
    }

    private static String required(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    private static Long required(Long value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getReferenceNo() {
        return referenceNo;
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
