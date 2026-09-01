package com.acme.training.config;

import com.acme.training.wms.masterdata.StorageLocation;
import com.acme.training.wms.masterdata.StorageLocationRepository;
import com.acme.training.wms.masterdata.Warehouse;
import com.acme.training.wms.masterdata.WarehouseArea;
import com.acme.training.wms.masterdata.WarehouseAreaRepository;
import com.acme.training.wms.masterdata.WarehouseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataConfiguration {

    @Bean
    public CommandLineRunner demoWarehouse(WarehouseRepository warehouseRepository,
                                           WarehouseAreaRepository areaRepository,
            StorageLocationRepository locationRepository) {
        return args -> {
            if (warehouseRepository.count() > 0) {
                return;
            }
            Warehouse warehouse = warehouseRepository.save(new Warehouse("WH-SH", "上海培训仓"));
            WarehouseArea area = areaRepository.save(new WarehouseArea(warehouse.getId(), "A01", "常温区"));
            locationRepository.save(new StorageLocation(warehouse.getId(), area.getId(), "A01-01-01"));
            locationRepository.save(new StorageLocation(warehouse.getId(), area.getId(), "A01-01-02"));
        };
    }
}
