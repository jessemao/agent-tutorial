package com.acme.training.wms.inbound;

import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.error.PlatformException;
import com.acme.training.wms.PersistenceConstraints;
import com.acme.training.wms.inventory.InventoryCommand;
import com.acme.training.wms.inventory.InventoryOperations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InboundService implements InboundOperations {

    private final InboundOrderRepository inboundRepository;
    private final InboundReceiptRepository receiptRepository;
    private final InventoryOperations inventoryOperations;
    private final AuditRecorder auditRecorder;

    InboundService(InboundOrderRepository inboundRepository,
                   InboundReceiptRepository receiptRepository,
                   InventoryOperations inventoryOperations,
                   AuditRecorder auditRecorder) {
        this.inboundRepository = inboundRepository;
        this.receiptRepository = receiptRepository;
        this.inventoryOperations = inventoryOperations;
        this.auditRecorder = auditRecorder;
    }

    @Override
    @Transactional
    public InboundView create(CreateInbound command) {
        InboundOrder order = inboundRepository.save(new InboundOrder(command));
        auditRecorder.record("CREATE", "INBOUND", order.getOrderNo());
        return new InboundView(order);
    }

    @Override
    @Transactional
    public InboundView receive(Long inboundId, String idempotencyKey, long quantity) {
        InboundReceipt receipt = receiptRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (receipt != null) {
            return replay(receipt, inboundId, quantity);
        }
        InboundOrder legacyOrder = inboundRepository.findByReceiptIdempotencyKey(idempotencyKey).orElse(null);
        if (legacyOrder != null) {
            if (!legacyOrder.getId().equals(inboundId) || !legacyOrder.matchesLegacyReceipt(idempotencyKey, quantity)) {
                throw idempotencyConflict();
            }
            recordReceipt(new InboundReceipt(legacyOrder, idempotencyKey, quantity));
            return new InboundView(legacyOrder);
        }
        InboundOrder order = locked(inboundId);
        receipt = receiptRepository.findLockedByIdempotencyKey(idempotencyKey).orElse(null);
        if (receipt != null) {
            return replay(receipt, inboundId, quantity);
        }
        order.receive(quantity);
        inventoryOperations.receive(new InventoryCommand(idempotencyKey, order.getOrderNo(), order.getSkuId(),
                order.getWarehouseId(), order.getLocationId(), quantity));
        auditRecorder.record("RECEIVE", "INBOUND", order.getOrderNo());
        recordReceipt(new InboundReceipt(order, idempotencyKey, quantity));
        return new InboundView(order);
    }

    private InboundView replay(InboundReceipt receipt, Long inboundId, long quantity) {
        if (!receipt.matches(inboundId, quantity)) {
            throw idempotencyConflict();
        }
        InboundOrder original = inboundRepository.findById(receipt.getInboundId())
                .orElseThrow(() -> new PlatformException("WMS_INBOUND_NOT_FOUND", "inbound order does not exist"));
        return new InboundView(original, receipt.getReceivedQuantity(), receipt.getStatus());
    }

    private void recordReceipt(InboundReceipt receipt) {
        try {
            receiptRepository.saveAndFlush(receipt);
        } catch (DataIntegrityViolationException exception) {
            if (PersistenceConstraints.hasName(exception, "uk_inbound_receipt_key")) {
                PlatformException conflict = idempotencyConflict();
                conflict.initCause(exception);
                throw conflict;
            }
            throw exception;
        }
    }

    private PlatformException idempotencyConflict() {
        return new PlatformException("WMS_IDEMPOTENCY_CONFLICT", "idempotency key belongs to another receipt");
    }

    @Override
    @Transactional(readOnly = true)
    public InboundView get(Long inboundId) {
        return new InboundView(inboundRepository.findById(inboundId)
                .orElseThrow(() -> new PlatformException("WMS_INBOUND_NOT_FOUND", "inbound order does not exist")));
    }

    private InboundOrder locked(Long inboundId) {
        return inboundRepository.findLockedById(inboundId)
                .orElseThrow(() -> new PlatformException("WMS_INBOUND_NOT_FOUND", "inbound order does not exist"));
    }
}
