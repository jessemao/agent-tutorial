package com.acme.training.wms.outbound;

public final class ShipmentView {

    private final Long id;
    private final String orderNo;
    private final ShipmentStatus status;
    private final String cancelReason;

    ShipmentView(ShipmentOrder order) {
        this.id = order.getId();
        this.orderNo = order.getOrderNo();
        this.status = order.getStatus();
        this.cancelReason = order.getCancelReason();
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public String getCancelReason() {
        return cancelReason;
    }
}
