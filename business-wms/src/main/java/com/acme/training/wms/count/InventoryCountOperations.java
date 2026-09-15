package com.acme.training.wms.count;
public interface InventoryCountOperations {
    InventoryCountView create(CreateInventoryCount command);
    InventoryCountView get(Long id);
    InventoryCountPage list(String countNo,Long warehouseId,InventoryCountStatus status,int page,int size);
    InventoryCountView saveDraft(Long id, SaveInventoryCountDraft command);
    InventoryCountView submit(Long id, SubmitInventoryCount command);
    InventoryCountView transition(Long id, TransitionInventoryCount command);
    InventoryCountView approve(Long id, ApproveInventoryCount command);
}
