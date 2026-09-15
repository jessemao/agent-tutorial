package com.acme.training.wms.web;
import com.acme.training.platform.web.ApiResponse; import com.acme.training.wms.count.InvalidCountRequestException;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import com.acme.training.wms.count.CountConflictException;
import java.util.*;
@RestControllerAdvice(assignableTypes=InventoryCountController.class)
public class InventoryCountExceptionHandler {
    @ExceptionHandler(InvalidCountRequestException.class) public ResponseEntity<ApiResponse<Void>> invalid(InvalidCountRequestException e){return ResponseEntity.badRequest().body(ApiResponse.failure("INVALID_REQUEST",e.getMessage()));}
    @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<ApiResponse<Void>> conflict(DataIntegrityViolationException e){
        String detail=e.getMostSpecificCause()==null?"":e.getMostSpecificCause().getMessage();
        boolean idempotency=detail.contains("uk_count_idempotency");
        boolean scope=detail.contains("uk_active_count_scope");
        if(!idempotency&&!scope)throw e;
        String code=idempotency?"WMS_IDEMPOTENCY_CONFLICT":"WMS_COUNT_SCOPE_CONFLICT";
        String message=idempotency?"幂等键已用于其他创建请求":"该库存维度已有进行中的盘点单";
        return ResponseEntity.status(409).body(ApiResponse.failure(code,message));
    }
    @ExceptionHandler(CountConflictException.class) public ResponseEntity<ConflictResponse> countConflict(CountConflictException e){return ResponseEntity.status(409).body(new ConflictResponse(e));}
    static final class ConflictResponse { private final boolean success=false; private final String code,message; private final ConflictData data; ConflictResponse(CountConflictException e){this.code=e.getCode();this.message=e.getMessage();this.data=new ConflictData(e);} public boolean isSuccess(){return success;} public String getCode(){return code;} public String getMessage(){return message;} public ConflictData getData(){return data;} }
    static final class ConflictData { private final Object current; private final List<Integer> lineIndexes; private final String confirmationToken; private final Object approvalLines; ConflictData(CountConflictException e){this.current=e.getCurrent();this.lineIndexes=e.getLineIndexes();this.confirmationToken=e.getConfirmationToken();this.approvalLines=e.getApprovalLines();} public Object getCurrent(){return current;} public List<Integer> getLineIndexes(){return lineIndexes;} public String getConfirmationToken(){return confirmationToken;} public Object getApprovalLines(){return approvalLines;} }
}
