package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class ItemStatistic8 extends Item8 {
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

  private int totalSold;

  public int getTotalSold() {
    return totalSold;
  }

  public void setTotalSold(int value) {
    totalSold = value;
  }

  private BigDecimal totalRevenue = BigDecimal.ZERO;

  public BigDecimal getTotalRevenue() {
    return totalRevenue;
  }

  public void setTotalRevenue(BigDecimal value) {
    totalRevenue = value;
  }
}
