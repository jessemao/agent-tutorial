package com.acme.training.wms.count;

import com.acme.training.platform.error.PlatformException;
import com.acme.training.wms.masterdata.MasterDataOption;
import com.acme.training.wms.masterdata.Sku;
import com.acme.training.wms.masterdata.StorageLocation;
import com.acme.training.wms.masterdata.Warehouse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
final class CountQueryService {
    private static final int MAX_PAGE_SIZE = 100;

    private final InventoryCountRepository counts;
    private final InventoryCountLineRepository lines;
    private final CountCatalog catalog;

    CountQueryService(InventoryCountRepository counts, InventoryCountLineRepository lines,
                      CountCatalog catalog) {
        this.counts = counts;
        this.lines = lines;
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    InventoryCountView get(Long id) {
        return view(requiredCount(id));
    }

    Warehouse requiredWarehouse(Long warehouseId) {
        return catalog.requiredWarehouse(warehouseId);
    }

    Sku requiredEnabledSku(Long skuId) {
        return catalog.requiredEnabledSku(skuId);
    }

    Sku requiredSku(Long skuId) {
        return catalog.requiredSku(skuId);
    }

    StorageLocation lockEnabledLocation(Long locationId, Long warehouseId) {
        return catalog.lockEnabledLocation(locationId, warehouseId);
    }

    StorageLocation requiredLocation(Long locationId) {
        return catalog.requiredLocation(locationId);
    }

    @Transactional(readOnly = true)
    InventoryCountPage list(String countNo, Long warehouseId, InventoryCountStatus status,
                            int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(MAX_PAGE_SIZE, size));
        Specification<InventoryCount> filters = filters(countNo, warehouseId, status);
        PageRequest paging = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Page<InventoryCount> result = counts.findAll(filters, paging);
        List<InventoryCountView> content = result.getContent().stream()
                .map(this::view)
                .collect(Collectors.toList());
        return new InventoryCountPage(content, result.getTotalElements(), safePage, safeSize);
    }

    InventoryCount requiredCount(Long id) {
        return counts.findById(id).orElseThrow(() -> new PlatformException(
                "WMS_COUNT_NOT_FOUND", "盘点单不存在"));
    }

    InventoryCountView view(InventoryCount count) {
        Warehouse warehouse = catalog.requiredWarehouse(count.getWarehouseId());
        List<InventoryCountLineView> itemLines = lines.findByCountIdOrderByIdAsc(count.getId())
                .stream()
                .map(this::lineView)
                .collect(Collectors.toList());
        String correctionNumber = count.getCorrectionOfCountId() == null
                ? null
                : counts.findById(count.getCorrectionOfCountId())
                        .map(InventoryCount::getCountNo)
                        .orElse(null);
        return new InventoryCountView(
                count.getId(), count.getCountNo(), count.getStatus(), count.getVersion(),
                option(warehouse), itemLines, count.getCreatedBy(), count.getCreatedAt(),
                count.getNote(), count.getSubmittedBy(), count.getSubmittedAt(),
                count.getRejectedBy(), count.getRejectedAt(), count.getRejectionReason(),
                count.getCancelledBy(), count.getCancelledAt(), count.getCancellationReason(),
                correctionNumber, count.getApprovedBy(), count.getApprovedAt(),
                count.getLastApprovalFailureCode(), count.getLastApprovalFailureAt());
    }

    private Specification<InventoryCount> filters(String countNo, Long warehouseId,
                                                   InventoryCountStatus status) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (countNo != null && !countNo.trim().isEmpty()) {
                predicates.add(builder.like(root.get("countNo"), "%" + countNo.trim() + "%"));
            }
            if (warehouseId != null) {
                predicates.add(builder.equal(root.get("warehouseId"), warehouseId));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private InventoryCountLineView lineView(InventoryCountLine line) {
        Sku sku = catalog.requiredSku(line.getSkuId());
        StorageLocation location = catalog.requiredLocation(line.getLocationId());
        return new InventoryCountLineView(
                option(sku), option(location), line.getCreationBookTotal(),
                line.getCountedTotal(), line.getSubmittedBookTotal(),
                line.getDifferenceReason(), line.getApprovalBookTotal(), line.getDifference(),
                line.getAvailableAfter(), line.getReservedAfter());
    }

    private MasterDataOption option(Warehouse warehouse) {
        return new MasterDataOption(warehouse.getId(), warehouse.getCode(), warehouse.getName());
    }

    private MasterDataOption option(Sku sku) {
        return new MasterDataOption(sku.getId(), sku.getCode(), sku.getName());
    }

    private MasterDataOption option(StorageLocation location) {
        return new MasterDataOption(
                location.getId(), location.getCode(), location.getCode());
    }
}
