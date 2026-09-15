package com.acme.training.wms.masterdata;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterDataService implements MasterDataOperations {
    private final WarehouseRepository warehouses;
    private final SkuRepository skus;
    private final StorageLocationRepository locations;

    public MasterDataService(WarehouseRepository warehouses, SkuRepository skus,
                             StorageLocationRepository locations) {
        this.warehouses = warehouses; this.skus = skus; this.locations = locations;
    }

    public List<MasterDataOption> listWarehouses() {
        return warehouses.findAllByOrderByCodeAsc().stream()
                .map(v -> new MasterDataOption(v.getId(), v.getCode(), v.getName())).collect(Collectors.toList());
    }
    public List<MasterDataOption> listSkus() {
        return skus.findByEnabledTrueOrderByCodeAsc().stream()
                .map(v -> new MasterDataOption(v.getId(), v.getCode(), v.getName())).collect(Collectors.toList());
    }
    public List<MasterDataOption> listLocations(Long warehouseId) {
        return locations.findByWarehouseIdAndEnabledTrueOrderByCodeAsc(warehouseId).stream()
                .map(v -> new MasterDataOption(v.getId(), v.getCode(), v.getCode())).collect(Collectors.toList());
    }
}
