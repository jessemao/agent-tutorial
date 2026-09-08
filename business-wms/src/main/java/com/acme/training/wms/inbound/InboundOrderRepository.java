package com.acme.training.wms.inbound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InboundOrder> findLockedById(Long id);

    Optional<InboundOrder> findByReceiptIdempotencyKey(String receiptIdempotencyKey);

    List<InboundOrder> findByStatusAndReceiptIdempotencyKeyIsNotNull(InboundStatus status);
}
