package com.acme.training.wms.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {

    Optional<InventoryBalance> findBySkuIdAndWarehouseIdAndLocationId(
            Long skuId, Long warehouseId, Long locationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryBalance> findLockedBySkuIdAndWarehouseIdAndLocationId(
            Long skuId, Long warehouseId, Long locationId);
}
