package com.acme.training.wms.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkuRepository extends JpaRepository<Sku, Long> {
    List<Sku> findByEnabledTrueOrderByCodeAsc();
}
