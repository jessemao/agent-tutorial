package com.acme.training.wms.web;

import com.acme.training.platform.web.ApiResponse;
import com.acme.training.wms.stocktake.CreateStocktake;
import com.acme.training.wms.stocktake.StocktakeOperations;
import com.acme.training.wms.stocktake.StocktakeView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/wms/stocktakes")
public class StocktakeController {

    private final StocktakeOperations stocktakeOperations;

    public StocktakeController(StocktakeOperations stocktakeOperations) {
        this.stocktakeOperations = stocktakeOperations;
    }

    @PostMapping
    public ApiResponse<StocktakeView> create(@Valid @RequestBody CreateStocktakeRequest request) {
        return ApiResponse.success(stocktakeOperations.create(new CreateStocktake(
                request.getCountNo(), request.getSkuId(), request.getWarehouseId(),
                request.getLocationId(), request.getCountedTotalQuantity())));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<StocktakeView> approve(@PathVariable Long id,
                                             @Valid @RequestBody ApproveStocktakeRequest request) {
        return ApiResponse.success(stocktakeOperations.approve(id, request.getIdempotencyKey()));
    }

    @GetMapping("/{id}")
    public ApiResponse<StocktakeView> get(@PathVariable Long id) {
        return ApiResponse.success(stocktakeOperations.get(id));
    }
}
