package com.acme.training.wms.count;

import com.acme.training.platform.error.PlatformException;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
final class CountActionPersistence {
    private final CountActionReceiptRepository receipts;
    private final CountApprovalPreviewRepository previews;
    private final CountApprovalFailureRecorder approvalFailures;

    CountActionPersistence(CountActionReceiptRepository receipts,
                           CountApprovalPreviewRepository previews,
                           CountApprovalFailureRecorder approvalFailures) {
        this.receipts = receipts;
        this.previews = previews;
        this.approvalFailures = approvalFailures;
    }

    InventoryCountView replay(CountAction action, String key, String fingerprint) {
        Optional<CountActionReceipt> found = receipts.findByActionTypeAndIdempotencyKey(
                action.name(), key);
        if (!found.isPresent()) {
            return null;
        }
        if (!found.get().getFingerprint().equals(fingerprint)) {
            throw new PlatformException(
                    "WMS_IDEMPOTENCY_CONFLICT", "幂等键已用于不同请求");
        }
        return found.get().getResult();
    }

    void saveReceipt(CountAction action, String key, Long countId, String fingerprint,
                     InventoryCountView result) {
        receipts.save(new CountActionReceipt(action, key, countId, fingerprint, result));
    }

    String confirmedFacts(String token, Long countId, long countVersion) {
        return Optional.ofNullable(token)
                .flatMap(previews::findByToken)
                .filter(preview -> preview.matches(countId, countVersion))
                .map(CountApprovalPreview::getFactsFingerprint)
                .orElse(null);
    }

    void savePreview(CountApprovalPreview preview) {
        previews.save(preview);
    }

    void recordApprovalFailure(Long countId, String failureCode) {
        approvalFailures.record(countId, failureCode);
    }
}
