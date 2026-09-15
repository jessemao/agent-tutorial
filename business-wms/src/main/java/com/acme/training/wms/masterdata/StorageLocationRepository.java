package com.acme.training.wms.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select location from StorageLocation location where location.id = :id")
    Optional<StorageLocation> findLockedById(@Param("id") Long id);

    List<StorageLocation> findByWarehouseIdAndEnabledTrueOrderByCodeAsc(Long warehouseId);
}
