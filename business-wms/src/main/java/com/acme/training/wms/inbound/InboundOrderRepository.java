package com.acme.training.wms.inbound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InboundOrder> findLockedById(Long id);
}
