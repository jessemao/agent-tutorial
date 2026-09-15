package com.acme.training.wms.count;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
final class CountPersistence {
    private final InventoryCountRepository counts;
    private final InventoryCountLineRepository lines;
    private final ActiveCountScopeRepository scopes;

    CountPersistence(InventoryCountRepository counts, InventoryCountLineRepository lines,
                     ActiveCountScopeRepository scopes) {
        this.counts = counts;
        this.lines = lines;
        this.scopes = scopes;
    }

    Optional<InventoryCount> findByIdempotencyKey(String idempotencyKey) {
        return counts.findByIdempotencyKey(idempotencyKey);
    }

    InventoryCount saveCount(InventoryCount count) {
        return counts.saveAndFlush(count);
    }

    List<InventoryCountLine> lines(Long countId) {
        return lines.findByCountIdOrderByIdAsc(countId);
    }

    void replaceLinesAndScopes(Long countId) {
        scopes.deleteByCountId(countId);
        lines.deleteByCountId(countId);
        scopes.flush();
        lines.flush();
    }

    InventoryCountLine saveLine(InventoryCountLine line) {
        return lines.save(line);
    }

    ActiveCountScope saveScope(ActiveCountScope scope) {
        return scopes.saveAndFlush(scope);
    }

    void releaseScopes(Long countId) {
        scopes.deleteByCountId(countId);
    }

    Optional<ActiveCountScope> findActiveScope(Long skuId, Long warehouseId,
                                                Long locationId) {
        return scopes.findBySkuIdAndWarehouseIdAndLocationId(skuId, warehouseId, locationId);
    }
}
