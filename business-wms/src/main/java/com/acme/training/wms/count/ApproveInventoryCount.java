package com.acme.training.wms.count;

public final class ApproveInventoryCount {
    private final String idempotencyKey;
    private final long expectedVersion;
    private final String note;
    private final String confirmationToken;

    public ApproveInventoryCount(String idempotencyKey, long expectedVersion, String note, String confirmationToken) {
        this.idempotencyKey = idempotencyKey;
        this.expectedVersion = expectedVersion;
        this.note = note;
        this.confirmationToken = confirmationToken;
    }
    public String getIdempotencyKey() { return idempotencyKey; }
    public long getExpectedVersion() { return expectedVersion; }
    public String getNote() { return note; }
    public String getConfirmationToken() { return confirmationToken; }
}
