package com.acme.training.wms.web;
import com.acme.training.platform.web.ApiResponse; import com.acme.training.wms.masterdata.*;
import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/wms/master-data")
public class MasterDataController {
    private final MasterDataOperations operations; public MasterDataController(MasterDataOperations operations){this.operations=operations;}
    @GetMapping("/warehouses") public ApiResponse<List<MasterDataOption>> warehouses(){return ApiResponse.success(operations.listWarehouses());}
    @GetMapping("/skus") public ApiResponse<List<MasterDataOption>> skus(){return ApiResponse.success(operations.listSkus());}
    @GetMapping("/locations") public ApiResponse<List<MasterDataOption>> locations(@RequestParam Long warehouseId){return ApiResponse.success(operations.listLocations(warehouseId));}
}
