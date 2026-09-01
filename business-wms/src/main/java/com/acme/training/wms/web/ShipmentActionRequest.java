package com.acme.training.wms.web;

import javax.validation.constraints.NotBlank;

public class ShipmentActionRequest {

    @NotBlank
    private String idempotencyKey;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

}
