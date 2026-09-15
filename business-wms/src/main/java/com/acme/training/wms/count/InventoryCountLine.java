package com.acme.training.wms.count;

import javax.persistence.*;

@Entity
@Table(name="wms_inventory_count_line", uniqueConstraints=@UniqueConstraint(name="uk_count_dimension", columnNames={"count_id","sku_id","location_id"}))
class InventoryCountLine {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="count_id",nullable=false) private Long countId;
    @Column(name="sku_id",nullable=false) private Long skuId;
    @Column(name="location_id",nullable=false) private Long locationId;
    @Column(name="creation_book_total",nullable=false) private long creationBookTotal;
    @Column(name="counted_total") private Long countedTotal;
    @Column(name="submitted_book_total") private Long submittedBookTotal;
    @Column(name="difference_reason",length=500) private String differenceReason;
    @Column(name="approval_book_total") private Long approvalBookTotal;
    @Column(name="approval_difference") private Long difference;
    @Column(name="available_after") private Long availableAfter;
    @Column(name="reserved_after") private Long reservedAfter;
    protected InventoryCountLine() { }
    InventoryCountLine(Long countId, Long skuId, Long locationId, long total){this.countId=countId;this.skuId=skuId;this.locationId=locationId;this.creationBookTotal=total;}
    Long getSkuId(){return skuId;} Long getLocationId(){return locationId;} long getCreationBookTotal(){return creationBookTotal;}
    Long getCountedTotal(){return countedTotal;} Long getSubmittedBookTotal(){return submittedBookTotal;} String getDifferenceReason(){return differenceReason;}
    void record(Long countedTotal,String differenceReason){this.countedTotal=countedTotal;this.differenceReason=differenceReason;}
    void submit(long bookTotal){this.submittedBookTotal=bookTotal;}
    void approve(long bookTotal,long approvedDifference,long approvedAvailable,long approvedReserved){this.approvalBookTotal=bookTotal;this.difference=approvedDifference;this.availableAfter=approvedAvailable;this.reservedAfter=approvedReserved;}
    Long getApprovalBookTotal(){return approvalBookTotal;} Long getDifference(){return difference;} Long getAvailableAfter(){return availableAfter;} Long getReservedAfter(){return reservedAfter;}
}
