package com.acme.training.wms.count;

import com.acme.training.platform.error.PlatformException;
import com.acme.training.wms.inventory.CountReconciliationChangedException;
import com.acme.training.wms.inventory.CountReconciliationCommand;
import com.acme.training.wms.inventory.CountReconciliationLine;
import com.acme.training.wms.inventory.CountReconciliationResult;
import com.acme.training.wms.inventory.InventoryBalanceView;
import com.acme.training.wms.masterdata.MasterDataOption;
import com.acme.training.wms.masterdata.Sku;
import com.acme.training.wms.masterdata.StorageLocation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CountReviewWorkflow {
    private final CountPersistence persistence;
    private final CountQueryService queries;
    private final CountCommandSupport support;

    CountReviewWorkflow(CountPersistence persistence, CountQueryService queries,
                        CountCommandSupport support) {
        this.persistence = persistence;
        this.queries = queries;
        this.support = support;
    }

    @Transactional
    InventoryCountView transition(Long id, TransitionInventoryCount command) {
        InventoryCount count = requiredCount(id);
        CountAction action = CountAction.parseTransition(command.getAction());
        String fingerprint = id + "|" + command.getExpectedVersion() + "|"
                + command.getReason();
        InventoryCountView replay = support.replay(action, command.getIdempotencyKey(), fingerprint);
        if (replay != null) {
            return replay;
        }
        requireVersion(count, command.getExpectedVersion());
        String actor = support.requiredOperatorId();
        applyTransition(count, action, actor, command.getReason());
        persistence.saveCount(count);
        InventoryCountView result = queries.view(count);
        support.complete(action, command.getIdempotencyKey(), id, fingerprint, result,
                count.getCountNo());
        return result;
    }

    @Transactional(noRollbackFor = CountConflictException.class)
    InventoryCountView approve(Long id, ApproveInventoryCount command) {
        validateApprovalCommand(command);
        InventoryCount count = requiredCount(id);
        String fingerprint = id + "|" + command.getExpectedVersion() + "|"
                + command.getNote() + "|" + command.getConfirmationToken();
        InventoryCountView replay = support.replay(CountAction.APPROVE,
                command.getIdempotencyKey(), fingerprint);
        if (replay != null) {
            return replay;
        }
        ensureApprovable(count, command.getExpectedVersion());
        String actor = support.requiredOperatorId();
        ensureIndependentReviewer(count, actor);

        List<InventoryCountLine> lines = persistence.lines(id);
        List<CountReconciliationLine> requested = lines.stream()
                .map(line -> new CountReconciliationLine(line.getSkuId(), line.getLocationId(),
                        line.getCountedTotal(), line.getSubmittedBookTotal()))
                .collect(Collectors.toList());
        List<CountReconciliationResult> adjusted = reconcile(count, requested, command, actor);
        applyApproval(count, lines, adjusted, actor, command.getNote());

        InventoryCountView result = queries.view(count);
        support.complete(CountAction.APPROVE, command.getIdempotencyKey(), id, fingerprint, result,
                count.getCountNo());
        return result;
    }

    private void applyTransition(InventoryCount count, CountAction action, String actor,
                                 String reason) {
        if (action == CountAction.REJECT) {
            ensureStatus(count, InventoryCountStatus.SUBMITTED);
            requireReason(reason);
            count.reject(actor, reason);
            return;
        }
        if (action == CountAction.REOPEN) {
            ensureStatus(count, InventoryCountStatus.REJECTED);
            count.reopen();
            return;
        }
        ensureOneOfStatuses(count, InventoryCountStatus.DRAFT, InventoryCountStatus.SUBMITTED);
        requireReason(reason);
        count.cancel(actor, reason);
        persistence.releaseScopes(count.getId());
    }

    private List<CountReconciliationResult> reconcile(InventoryCount count,
                                                       List<CountReconciliationLine> requested,
                                                       ApproveInventoryCount command,
                                                       String actor) {
        String facts = support.confirmedFacts(command.getConfirmationToken(), count.getId(),
                count.getVersion());
        try {
            return support.reconcile(new CountReconciliationCommand(
                    count.getCountNo(), count.getWarehouseId(), requested,
                    count.getId() + "|" + count.getVersion(), facts));
        } catch (CountReconciliationChangedException changed) {
            throw differenceChanged(count, command, actor, changed);
        } catch (PlatformException failure) {
            try {
                support.recordApprovalFailure(count.getId(), failure.getCode());
            } catch (RuntimeException recordingFailure) {
                failure.addSuppressed(recordingFailure);
            }
            throw failure;
        }
    }

    private CountConflictException differenceChanged(InventoryCount count,
                                                      ApproveInventoryCount command,
                                                      String actor,
                                                      CountReconciliationChangedException changed) {
        String code = command.getConfirmationToken() == null
                || command.getConfirmationToken().trim().isEmpty()
                ? "WMS_COUNT_DIFFERENCE_CHANGED" : "WMS_COUNT_CONFIRMATION_STALE";
        List<CountApprovalLineView> preview = changed.getResults().stream()
                .map(result -> approvalLine(result.getSkuId(), result.getLocationId(),
                        result.getBookTotal(), result.getDifference()))
                .collect(Collectors.toList());
        String token = UUID.randomUUID().toString();
        support.savePreview(new CountApprovalPreview(
                token, count.getId(), count.getVersion(), changed.getFactsFingerprint(), actor));
        support.recordApprovalConflict(count.getCountNo());
        return new CountConflictException(code, "库存已变化，请确认最新盘点差异", queries.view(count),
                Collections.emptyList(), token, preview);
    }

    private CountApprovalLineView approvalLine(Long skuId, Long locationId, long bookTotal,
                                               long difference) {
        Sku sku = queries.requiredSku(skuId);
        StorageLocation location = queries.requiredLocation(locationId);
        return new CountApprovalLineView(
                new MasterDataOption(sku.getId(), sku.getCode(), sku.getName()),
                new MasterDataOption(location.getId(), location.getCode(), location.getCode()),
                bookTotal, difference);
    }

    private void applyApproval(InventoryCount count, List<InventoryCountLine> lines,
                               List<CountReconciliationResult> adjusted, String actor,
                               String note) {
        Map<String, CountReconciliationResult> byDimension = adjusted.stream()
                .collect(Collectors.toMap(result -> dimension(result.getSkuId(), result.getLocationId()),
                        result -> result));
        for (InventoryCountLine line : lines) {
            CountReconciliationResult result = byDimension.get(
                    dimension(line.getSkuId(), line.getLocationId()));
            InventoryBalanceView balance = result.getBalance();
            line.approve(result.getBookTotal(), result.getDifference(),
                    balance.getAvailableQuantity(), balance.getReservedQuantity());
        }
        count.approve(actor, note);
        persistence.releaseScopes(count.getId());
        persistence.saveCount(count);
    }

    private void validateApprovalCommand(ApproveInventoryCount command) {
        if (command == null || command.getIdempotencyKey() == null
                || command.getIdempotencyKey().trim().isEmpty()) {
            throw new InvalidCountRequestException("幂等键不能为空");
        }
    }

    private void ensureApprovable(InventoryCount count, long expectedVersion) {
        if (count.getStatus() != InventoryCountStatus.SUBMITTED) {
            throw new CountConflictException("WMS_COUNT_INVALID_STATE", "当前状态不允许审核",
                    queries.view(count), Collections.emptyList());
        }
        requireVersion(count, expectedVersion);
    }

    private void ensureIndependentReviewer(InventoryCount count, String actor) {
        if (actor.equals(count.getCreatedBy()) || actor.equals(count.getSubmittedBy())) {
            throw new CountConflictException("WMS_COUNT_SELF_APPROVAL", "创建人或最后提交人不能审核",
                    queries.view(count), Collections.emptyList());
        }
    }

    private void requireVersion(InventoryCount count, long expectedVersion) {
        if (count.getVersion() != expectedVersion) {
            throw new CountConflictException("WMS_COUNT_VERSION_CONFLICT", "盘点版本已变化",
                    queries.view(count), Collections.emptyList());
        }
    }

    private void ensureStatus(InventoryCount count, InventoryCountStatus expected) {
        if (count.getStatus() != expected) {
            throw invalidStatus(count);
        }
    }

    private void ensureOneOfStatuses(InventoryCount count, InventoryCountStatus first,
                                     InventoryCountStatus second) {
        if (count.getStatus() != first && count.getStatus() != second) {
            throw invalidStatus(count);
        }
    }

    private CountConflictException invalidStatus(InventoryCount count) {
        return new CountConflictException("WMS_COUNT_INVALID_STATE", "当前状态不允许该动作",
                queries.view(count), Collections.emptyList());
    }

    private void requireReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidCountRequestException("原因不能为空");
        }
    }

    private InventoryCount requiredCount(Long id) {
        return queries.requiredCount(id);
    }

    private String dimension(Long skuId, Long locationId) {
        return skuId + ":" + locationId;
    }
}
