package com.acme.training.wms.count;
import com.acme.training.wms.masterdata.MasterDataOption;
public final class InventoryCountLineView implements java.io.Serializable {
    private final MasterDataOption sku; private final MasterDataOption location; private final long creationBookTotal; private final Long countedTotal; private final Long submittedBookTotal; private final String differenceReason; private final Long approvalBookTotal,difference,availableAfter,reservedAfter;
    public InventoryCountLineView(MasterDataOption sku,MasterDataOption location,long total,Long counted,Long submitted,String reason,Long approvalBookTotal,Long difference,Long availableAfter,Long reservedAfter){this.sku=sku;this.location=location;this.creationBookTotal=total;this.countedTotal=counted;this.submittedBookTotal=submitted;this.differenceReason=reason;this.approvalBookTotal=approvalBookTotal;this.difference=difference;this.availableAfter=availableAfter;this.reservedAfter=reservedAfter;}
    public MasterDataOption getSku(){return sku;} public MasterDataOption getLocation(){return location;} public long getCreationBookTotal(){return creationBookTotal;} public Long getCountedTotal(){return countedTotal;} public Long getSubmittedBookTotal(){return submittedBookTotal;} public String getDifferenceReason(){return differenceReason;}
    public Long getApprovalBookTotal(){return approvalBookTotal;} public Long getDifference(){return difference;} public Long getAvailableAfter(){return availableAfter;} public Long getReservedAfter(){return reservedAfter;}
}
