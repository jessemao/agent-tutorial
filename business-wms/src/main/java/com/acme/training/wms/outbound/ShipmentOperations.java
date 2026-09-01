package com.acme.training.wms.outbound;

public interface ShipmentOperations {

    ShipmentView create(CreateShipment command);

    ShipmentView reserve(Long shipmentId, String idempotencyKey);

    ShipmentView ship(Long shipmentId, String idempotencyKey);

    ShipmentView cancel(Long shipmentId, String idempotencyKey, String reason);

    ShipmentView get(Long shipmentId);
}
