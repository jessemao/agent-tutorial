package com.acme.training.wms.inventory;

public final class InventoryCountAdjustmentCommand {

    private final String idempotencyKey;
    private final String countNo;
    private final Long skuId;
    private final Long warehouseId;
    private final Long locationId;
    private final long expectedTotalQuantity;
    private final long expectedReservedQuantity;
    private final long countedTotalQuantity;

    public InventoryCountAdjustmentCommand(String idempotencyKey, String countNo, Long skuId,
                                           Long warehouseId, Long locationId,
                                           long expectedTotalQuantity, long expectedReservedQuantity,
                                           long countedTotalQuantity) {
        this.idempotencyKey = required(idempotencyKey, "idempotencyKey");
        this.countNo = required(countNo, "countNo");
        this.skuId = required(skuId, "skuId");
        this.warehouseId = required(warehouseId, "warehouseId");
        this.locationId = required(locationId, "locationId");
        this.expectedTotalQuantity = nonNegative(expectedTotalQuantity, "expectedTotalQuantity");
        this.expectedReservedQuantity = nonNegative(expectedReservedQuantity, "expectedReservedQuantity");
        this.countedTotalQuantity = nonNegative(countedTotalQuantity, "countedTotalQuantity");
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

    private static long nonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getCountNo() { return countNo; }
    public Long getSkuId() { return skuId; }
    public Long getWarehouseId() { return warehouseId; }
    public Long getLocationId() { return locationId; }
    public long getExpectedTotalQuantity() { return expectedTotalQuantity; }
    public long getExpectedReservedQuantity() { return expectedReservedQuantity; }
    public long getCountedTotalQuantity() { return countedTotalQuantity; }
}
