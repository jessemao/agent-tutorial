package com.acme.training.wms.web;

import javax.validation.constraints.NotBlank;

public class ApproveInventoryCountRequest {
    @NotBlank private String idempotencyKey; private long expectedVersion; private String note; private String confirmationToken;
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey=idempotencyKey; }
    public long getExpectedVersion() { return expectedVersion; }
    public void setExpectedVersion(long expectedVersion) { this.expectedVersion=expectedVersion; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note=note; }
    public String getConfirmationToken() { return confirmationToken; }
    public void setConfirmationToken(String confirmationToken) { this.confirmationToken=confirmationToken; }
}
