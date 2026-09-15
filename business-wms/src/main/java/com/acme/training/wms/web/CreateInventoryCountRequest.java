package com.acme.training.wms.web;
import javax.validation.Valid; import javax.validation.constraints.*; import java.util.List;
public class CreateInventoryCountRequest {
    @NotBlank private String idempotencyKey; @NotNull private Long warehouseId; @NotEmpty @Valid private List<Line> lines; private Long correctionOfCountId;
    public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;}
    public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long v){warehouseId=v;}
    public List<Line> getLines(){return lines;} public void setLines(List<Line> v){lines=v;}
    public Long getCorrectionOfCountId(){return correctionOfCountId;} public void setCorrectionOfCountId(Long v){correctionOfCountId=v;}
    public static class Line { @NotNull private Long skuId; @NotNull private Long locationId;
        public Long getSkuId(){return skuId;} public void setSkuId(Long v){skuId=v;} public Long getLocationId(){return locationId;} public void setLocationId(Long v){locationId=v;} }
}
