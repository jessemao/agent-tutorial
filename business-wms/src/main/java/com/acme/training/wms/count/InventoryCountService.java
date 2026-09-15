package com.acme.training.wms.count;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Public application entry point for inventory-count use cases. */
@Service
public class InventoryCountService implements InventoryCountOperations {
    private final CountDraftWorkflow drafts;
    private final CountReviewWorkflow reviews;
    private final CountQueryService queries;

    public InventoryCountService(CountDraftWorkflow drafts, CountReviewWorkflow reviews,
                                 CountQueryService queries) {
        this.drafts = drafts;
        this.reviews = reviews;
        this.queries = queries;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryCountView create(CreateInventoryCount command) {
        return drafts.create(command);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryCountView get(Long id) {
        return queries.get(id);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryCountPage list(String countNo, Long warehouseId,
                                   InventoryCountStatus status, int page, int size) {
        return queries.list(countNo, warehouseId, status, page, size);
    }

    @Override
    @Transactional
    public InventoryCountView saveDraft(Long id, SaveInventoryCountDraft command) {
        return drafts.saveDraft(id, command);
    }

    @Override
    @Transactional
    public InventoryCountView submit(Long id, SubmitInventoryCount command) {
        return drafts.submit(id, command);
    }

    @Override
    @Transactional
    public InventoryCountView transition(Long id, TransitionInventoryCount command) {
        return reviews.transition(id, command);
    }

    @Override
    @Transactional(noRollbackFor = CountConflictException.class)
    public InventoryCountView approve(Long id, ApproveInventoryCount command) {
        return reviews.approve(id, command);
    }
}
