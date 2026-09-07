package com.acme.training.wms.web;

import com.acme.training.platform.web.ApiResponse;
import com.acme.training.wms.inbound.CreateInbound;
import com.acme.training.wms.inbound.InboundOperations;
import com.acme.training.wms.inbound.InboundView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/wms/inbounds")
public class InboundController {

    private final InboundOperations inboundOperations;

    public InboundController(InboundOperations inboundOperations) {
        this.inboundOperations = inboundOperations;
    }

    @PostMapping
    public ApiResponse<InboundView> create(@Valid @RequestBody CreateInboundRequest request) {
        return ApiResponse.success(inboundOperations.create(new CreateInbound(request.getOrderNo(), request.getSkuId(),
                request.getWarehouseId(), request.getLocationId(), request.getPlannedQuantity())));
    }

    @PostMapping("/{id}/receive")
    public ApiResponse<InboundView> receive(@PathVariable Long id,
                                            @Valid @RequestBody ReceiveInboundRequest request) {
        return ApiResponse.success(inboundOperations.receive(id, request.getIdempotencyKey(), request.getQuantity()));
    }

    @GetMapping("/{id}")
    public ApiResponse<InboundView> get(@PathVariable Long id) {
        return ApiResponse.success(inboundOperations.get(id));
    }
}
