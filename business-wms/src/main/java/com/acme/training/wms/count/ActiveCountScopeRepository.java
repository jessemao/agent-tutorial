package com.acme.training.wms.count;
import org.springframework.data.jpa.repository.JpaRepository;
interface ActiveCountScopeRepository extends JpaRepository<ActiveCountScope,Long>{
    boolean existsBySkuIdAndWarehouseIdAndLocationId(Long skuId,Long warehouseId,Long locationId);
    java.util.Optional<ActiveCountScope> findBySkuIdAndWarehouseIdAndLocationId(Long skuId,Long warehouseId,Long locationId);
    void deleteByCountId(Long countId);
}
