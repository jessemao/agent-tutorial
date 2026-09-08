package com.acme.training.wms.inbound;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class InboundReceiptMigrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InboundOrderRepository inboundRepository;

    @Autowired
    private InboundReceiptRepository receiptRepository;

    @Test
    void backfillsReceiptEvidenceForLegacyCompletedOrdersBeforeServingTraffic() {
        InboundOrder order = inboundRepository.save(new InboundOrder(
                new CreateInbound("IN-T02-LEGACY", 3301L, 1L, 1L, 10)));
        entityManager.getEntityManager().createNativeQuery(
                        "update wms_inbound_order set received_quantity = 10, status = 'RECEIVED', "
                                + "receipt_idempotency_key = 'legacy-receipt' where id = :id")
                .setParameter("id", order.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        new InboundReceiptMigration(inboundRepository, receiptRepository).run(null);

        InboundReceipt receipt = receiptRepository.findByIdempotencyKey("legacy-receipt").orElseThrow(AssertionError::new);
        assertEquals(order.getId(), receipt.getInboundId());
        assertEquals(10, receipt.getReceivedQuantity());
        assertEquals(InboundStatus.RECEIVED, receipt.getStatus());
    }
}
