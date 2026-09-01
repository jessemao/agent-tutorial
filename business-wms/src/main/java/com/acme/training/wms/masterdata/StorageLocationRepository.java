package com.acme.training.wms.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StorageLocation> findLockedById(Long id);
}
