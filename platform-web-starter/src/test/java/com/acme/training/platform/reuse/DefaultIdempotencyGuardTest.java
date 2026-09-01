package com.acme.training.platform.reuse;

import com.acme.training.platform.error.PlatformException;
import com.acme.training.platform.idempotency.IdempotencyDecision;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultIdempotencyGuardTest {

    private final DefaultIdempotencyGuard guard = new DefaultIdempotencyGuard();

    @Test
    void distinguishesNewRequestFromSafeReplay() {
        assertThat(guard.decide(false, false, "UNIT_CONFLICT", "conflict"))
                .isEqualTo(IdempotencyDecision.NEW_REQUEST);
        assertThat(guard.decide(true, true, "UNIT_CONFLICT", "conflict"))
                .isEqualTo(IdempotencyDecision.SAFE_REPLAY);
    }

    @Test
    void rejectsReuseWithDifferentBusinessPayload() {
        assertThatThrownBy(() -> guard.decide(true, false, "UNIT_IDEMPOTENCY_CONFLICT", "different payload"))
                .isInstanceOf(PlatformException.class)
                .hasMessage("different payload")
                .extracting("code")
                .isEqualTo("UNIT_IDEMPOTENCY_CONFLICT");
    }
}
