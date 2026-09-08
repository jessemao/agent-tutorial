package com.acme.training.wms.inbound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

interface InboundReceiptRepository extends JpaRepository<InboundReceipt, Long> {
    Optional<InboundReceipt> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InboundReceipt> findLockedByIdempotencyKey(String idempotencyKey);
}
