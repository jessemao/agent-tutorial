package com.acme.training.wms.web;
import javax.validation.Valid; import javax.validation.constraints.*; import java.util.List;
public class SaveInventoryCountRequest {
    @NotBlank private String idempotencyKey; @NotNull private Long expectedVersion; private String note; @NotEmpty @Valid private List<Line> lines;
    public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;} public Long getExpectedVersion(){return expectedVersion;} public void setExpectedVersion(Long v){expectedVersion=v;} public String getNote(){return note;} public void setNote(String v){note=v;} public List<Line> getLines(){return lines;} public void setLines(List<Line> v){lines=v;}
    public static class Line { @NotNull private Long skuId; @NotNull private Long locationId; @PositiveOrZero private Long countedTotal; private String differenceReason; public Long getSkuId(){return skuId;} public void setSkuId(Long v){skuId=v;} public Long getLocationId(){return locationId;} public void setLocationId(Long v){locationId=v;} public Long getCountedTotal(){return countedTotal;} public void setCountedTotal(Long v){countedTotal=v;} public String getDifferenceReason(){return differenceReason;} public void setDifferenceReason(String v){differenceReason=v;} }
}
