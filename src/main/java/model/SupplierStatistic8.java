package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class SupplierStatistic8 extends Supplier8 {
  private LocalDateTime startDate;

  public LocalDateTime getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDateTime value) {
    this.startDate = value;
  }

  private LocalDateTime endDate;

  public LocalDateTime getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDateTime value) {
    this.endDate = value;
  }

  private int totalImportedQuantity;

  public int getTotalImportedQuantity() {
    return totalImportedQuantity;
  }

  public void setTotalImportedQuantity(int value) {
    totalImportedQuantity = value;
  }

  private BigDecimal totalImportValue = BigDecimal.ZERO;

  public BigDecimal getTotalImportValue() {
    return totalImportValue;
  }

  public void setTotalImportValue(BigDecimal value) {
    totalImportValue = value;
  }
}
