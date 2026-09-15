package com.acme.training.wms.count;
public final class CountDimension {
    private final Long skuId; private final Long locationId;
    public CountDimension(Long skuId,Long locationId){this.skuId=skuId;this.locationId=locationId;}
    public Long getSkuId(){return skuId;} public Long getLocationId(){return locationId;}
}
