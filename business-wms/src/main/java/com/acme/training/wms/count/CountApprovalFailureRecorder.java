package com.acme.training.wms.count;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
class CountApprovalFailureRecorder {
    private final InventoryCountRepository counts;

    CountApprovalFailureRecorder(InventoryCountRepository counts) {
        this.counts = counts;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long countId, String failureCode) {
        counts.findById(countId).ifPresent(count -> count.recordApprovalFailure(failureCode));
    }
}
