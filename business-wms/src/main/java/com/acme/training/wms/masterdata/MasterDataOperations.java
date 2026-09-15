package com.acme.training.wms.masterdata;

import java.util.List;

public interface MasterDataOperations {
    List<MasterDataOption> listWarehouses();
    List<MasterDataOption> listSkus();
    List<MasterDataOption> listLocations(Long warehouseId);
}
