package com.acme.training.wms.count;

import com.acme.training.wms.masterdata.SkuRepository;
import com.acme.training.wms.masterdata.Sku;
import com.acme.training.wms.masterdata.StorageLocation;
import com.acme.training.wms.masterdata.StorageLocationRepository;
import com.acme.training.wms.masterdata.Warehouse;
import com.acme.training.wms.masterdata.WarehouseRepository;
import com.acme.training.platform.error.PlatformException;
import org.springframework.stereotype.Component;

@Component
final class CountCatalog {
    private final WarehouseRepository warehouses;
    private final SkuRepository skus;
    private final StorageLocationRepository locations;

    CountCatalog(WarehouseRepository warehouses, SkuRepository skus,
                 StorageLocationRepository locations) {
        this.warehouses = warehouses;
        this.skus = skus;
        this.locations = locations;
    }

    Warehouse requiredWarehouse(Long warehouseId) {
        return warehouses.findById(warehouseId).orElseThrow(() -> new PlatformException(
                "WMS_WAREHOUSE_NOT_FOUND", "仓库不存在"));
    }

    Sku requiredEnabledSku(Long skuId) {
        return skus.findById(skuId)
                .filter(Sku::isEnabled)
                .orElseThrow(() -> new PlatformException(
                        "WMS_INVALID_SKU", "SKU 不存在或未启用"));
    }

    Sku requiredSku(Long skuId) {
        return skus.findById(skuId).orElseThrow(() -> new PlatformException(
                "WMS_INVALID_SKU", "SKU 不存在"));
    }

    StorageLocation lockEnabledLocation(Long locationId, Long warehouseId) {
        return locations.findLockedById(locationId)
                .filter(StorageLocation::isEnabled)
                .filter(location -> warehouseId.equals(location.getWarehouseId()))
                .orElseThrow(() -> new PlatformException(
                        "WMS_INVALID_LOCATION", "库位不存在、未启用或不属于所选仓库"));
    }

    StorageLocation requiredLocation(Long locationId) {
        return locations.findById(locationId).orElseThrow(() -> new PlatformException(
                "WMS_INVALID_LOCATION", "库位不存在"));
    }
}
