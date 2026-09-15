package com.acme.training.wms.web;
import com.acme.training.platform.web.ApiResponse;
import com.acme.training.wms.count.*;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/wms/inventory-counts")
public class InventoryCountController {
    private final InventoryCountOperations operations;
    public InventoryCountController(InventoryCountOperations operations){this.operations=operations;}
    @PostMapping public ApiResponse<InventoryCountView> create(@Valid @RequestBody CreateInventoryCountRequest r){
        return ApiResponse.success(operations.create(new CreateInventoryCount(r.getIdempotencyKey(),r.getWarehouseId(),r.getLines().stream().map(v->new CountDimension(v.getSkuId(),v.getLocationId())).collect(Collectors.toList()),r.getCorrectionOfCountId())));
    }
    @GetMapping("/{id}") public ApiResponse<InventoryCountView> get(@PathVariable Long id){return ApiResponse.success(operations.get(id));}
    @GetMapping public ApiResponse<InventoryCountPage> list(@RequestParam(required=false) String countNo,@RequestParam(required=false) Long warehouseId,@RequestParam(required=false) InventoryCountStatus status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return ApiResponse.success(operations.list(countNo,warehouseId,status,page,size));}
    @PutMapping("/{id}") public ApiResponse<InventoryCountView> save(@PathVariable Long id,@Valid @RequestBody SaveInventoryCountRequest r){return ApiResponse.success(operations.saveDraft(id,new SaveInventoryCountDraft(r.getIdempotencyKey(),r.getExpectedVersion(),r.getNote(),r.getLines().stream().map(v->new SaveCountLine(v.getSkuId(),v.getLocationId(),v.getCountedTotal(),v.getDifferenceReason())).collect(Collectors.toList()))));}
    @PostMapping("/{id}/transitions") public ApiResponse<InventoryCountView> transition(@PathVariable Long id,@Valid @RequestBody CountTransitionRequest r){if("SUBMIT".equals(r.getAction()))return ApiResponse.success(operations.submit(id,new SubmitInventoryCount(r.getIdempotencyKey(),r.getExpectedVersion(),r.getNote())));return ApiResponse.success(operations.transition(id,new TransitionInventoryCount(r.getAction(),r.getIdempotencyKey(),r.getExpectedVersion(),r.getReason())));}
    @PostMapping("/{id}/approval") public ApiResponse<InventoryCountView> approve(@PathVariable Long id,@Valid @RequestBody ApproveInventoryCountRequest r){return ApiResponse.success(operations.approve(id,new ApproveInventoryCount(r.getIdempotencyKey(),r.getExpectedVersion(),r.getNote(),r.getConfirmationToken())));}
}
