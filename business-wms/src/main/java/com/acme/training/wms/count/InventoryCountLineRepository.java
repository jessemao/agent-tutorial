package com.acme.training.wms.count;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
interface InventoryCountLineRepository extends JpaRepository<InventoryCountLine,Long>{ List<InventoryCountLine> findByCountIdOrderByIdAsc(Long countId); void deleteByCountId(Long countId); }
