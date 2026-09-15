package com.acme.training.wms.count;

public final class CountApprovalLineView {
    private final com.acme.training.wms.masterdata.MasterDataOption sku,location; private final long approvalBookTotal; private final long difference;
    public CountApprovalLineView(com.acme.training.wms.masterdata.MasterDataOption sku,com.acme.training.wms.masterdata.MasterDataOption location,long approvalBookTotal,long difference){this.sku=sku;this.location=location;this.approvalBookTotal=approvalBookTotal;this.difference=difference;}
    public com.acme.training.wms.masterdata.MasterDataOption getSku(){return sku;} public com.acme.training.wms.masterdata.MasterDataOption getLocation(){return location;} public long getApprovalBookTotal(){return approvalBookTotal;} public long getDifference(){return difference;}
}
