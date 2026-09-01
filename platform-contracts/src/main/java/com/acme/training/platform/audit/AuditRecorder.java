package com.acme.training.platform.audit;

/** Records a business audit event with platform-provided operator and time. */
public interface AuditRecorder {

    void record(String action, String businessType, String businessId);
}
