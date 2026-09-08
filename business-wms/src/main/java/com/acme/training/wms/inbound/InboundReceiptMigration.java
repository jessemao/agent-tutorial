package com.acme.training.wms.inbound;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class InboundReceiptMigration implements ApplicationRunner {

    private final InboundOrderRepository inboundRepository;
    private final InboundReceiptRepository receiptRepository;

    InboundReceiptMigration(InboundOrderRepository inboundRepository,
                            InboundReceiptRepository receiptRepository) {
        this.inboundRepository = inboundRepository;
        this.receiptRepository = receiptRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        for (InboundOrder order : inboundRepository.findByStatusAndReceiptIdempotencyKeyIsNotNull(
                InboundStatus.RECEIVED)) {
            String key = order.getReceiptIdempotencyKey();
            if (!receiptRepository.findByIdempotencyKey(key).isPresent()) {
                receiptRepository.save(new InboundReceipt(order, key, order.getReceivedQuantity()));
            }
        }
    }
}
