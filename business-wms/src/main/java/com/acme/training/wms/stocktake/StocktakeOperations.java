package com.acme.training.wms.stocktake;

public interface StocktakeOperations {

    StocktakeView create(CreateStocktake command);

    StocktakeView approve(Long stocktakeId, String idempotencyKey);

    StocktakeView get(Long stocktakeId);
}
