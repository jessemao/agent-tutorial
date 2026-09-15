package com.acme.training.wms.count;
import java.util.List;
public final class CreateInventoryCount {
    private final String idempotencyKey; private final Long warehouseId; private final List<CountDimension> lines; private final Long correctionOfCountId;
    public CreateInventoryCount(String key,Long warehouseId,List<CountDimension> lines){this(key,warehouseId,lines,null);}
    public CreateInventoryCount(String key,Long warehouseId,List<CountDimension> lines,Long correctionOfCountId){this.idempotencyKey=key;this.warehouseId=warehouseId;this.lines=lines;this.correctionOfCountId=correctionOfCountId;}
    public String getIdempotencyKey(){return idempotencyKey;} public Long getWarehouseId(){return warehouseId;} public List<CountDimension> getLines(){return lines;} public Long getCorrectionOfCountId(){return correctionOfCountId;}
}
