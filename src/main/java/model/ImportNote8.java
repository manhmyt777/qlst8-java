package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class ImportNote8 {
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  private LocalDateTime importDate;

  public LocalDateTime getImportDate() {
    return importDate;
  }

  public void setImportDate(LocalDateTime value) {
    this.importDate = value;
  }

  private String note;

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    this.note = value;
  }

  private WarehouseStaff8 warehouseStaff;

  public WarehouseStaff8 getWarehouseStaff() {
    return warehouseStaff;
  }

  public void setWarehouseStaff(WarehouseStaff8 value) {
    this.warehouseStaff = value;
  }

  private Supplier8 supplier;

  public Supplier8 getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier8 value) {
    this.supplier = value;
  }

  private List<ImportDetail8> importDetails = new ArrayList<>();

  public List<ImportDetail8> getImportDetails() {
    return importDetails;
  }

  public void setImportDetails(List<ImportDetail8> value) {
    this.importDetails = value;
  }

  public BigDecimal calculateTotal() {
    return importDetails.stream()
        .map(x -> x.getImportPrice().multiply(BigDecimal.valueOf(x.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal getTotalCost() {
    return calculateTotal();
  }
}
