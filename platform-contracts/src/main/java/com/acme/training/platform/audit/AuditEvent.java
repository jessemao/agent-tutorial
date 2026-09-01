package com.acme.training.platform.audit;

import java.time.Instant;

public final class AuditEvent {

    private final String operatorId;
    private final String action;
    private final String businessType;
    private final String businessId;
    private final Instant occurredAt;

    public AuditEvent(String operatorId, String action, String businessType,
                      String businessId, Instant occurredAt) {
        this.operatorId = operatorId;
        this.action = action;
        this.businessType = businessType;
        this.businessId = businessId;
        this.occurredAt = occurredAt;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getAction() {
        return action;
    }

    public String getBusinessType() {
        return businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
