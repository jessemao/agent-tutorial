package com.acme.training.wms.inbound;

import com.acme.training.platform.audit.AuditRecorder;
import com.acme.training.platform.error.PlatformException;
import com.acme.training.platform.idempotency.IdempotencyDecision;
import com.acme.training.platform.idempotency.IdempotencyGuard;
import com.acme.training.wms.inventory.InventoryCommand;
import com.acme.training.wms.inventory.InventoryOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InboundService implements InboundOperations {

    private final InboundOrderRepository inboundRepository;
    private final InventoryOperations inventoryOperations;
    private final AuditRecorder auditRecorder;
    private final IdempotencyGuard idempotencyGuard;

    InboundService(InboundOrderRepository inboundRepository,
                   InventoryOperations inventoryOperations,
                   AuditRecorder auditRecorder,
                   IdempotencyGuard idempotencyGuard) {
        this.inboundRepository = inboundRepository;
        this.inventoryOperations = inventoryOperations;
        this.auditRecorder = auditRecorder;
        this.idempotencyGuard = idempotencyGuard;
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
        InboundOrder order = locked(inboundId);
        if (order.getStatus() == InboundStatus.RECEIVED
                && idempotencyGuard.decide(true, order.matchesReceipt(idempotencyKey, quantity),
                "WMS_IDEMPOTENCY_CONFLICT", "inbound order was already received by another request")
                == IdempotencyDecision.SAFE_REPLAY) {
            return new InboundView(order);
        }
        order.receiveAll(idempotencyKey, quantity);
        inventoryOperations.receive(new InventoryCommand(idempotencyKey, order.getOrderNo(), order.getSkuId(),
                order.getWarehouseId(), order.getLocationId(), quantity));
        auditRecorder.record("RECEIVE", "INBOUND", order.getOrderNo());
        return new InboundView(order);
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
