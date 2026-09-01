package com.acme.training.platform.idempotency;

public enum IdempotencyDecision {
    NEW_REQUEST,
    SAFE_REPLAY
}
