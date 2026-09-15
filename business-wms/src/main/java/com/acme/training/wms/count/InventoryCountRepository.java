package com.acme.training.wms.count;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
interface InventoryCountRepository extends JpaRepository<InventoryCount,Long>, JpaSpecificationExecutor<InventoryCount>{
    Optional<InventoryCount> findByCountNo(String countNo);
    Optional<InventoryCount> findByIdempotencyKey(String key);
}
