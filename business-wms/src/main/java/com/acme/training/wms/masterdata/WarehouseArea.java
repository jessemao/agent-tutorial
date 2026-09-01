package com.acme.training.wms.masterdata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "wms_area", uniqueConstraints =
        @UniqueConstraint(name = "uk_area_warehouse_code", columnNames = {"warehouse_id", "code"}))
public class WarehouseArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    protected WarehouseArea() {
    }

    public WarehouseArea(Long warehouseId, String code, String name) {
        this.warehouseId = warehouseId;
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }
}
