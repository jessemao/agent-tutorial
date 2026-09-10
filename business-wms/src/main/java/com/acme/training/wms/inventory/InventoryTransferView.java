package com.acme.training.wms.inventory;

public final class InventoryTransferView {

    private final String transferNo;
    private final InventoryBalanceView source;
    private final InventoryBalanceView target;

    public InventoryTransferView(String transferNo, InventoryBalanceView source, InventoryBalanceView target) {
        this.transferNo = transferNo;
        this.source = source;
        this.target = target;
    }

    public String getTransferNo() {
        return transferNo;
    }

    public InventoryBalanceView getSource() {
        return source;
    }

    public InventoryBalanceView getTarget() {
        return target;
    }
}
