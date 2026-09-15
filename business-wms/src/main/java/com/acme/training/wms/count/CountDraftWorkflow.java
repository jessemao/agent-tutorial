package com.acme.training.wms.count;

import com.acme.training.platform.error.PlatformException;
import com.acme.training.wms.inventory.InventoryBalanceView;
import com.acme.training.wms.masterdata.Sku;
import com.acme.training.wms.masterdata.StorageLocation;
import com.acme.training.wms.masterdata.Warehouse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
class CountDraftWorkflow {
    private final CountPersistence persistence;
    private final CountQueryService queries;
    private final CountCommandSupport support;

    CountDraftWorkflow(CountPersistence persistence, CountQueryService queries,
                       CountCommandSupport support) {
        this.persistence = persistence;
        this.queries = queries;
        this.support = support;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    InventoryCountView create(CreateInventoryCount command) {
        validateShape(command);
        String fingerprint = fingerprint(command);
        Warehouse warehouse = queries.requiredWarehouse(command.getWarehouseId());
        Map<Long, StorageLocation> lockedLocations = lockLocations(command, warehouse);
        InventoryCountView replay = support.replay(CountAction.CREATE,
                command.getIdempotencyKey(), fingerprint);
        if (replay != null) {
            return replay;
        }
        Optional<InventoryCount> existing = persistence.findByIdempotencyKey(
                command.getIdempotencyKey());
        if (existing.isPresent()) {
            ensureSameRequest(existing.get(), fingerprint);
            return queries.view(existing.get());
        }

        validateCorrectionSource(command.getCorrectionOfCountId());

        List<ValidatedLine> validated = validateCreateLines(command, warehouse, lockedLocations);
        InventoryCount count = persistence.saveCount(new InventoryCount(
                warehouse.getId(), command.getIdempotencyKey(), fingerprint,
                support.requiredOperatorId(), command.getCorrectionOfCountId()));
        count.assignNumber();
        persistence.saveCount(count);
        saveCreatedLines(count, warehouse, validated);

        InventoryCountView result = queries.view(count);
        support.complete(CountAction.CREATE, command.getIdempotencyKey(), count.getId(),
                fingerprint, result, count.getCountNo());
        return result;
    }

    @Transactional
    InventoryCountView saveDraft(Long id, SaveInventoryCountDraft command) {
        InventoryCount count = requiredCount(id);
        String fingerprint = saveFingerprint(id, command);
        InventoryCountView replay = support.replay(CountAction.SAVE,
                command.getIdempotencyKey(), fingerprint);
        if (replay != null) {
            return replay;
        }
        requireDraft(count);
        requireVersion(count, command.getExpectedVersion());
        validateSave(command);

        Map<String, InventoryCountLine> previousLines = persistence.lines(id).stream()
                .collect(Collectors.toMap(line -> dimension(line.getSkuId(), line.getLocationId()),
                        line -> line));
        persistence.replaceLinesAndScopes(id);
        Set<String> dimensions = new HashSet<>();
        for (SaveCountLine requested : command.getLines()) {
            saveDraftLine(count, requested, previousLines, dimensions);
        }
        count.saveDraft(command.getNote());
        persistence.saveCount(count);

        InventoryCountView result = queries.view(count);
        support.complete(CountAction.SAVE, command.getIdempotencyKey(), id, fingerprint, result,
                count.getCountNo());
        return result;
    }

    @Transactional
    InventoryCountView submit(Long id, SubmitInventoryCount command) {
        InventoryCount count = requiredCount(id);
        String fingerprint = submitFingerprint(id, command);
        InventoryCountView replay = support.replay(CountAction.SUBMIT,
                command.getIdempotencyKey(), fingerprint);
        if (replay != null) {
            return replay;
        }
        requireDraft(count);
        requireVersion(count, command.getExpectedVersion());
        List<InventoryCountLine> current = persistence.lines(id);
        List<Integer> missingIndexes = missingIndexes(current);
        if (!missingIndexes.isEmpty()) {
            throw new CountConflictException("WMS_COUNT_INCOMPLETE", "仍有明细未填写实盘数",
                    queries.view(count), missingIndexes);
        }
        submitLines(count, current);
        count.submit(support.requiredOperatorId(), command.getNote());
        persistence.saveCount(count);

        InventoryCountView result = queries.view(count);
        support.complete(CountAction.SUBMIT, command.getIdempotencyKey(), id, fingerprint, result,
                count.getCountNo());
        return result;
    }

    private void ensureSameRequest(InventoryCount existing, String fingerprint) {
        if (!existing.getRequestFingerprint().equals(fingerprint)) {
            throw new PlatformException("WMS_IDEMPOTENCY_CONFLICT", "幂等键已用于不同请求");
        }
    }

    private void validateCorrectionSource(Long correctionOfCountId) {
        if (correctionOfCountId == null) {
            return;
        }
        InventoryCount original = requiredCount(correctionOfCountId);
        if (original.getStatus() != InventoryCountStatus.APPROVED) {
            throw new PlatformException("WMS_COUNT_INVALID_CORRECTION_SOURCE",
                    "纠错来源必须是已批准盘点单");
        }
    }

    private Map<Long, StorageLocation> lockLocations(CreateInventoryCount command,
                                                      Warehouse warehouse) {
        Map<Long, StorageLocation> locked = new HashMap<>();
        command.getLines().stream().map(CountDimension::getLocationId).distinct().sorted()
                .forEach(locationId -> locked.put(locationId,
                        queries.lockEnabledLocation(locationId, warehouse.getId())));
        return locked;
    }

    private List<ValidatedLine> validateCreateLines(CreateInventoryCount command,
                                                    Warehouse warehouse,
                                                    Map<Long, StorageLocation> lockedLocations) {
        List<ValidatedLine> validated = new ArrayList<>();
        for (CountDimension dimension : command.getLines()) {
            Sku sku = queries.requiredEnabledSku(dimension.getSkuId());
            StorageLocation location = lockedLocations.get(dimension.getLocationId());
            Optional<ActiveCountScope> conflict = persistence.findActiveScope(
                    sku.getId(), warehouse.getId(), location.getId());
            if (conflict.isPresent()) {
                throw new CountConflictException("WMS_COUNT_SCOPE_CONFLICT",
                        "该库存维度已有进行中的盘点单",
                        queries.view(requiredCount(conflict.get().getCountId())),
                        Collections.singletonList(validated.size()));
            }
            validated.add(new ValidatedLine(sku, location,
                    totalQuantity(sku.getId(), warehouse.getId(), location.getId())));
        }
        return validated;
    }

    private void saveCreatedLines(InventoryCount count, Warehouse warehouse,
                                  List<ValidatedLine> validated) {
        for (ValidatedLine line : validated) {
            persistence.saveLine(new InventoryCountLine(
                    count.getId(), line.sku.getId(), line.location.getId(), line.total));
            persistence.saveScope(new ActiveCountScope(
                    count.getId(), line.sku.getId(), warehouse.getId(), line.location.getId()));
        }
    }

    private void saveDraftLine(InventoryCount count, SaveCountLine requested,
                               Map<String, InventoryCountLine> previousLines,
                               Set<String> dimensions) {
        String key = dimension(requested.getSkuId(), requested.getLocationId());
        if (!dimensions.add(key)) {
            throw new InvalidCountRequestException("盘点维度不能重复");
        }
        ValidatedLine validated = validateDimension(count, requested.getSkuId(),
                requested.getLocationId());
        InventoryCountLine previous = previousLines.get(key);
        long creationTotal = previous == null
                ? validated.total : previous.getCreationBookTotal();
        InventoryCountLine line = new InventoryCountLine(
                count.getId(), requested.getSkuId(), requested.getLocationId(), creationTotal);
        line.record(requested.getCountedTotal(), requested.getDifferenceReason());
        persistence.saveLine(line);
        persistence.saveScope(new ActiveCountScope(
                count.getId(), requested.getSkuId(), count.getWarehouseId(),
                requested.getLocationId()));
    }

    private List<Integer> missingIndexes(List<InventoryCountLine> lines) {
        List<Integer> missing = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).getCountedTotal() == null) {
                missing.add(index);
            }
        }
        return missing;
    }

    private void submitLines(InventoryCount count, List<InventoryCountLine> lines) {
        for (InventoryCountLine line : lines) {
            line.submit(totalQuantity(line.getSkuId(), count.getWarehouseId(),
                    line.getLocationId()));
        }
    }

    private long totalQuantity(Long skuId, Long warehouseId, Long locationId) {
        return support.totalQuantity(skuId, warehouseId, locationId);
    }

    private ValidatedLine validateDimension(InventoryCount count, Long skuId, Long locationId) {
        Sku sku = queries.requiredEnabledSku(skuId);
        StorageLocation location = queries.lockEnabledLocation(locationId, count.getWarehouseId());
        Optional<ActiveCountScope> conflict = persistence.findActiveScope(
                skuId, count.getWarehouseId(), locationId);
        if (conflict.isPresent() && !count.getId().equals(conflict.get().getCountId())) {
            throw new CountConflictException("WMS_COUNT_SCOPE_CONFLICT",
                    "该库存维度已有进行中的盘点单",
                    queries.view(requiredCount(conflict.get().getCountId())),
                    Collections.singletonList(0));
        }
        return new ValidatedLine(sku, location,
                totalQuantity(skuId, count.getWarehouseId(), locationId));
    }

    private InventoryCount requiredCount(Long id) {
        return queries.requiredCount(id);
    }

    private void requireDraft(InventoryCount count) {
        if (count.getStatus() != InventoryCountStatus.DRAFT) {
            throw new CountConflictException("WMS_COUNT_INVALID_STATE", "当前状态不允许修改草稿",
                    queries.view(count), Collections.emptyList());
        }
    }

    private void requireVersion(InventoryCount count, long expectedVersion) {
        if (count.getVersion() != expectedVersion) {
            throw new CountConflictException("WMS_COUNT_VERSION_CONFLICT", "盘点版本已变化",
                    queries.view(count), Collections.emptyList());
        }
    }

    private void validateSave(SaveInventoryCountDraft command) {
        if (command == null || command.getIdempotencyKey() == null
                || command.getIdempotencyKey().trim().isEmpty()
                || command.getLines() == null || command.getLines().isEmpty()) {
            throw new InvalidCountRequestException("幂等键和盘点明细不能为空");
        }
        for (SaveCountLine line : command.getLines()) {
            if (line == null || line.getSkuId() == null || line.getLocationId() == null
                    || line.getCountedTotal() != null && line.getCountedTotal() < 0) {
                throw new InvalidCountRequestException(
                        "盘点维度不能为空且实盘数必须为非负整数");
            }
        }
    }

    private void validateShape(CreateInventoryCount command) {
        if (command == null || command.getIdempotencyKey() == null
                || command.getIdempotencyKey().trim().isEmpty()
                || command.getWarehouseId() == null || command.getLines() == null
                || command.getLines().isEmpty()) {
            throw new InvalidCountRequestException("幂等键、仓库和盘点明细不能为空");
        }
        Set<String> unique = new HashSet<>();
        for (CountDimension line : command.getLines()) {
            if (line == null || line.getSkuId() == null || line.getLocationId() == null
                    || !unique.add(dimension(line.getSkuId(), line.getLocationId()))) {
                throw new InvalidCountRequestException("盘点维度不能为空或重复");
            }
        }
    }

    private String fingerprint(CreateInventoryCount command) {
        return command.getWarehouseId() + "|" + command.getCorrectionOfCountId() + "|"
                + command.getLines().stream()
                        .map(line -> dimension(line.getSkuId(), line.getLocationId()))
                        .sorted().collect(Collectors.joining(","));
    }

    private String saveFingerprint(Long id, SaveInventoryCountDraft command) {
        return id + "|" + command.getExpectedVersion() + "|" + command.getNote() + "|"
                + command.getLines().stream()
                        .map(line -> line.getSkuId() + ":" + line.getLocationId() + ":"
                                + line.getCountedTotal() + ":" + line.getDifferenceReason())
                        .sorted().collect(Collectors.joining(","));
    }

    private String submitFingerprint(Long id, SubmitInventoryCount command) {
        return id + "|" + command.getExpectedVersion() + "|" + command.getNote();
    }

    private String dimension(Long skuId, Long locationId) {
        return skuId + ":" + locationId;
    }

    private static final class ValidatedLine {
        private final Sku sku;
        private final StorageLocation location;
        private final long total;

        private ValidatedLine(Sku sku, StorageLocation location, long total) {
            this.sku = sku;
            this.location = location;
            this.total = total;
        }
    }
}
