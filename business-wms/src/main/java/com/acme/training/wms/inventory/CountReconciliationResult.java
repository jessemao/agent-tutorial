package com.acme.training.wms.inventory;

public final class CountReconciliationResult {
    private final Long skuId; private final Long locationId; private final long bookTotal; private final long difference; private final InventoryBalanceView balance;
    CountReconciliationResult(Long skuId, Long locationId, long bookTotal, long difference, InventoryBalanceView balance) { this.skuId=skuId; this.locationId=locationId; this.bookTotal=bookTotal; this.difference=difference; this.balance=balance; }
    public Long getSkuId() { return skuId; }
    public Long getLocationId() { return locationId; }
    public long getBookTotal() { return bookTotal; }
    public long getDifference() { return difference; }
    public InventoryBalanceView getBalance() { return balance; }
}
