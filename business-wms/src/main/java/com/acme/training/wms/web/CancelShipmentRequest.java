package com.acme.training.wms.web;

import javax.validation.constraints.NotBlank;

public class CancelShipmentRequest {

    @NotBlank
    private String idempotencyKey;

    @NotBlank
    private String reason;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
