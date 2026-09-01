package com.acme.training.wms.outbound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

interface ShipmentOrderRepository extends JpaRepository<ShipmentOrder, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ShipmentOrder> findLockedById(Long id);
}
