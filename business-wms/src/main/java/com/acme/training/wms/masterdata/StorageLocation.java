package com.acme.training.wms.masterdata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "wms_location", uniqueConstraints =
        @UniqueConstraint(name = "uk_location_warehouse_code", columnNames = {"warehouse_id", "code"}))
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false)
    private boolean enabled;

    protected StorageLocation() {
    }

    public StorageLocation(Long warehouseId, Long areaId, String code) {
        this.warehouseId = warehouseId;
        this.areaId = areaId;
        this.code = code;
        this.enabled = true;
    }

    public Long getId() {
        return id;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
