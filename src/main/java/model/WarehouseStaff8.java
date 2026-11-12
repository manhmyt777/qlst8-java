package model;

import java.util.*;

public class WarehouseStaff8 extends Staff8 {
  private String warehouseArea;

  public String getWarehouseArea() {
    return warehouseArea;
  }

  public void setWarehouseArea(String value) {
    this.warehouseArea = value;
  }

  private List<ImportNote8> importNotes = new ArrayList<>();

  public List<ImportNote8> getImportNotes() {
    return importNotes;
  }

  public void setImportNotes(List<ImportNote8> value) {
    this.importNotes = value;
  }

  private List<OnlineOrder8> onlineOrders = new ArrayList<>();

  public List<OnlineOrder8> getOnlineOrders() {
    return onlineOrders;
  }

  public void setOnlineOrders(List<OnlineOrder8> value) {
    this.onlineOrders = value;
  }
}
