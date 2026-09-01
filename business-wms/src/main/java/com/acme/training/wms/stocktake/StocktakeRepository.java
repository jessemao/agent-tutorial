package com.acme.training.wms.stocktake;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

interface StocktakeRepository extends JpaRepository<Stocktake, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Stocktake> findLockedById(Long id);
}
