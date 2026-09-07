package com.acme.training.wms.inbound;

public interface InboundOperations {

    InboundView create(CreateInbound command);

    InboundView receive(Long inboundId, String idempotencyKey, long quantity);

    InboundView get(Long inboundId);
}
