package com.acme.training.platform.idempotency;

/** Decides only the common idempotency outcome; business modules own lookup and payload matching. */
public interface IdempotencyGuard {

    IdempotencyDecision decide(boolean previousExists, boolean sameRequest,
                               String conflictCode, String conflictMessage);
}
