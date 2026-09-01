package com.acme.training.platform.reuse;

import com.acme.training.platform.error.PlatformException;
import com.acme.training.platform.idempotency.IdempotencyDecision;
import com.acme.training.platform.idempotency.IdempotencyGuard;

public final class DefaultIdempotencyGuard implements IdempotencyGuard {

    @Override
    public IdempotencyDecision decide(boolean previousExists, boolean sameRequest,
                                      String conflictCode, String conflictMessage) {
        if (!previousExists) {
            return IdempotencyDecision.NEW_REQUEST;
        }
        if (!sameRequest) {
            throw new PlatformException(conflictCode, conflictMessage);
        }
        return IdempotencyDecision.SAFE_REPLAY;
    }
}
