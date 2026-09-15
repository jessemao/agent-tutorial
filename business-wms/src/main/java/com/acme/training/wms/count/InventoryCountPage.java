package com.acme.training.wms.count;
import java.util.List;
public final class InventoryCountPage {
    private final List<InventoryCountView> items; private final long total; private final int page; private final int size;
    public InventoryCountPage(List<InventoryCountView> items,long total,int page,int size){this.items=items;this.total=total;this.page=page;this.size=size;}
    public List<InventoryCountView> getItems(){return items;} public long getTotal(){return total;} public int getPage(){return page;} public int getSize(){return size;}
}
