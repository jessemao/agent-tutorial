package com.acme.training.platform.audit;

/** Platform seam; production adapters may write to a database or message broker. */
public interface AuditPublisher {

    void publish(AuditEvent event);
}
