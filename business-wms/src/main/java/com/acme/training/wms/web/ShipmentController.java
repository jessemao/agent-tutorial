package com.acme.training.wms.web;

import com.acme.training.platform.web.ApiResponse;
import com.acme.training.wms.outbound.CreateShipment;
import com.acme.training.wms.outbound.ShipmentOperations;
import com.acme.training.wms.outbound.ShipmentView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/wms/shipments")
public class ShipmentController {

    private final ShipmentOperations shipmentOperations;

    public ShipmentController(ShipmentOperations shipmentOperations) {
        this.shipmentOperations = shipmentOperations;
    }

    @PostMapping
    public ApiResponse<ShipmentView> create(@Valid @RequestBody CreateShipmentRequest request) {
        return ApiResponse.success(shipmentOperations.create(new CreateShipment(request.getOrderNo(),
                request.getSkuId(), request.getWarehouseId(), request.getLocationId(), request.getQuantity())));
    }

    @PostMapping("/{id}/reserve")
    public ApiResponse<ShipmentView> reserve(@PathVariable Long id,
                                             @Valid @RequestBody ShipmentActionRequest request) {
        return ApiResponse.success(shipmentOperations.reserve(id, request.getIdempotencyKey()));
    }

    @PostMapping("/{id}/ship")
    public ApiResponse<ShipmentView> ship(@PathVariable Long id,
                                          @Valid @RequestBody ShipmentActionRequest request) {
        return ApiResponse.success(shipmentOperations.ship(id, request.getIdempotencyKey()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ShipmentView> cancel(@PathVariable Long id,
                                            @Valid @RequestBody CancelShipmentRequest request) {
        return ApiResponse.success(shipmentOperations.cancel(id, request.getIdempotencyKey(), request.getReason()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShipmentView> get(@PathVariable Long id) {
        return ApiResponse.success(shipmentOperations.get(id));
    }
}
