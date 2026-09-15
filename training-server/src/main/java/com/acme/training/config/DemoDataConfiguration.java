package com.acme.training.config;

import com.acme.training.wms.masterdata.StorageLocation;
import com.acme.training.wms.masterdata.StorageLocationRepository;
import com.acme.training.wms.masterdata.Warehouse;
import com.acme.training.wms.masterdata.WarehouseArea;
import com.acme.training.wms.masterdata.WarehouseAreaRepository;
import com.acme.training.wms.masterdata.WarehouseRepository;
import com.acme.training.wms.masterdata.SkuRepository;
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
                                           SkuRepository skuRepository,
                                           JdbcTemplate jdbcTemplate) {
        return args -> {
            if (skuRepository.count() == 0) {
                jdbcTemplate.update("insert into wms_sku (id, code, name, enabled) values (?, ?, ?, ?)",
                        303L, "SKU-303", "培训商品 303", true);
                jdbcTemplate.update("insert into wms_sku (id, code, name, enabled) values (?, ?, ?, ?)",
                        304L, "SKU-304", "培训商品 304", true);
            }
            if (warehouseRepository.count() > 0) {
                return;
            }
            Warehouse warehouse = warehouseRepository.save(new Warehouse("WH-SH", "上海培训仓"));
            WarehouseArea area = areaRepository.save(new WarehouseArea(warehouse.getId(), "A01", "常温区"));
            StorageLocation source = locationRepository.save(
                    new StorageLocation(warehouse.getId(), area.getId(), "A01-01-01"));
            locationRepository.save(new StorageLocation(warehouse.getId(), area.getId(), "A01-01-02"));
            locationRepository.save(StorageLocation.disabled(
                    warehouse.getId(), area.getId(), "DISABLED-T03"));
            Warehouse otherWarehouse = warehouseRepository.save(new Warehouse("WH-T03-OTHER", "T03 异仓测试仓"));
            WarehouseArea otherArea = areaRepository.save(
                    new WarehouseArea(otherWarehouse.getId(), "T03", "T03 异仓测试区"));
            locationRepository.save(new StorageLocation(
                    otherWarehouse.getId(), otherArea.getId(), "OTHER-WAREHOUSE-T03"));
            jdbcTemplate.update("insert into wms_inventory_balance "
                            + "(sku_id, warehouse_id, location_id, available_quantity, reserved_quantity, version) "
                            + "values (?, ?, ?, ?, ?, ?)",
                    303L, warehouse.getId(), source.getId(), 10L, 0L, 0L);
        };
    }
}
