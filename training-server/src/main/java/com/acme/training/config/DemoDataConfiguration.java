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
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DemoDataConfiguration {

    @Bean
    public CommandLineRunner demoWarehouse(WarehouseRepository warehouseRepository,
                                           WarehouseAreaRepository areaRepository,
                                           StorageLocationRepository locationRepository,
                                           JdbcTemplate jdbcTemplate) {
        return args -> {
            if (warehouseRepository.count() > 0) {
                return;
            }
            Warehouse warehouse = warehouseRepository.save(new Warehouse("WH-SH", "上海培训仓"));
            WarehouseArea area = areaRepository.save(new WarehouseArea(warehouse.getId(), "A01", "常温区"));
            StorageLocation source = locationRepository.save(
                    new StorageLocation(warehouse.getId(), area.getId(), "A01-01-01"));
            locationRepository.save(new StorageLocation(warehouse.getId(), area.getId(), "A01-01-02"));
            jdbcTemplate.update("insert into wms_inventory_balance "
                            + "(sku_id, warehouse_id, location_id, available_quantity, reserved_quantity, version) "
                            + "values (?, ?, ?, ?, ?, ?)",
                    303L, warehouse.getId(), source.getId(), 10L, 0L, 0L);
        };
    }
}
