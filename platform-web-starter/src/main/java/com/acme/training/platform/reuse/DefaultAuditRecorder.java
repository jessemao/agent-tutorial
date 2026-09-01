package com.acme.training.platform.reuse;

import com.acme.training.platform.audit.AuditEvent;
import com.acme.training.platform.audit.AuditPublisher;
import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.operator.CurrentOperator;

import java.time.Clock;
import java.time.Instant;

public final class DefaultAuditRecorder implements AuditRecorder {

    private final CurrentOperator currentOperator;
    private final AuditPublisher auditPublisher;
    private final Clock clock;

    public DefaultAuditRecorder(CurrentOperator currentOperator, AuditPublisher auditPublisher, Clock clock) {
        this.currentOperator = currentOperator;
        this.auditPublisher = auditPublisher;
        this.clock = clock;
    }

    @Override
    public void record(String action, String businessType, String businessId) {
        auditPublisher.publish(new AuditEvent(currentOperator.requiredOperatorId(), action,
                businessType, businessId, Instant.now(clock)));
    }
}
