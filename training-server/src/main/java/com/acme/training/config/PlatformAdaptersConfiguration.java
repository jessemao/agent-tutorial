package com.acme.training.config;

import com.acme.training.platform.audit.AuditEvent;
import com.acme.training.platform.audit.AuditPublisher;
import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.error.PlatformException;
import com.acme.training.platform.idempotency.IdempotencyGuard;
import com.acme.training.platform.operator.CurrentOperator;
import com.acme.training.platform.reuse.DefaultAuditRecorder;
import com.acme.training.platform.reuse.DefaultIdempotencyGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;

@Configuration
public class PlatformAdaptersConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformAdaptersConfiguration.class);

    @Bean
    @RequestScope
    public CurrentOperator currentOperator(HttpServletRequest request) {
        final String operatorId = request.getHeader("X-Operator");
        return new CurrentOperator() {
            @Override
            public String requiredOperatorId() {
                if (operatorId == null || operatorId.trim().isEmpty()) {
                    throw new PlatformException("PLATFORM_OPERATOR_REQUIRED", "X-Operator header is required");
                }
                return operatorId;
            }
        };
    }

    @Bean
    public AuditPublisher auditPublisher() {
        return new AuditPublisher() {
            @Override
            public void publish(AuditEvent event) {
                LOG.info("audit operator={} action={} type={} id={}", event.getOperatorId(),
                        event.getAction(), event.getBusinessType(), event.getBusinessId());
            }
        };
    }

    @Bean
    public AuditRecorder auditRecorder(CurrentOperator currentOperator, AuditPublisher auditPublisher) {
        return new DefaultAuditRecorder(currentOperator, auditPublisher, Clock.systemUTC());
    }

    @Bean
    public IdempotencyGuard idempotencyGuard() {
        return new DefaultIdempotencyGuard();
    }
}
