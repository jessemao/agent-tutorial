package com.acme.training.wms.count;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wms_inventory_count", uniqueConstraints = {
        @UniqueConstraint(name = "uk_count_no", columnNames = "count_no"),
        @UniqueConstraint(name = "uk_count_idempotency", columnNames = "idempotency_key")})
class InventoryCount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="count_no", unique=true, length=32) private String countNo;
    @Column(name="warehouse_id", nullable=false) private Long warehouseId;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private InventoryCountStatus status;
    @Column(name="idempotency_key", nullable=false, length=100) private String idempotencyKey;
    @Column(name="request_fingerprint", nullable=false, length=1000) private String requestFingerprint;
    @Column(name="created_by", nullable=false, length=100) private String createdBy;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(length=500) private String note;
    @Column(name="submitted_by",length=100) private String submittedBy;
    @Column(name="submitted_at") private Instant submittedAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Column(name="rejected_by",length=100) private String rejectedBy;
    @Column(name="rejected_at") private Instant rejectedAt;
    @Column(name="rejection_reason",length=500) private String rejectionReason;
    @Column(name="cancelled_by",length=100) private String cancelledBy;
    @Column(name="cancelled_at") private Instant cancelledAt;
    @Column(name="cancellation_reason",length=500) private String cancellationReason;
    @Column(name="correction_of_count_id") private Long correctionOfCountId;
    @Column(name="approved_by",length=100) private String approvedBy;
    @Column(name="approved_at") private Instant approvedAt;
    @Column(name="last_approval_failure_code",length=80) private String lastApprovalFailureCode;
    @Column(name="last_approval_failure_at") private Instant lastApprovalFailureAt;
    @Version private long version;

    protected InventoryCount() { }
    InventoryCount(Long warehouseId, String key, String fingerprint, String operator,Long correctionOfCountId) {
        this.warehouseId=warehouseId; this.idempotencyKey=key; this.requestFingerprint=fingerprint;
        this.createdBy=operator; this.createdAt=Instant.now(); this.updatedAt=this.createdAt; this.status=InventoryCountStatus.DRAFT; this.correctionOfCountId=correctionOfCountId;
    }
    void assignNumber() { this.countNo = String.format("IC-%08d", id); }
    void saveDraft(String note){this.note=note;this.updatedAt=Instant.now();}
    void submit(String operatorId,String note){this.status=InventoryCountStatus.SUBMITTED;this.submittedBy=operatorId;this.submittedAt=Instant.now();this.updatedAt=this.submittedAt;if(note!=null)this.note=note;}
    void reject(String operatorId,String reason){this.status=InventoryCountStatus.REJECTED;this.rejectedBy=operatorId;this.rejectedAt=Instant.now();this.rejectionReason=reason;this.updatedAt=this.rejectedAt;}
    void reopen(){this.status=InventoryCountStatus.DRAFT;this.updatedAt=Instant.now();}
    void cancel(String operatorId,String reason){this.status=InventoryCountStatus.CANCELLED;this.cancelledBy=operatorId;this.cancelledAt=Instant.now();this.cancellationReason=reason;this.updatedAt=this.cancelledAt;}
    void approve(String operatorId,String approvalNote){this.status=InventoryCountStatus.APPROVED;this.approvedBy=operatorId;this.approvedAt=Instant.now();this.updatedAt=this.approvedAt;if(approvalNote!=null)this.note=approvalNote;}
    void recordApprovalFailure(String failureCode){this.lastApprovalFailureCode=failureCode;this.lastApprovalFailureAt=Instant.now();}
    Long getId(){return id;} String getCountNo(){return countNo;} Long getWarehouseId(){return warehouseId;}
    InventoryCountStatus getStatus(){return status;} String getRequestFingerprint(){return requestFingerprint;}
    String getCreatedBy(){return createdBy;} Instant getCreatedAt(){return createdAt;}
    long getVersion(){return version;} String getNote(){return note;} String getSubmittedBy(){return submittedBy;} Instant getSubmittedAt(){return submittedAt;}
    String getRejectedBy(){return rejectedBy;} Instant getRejectedAt(){return rejectedAt;} String getRejectionReason(){return rejectionReason;} String getCancelledBy(){return cancelledBy;} Instant getCancelledAt(){return cancelledAt;} String getCancellationReason(){return cancellationReason;}
    Long getCorrectionOfCountId(){return correctionOfCountId;}
    String getApprovedBy(){return approvedBy;} Instant getApprovedAt(){return approvedAt;}
    String getLastApprovalFailureCode(){return lastApprovalFailureCode;} Instant getLastApprovalFailureAt(){return lastApprovalFailureAt;}
}
