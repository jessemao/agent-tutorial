package com.acme.training.platform.reuse;

import com.acme.training.platform.audit.AuditEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAuditRecorderTest {

    @Test
    void suppliesPlatformOperatorAndTimeWithoutKnowingBusinessObjects() {
        AtomicReference<AuditEvent> published = new AtomicReference<>();
        Instant now = Instant.parse("2026-08-28T03:00:00Z");
        DefaultAuditRecorder recorder = new DefaultAuditRecorder(
                () -> "trainer", published::set, Clock.fixed(now, ZoneOffset.UTC));

        recorder.record("CANCEL", "SHIPMENT", "SO-101");

        assertThat(published.get().getOperatorId()).isEqualTo("trainer");
        assertThat(published.get().getAction()).isEqualTo("CANCEL");
        assertThat(published.get().getBusinessType()).isEqualTo("SHIPMENT");
        assertThat(published.get().getBusinessId()).isEqualTo("SO-101");
        assertThat(published.get().getOccurredAt()).isEqualTo(now);
    }
}
