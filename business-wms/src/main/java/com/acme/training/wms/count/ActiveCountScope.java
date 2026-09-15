package com.acme.training.wms.count;

import javax.persistence.*;

@Entity
@Table(name="wms_active_count_scope", uniqueConstraints=@UniqueConstraint(name="uk_active_count_scope", columnNames={"sku_id","warehouse_id","location_id"}))
class ActiveCountScope {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="count_id",nullable=false) private Long countId;
    @Column(name="sku_id",nullable=false) private Long skuId;
    @Column(name="warehouse_id",nullable=false) private Long warehouseId;
    @Column(name="location_id",nullable=false) private Long locationId;
    protected ActiveCountScope() { }
    ActiveCountScope(Long countId, Long skuId, Long warehouseId, Long locationId){this.countId=countId;this.skuId=skuId;this.warehouseId=warehouseId;this.locationId=locationId;}
    Long getCountId(){return countId;}
}
