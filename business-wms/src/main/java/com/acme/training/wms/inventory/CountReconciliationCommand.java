package com.acme.training.wms.inventory;

import java.util.List;

public final class CountReconciliationCommand {
    private final String countNo; private final Long warehouseId; private final List<CountReconciliationLine> lines; private final String confirmationContext; private final String confirmedFacts;
    public CountReconciliationCommand(String countNo, Long warehouseId, List<CountReconciliationLine> lines) { this(countNo,warehouseId,lines,null,null); }
    public CountReconciliationCommand(String countNo, Long warehouseId, List<CountReconciliationLine> lines, String confirmationContext, String confirmedFacts) { this.countNo=countNo; this.warehouseId=warehouseId; this.lines=lines; this.confirmationContext=confirmationContext; this.confirmedFacts=confirmedFacts; }
    public String getCountNo() { return countNo; }
    public Long getWarehouseId() { return warehouseId; }
    public List<CountReconciliationLine> getLines() { return lines; }
    public String getConfirmationContext() { return confirmationContext; }
    public String getConfirmedFacts() { return confirmedFacts; }
}
