package com.acme.training.wms.stocktake;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "wms_stocktake")
class Stocktake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "count_no", nullable = false, unique = true, length = 64)
    private String countNo;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "snapshot_total_quantity", nullable = false)
    private long snapshotTotalQuantity;

    @Column(name = "snapshot_reserved_quantity", nullable = false)
    private long snapshotReservedQuantity;

    @Column(name = "counted_total_quantity", nullable = false)
    private long countedTotalQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private StocktakeStatus status;

    @Column(name = "approval_idempotency_key", length = 80)
    private String approvalIdempotencyKey;

    @Version
    private long version;

    protected Stocktake() {
    }

    Stocktake(CreateStocktake command, long snapshotTotalQuantity, long snapshotReservedQuantity) {
        this.countNo = command.getCountNo();
        this.skuId = command.getSkuId();
        this.warehouseId = command.getWarehouseId();
        this.locationId = command.getLocationId();
        this.snapshotTotalQuantity = snapshotTotalQuantity;
        this.snapshotReservedQuantity = snapshotReservedQuantity;
        this.countedTotalQuantity = command.getCountedTotalQuantity();
        this.status = StocktakeStatus.PENDING_APPROVAL;
    }

    void markApproved(String idempotencyKey) {
        status = StocktakeStatus.APPROVED;
        approvalIdempotencyKey = idempotencyKey;
    }

    boolean matchesApproval(String idempotencyKey) {
        return status == StocktakeStatus.APPROVED
                && approvalIdempotencyKey != null
                && approvalIdempotencyKey.equals(idempotencyKey);
    }

    Long getId() { return id; }
    String getCountNo() { return countNo; }
    Long getSkuId() { return skuId; }
    Long getWarehouseId() { return warehouseId; }
    Long getLocationId() { return locationId; }
    long getSnapshotTotalQuantity() { return snapshotTotalQuantity; }
    long getSnapshotReservedQuantity() { return snapshotReservedQuantity; }
    long getCountedTotalQuantity() { return countedTotalQuantity; }
    StocktakeStatus getStatus() { return status; }
}
