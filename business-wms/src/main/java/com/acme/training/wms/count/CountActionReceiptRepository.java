package com.acme.training.wms.count;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
interface CountActionReceiptRepository extends JpaRepository<CountActionReceipt,Long>{
    Optional<CountActionReceipt> findByActionTypeAndIdempotencyKey(String action,String key);
}
