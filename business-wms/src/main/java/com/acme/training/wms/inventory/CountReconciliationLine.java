package com.acme.training.wms.inventory;

public final class CountReconciliationLine {
    private final Long skuId;
    private final Long locationId;
    private final long countedTotal;
    private final long expectedBookTotal;
    public CountReconciliationLine(Long skuId, Long locationId, long countedTotal, long expectedBookTotal) { this.skuId=skuId; this.locationId=locationId; this.countedTotal=countedTotal; this.expectedBookTotal=expectedBookTotal; }
    public Long getSkuId() { return skuId; }
    public Long getLocationId() { return locationId; }
    public long getCountedTotal() { return countedTotal; }
    public long getExpectedBookTotal() { return expectedBookTotal; }
}
