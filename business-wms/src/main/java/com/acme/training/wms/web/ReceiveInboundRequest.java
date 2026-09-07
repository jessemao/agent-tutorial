package com.acme.training.wms.web;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

public class ReceiveInboundRequest {

    @NotBlank
    private String idempotencyKey;

    @Positive
    private long quantity;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
