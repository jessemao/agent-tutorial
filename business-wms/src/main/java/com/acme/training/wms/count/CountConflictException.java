package com.acme.training.wms.count;
import java.util.List;
public class CountConflictException extends RuntimeException {
    private final String code; private final InventoryCountView current; private final List<Integer> lineIndexes; private final String confirmationToken; private final List<CountApprovalLineView> approvalLines;
    public CountConflictException(String code,String message,InventoryCountView current,List<Integer> indexes){this(code,message,current,indexes,null,java.util.Collections.emptyList());}
    public CountConflictException(String code,String message,InventoryCountView current,List<Integer> indexes,String confirmationToken,List<CountApprovalLineView> approvalLines){super(message);this.code=code;this.current=current;this.lineIndexes=indexes;this.confirmationToken=confirmationToken;this.approvalLines=approvalLines;}
    public String getCode(){return code;} public InventoryCountView getCurrent(){return current;} public List<Integer> getLineIndexes(){return lineIndexes;}
    public String getConfirmationToken(){return confirmationToken;} public List<CountApprovalLineView> getApprovalLines(){return approvalLines;}
}
