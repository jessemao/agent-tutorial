package com.acme.training.wms.inbound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "wms_inbound_receipt", uniqueConstraints =
        @UniqueConstraint(name = "uk_inbound_receipt_key", columnNames = "idempotency_key"))
class InboundReceipt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "inbound_id", nullable = false)
    private Long inboundId;
    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;
    @Column(nullable = false)
    private long quantity;
    @Column(name = "received_quantity", nullable = false)
    private long receivedQuantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private InboundStatus status;

    protected InboundReceipt() {}

    InboundReceipt(InboundOrder order, String idempotencyKey, long quantity) {
        this.inboundId = order.getId();
        this.idempotencyKey = idempotencyKey;
        this.quantity = quantity;
        this.receivedQuantity = order.getReceivedQuantity();
        this.status = order.getStatus();
    }

    boolean matches(Long inboundId, long quantity) {
        return this.inboundId.equals(inboundId) && this.quantity == quantity;
    }

    Long getInboundId() { return inboundId; }
    long getReceivedQuantity() { return receivedQuantity; }
    InboundStatus getStatus() { return status; }
}
