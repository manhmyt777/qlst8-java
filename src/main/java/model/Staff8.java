package model;

import java.math.BigDecimal;
import java.util.*;

public abstract class Staff8 extends User8 {
  private String position;

  public String getPosition() {
    return position;
  }

  public void setPosition(String value) {
    this.position = value;
  }

  private BigDecimal salaryRate = BigDecimal.ZERO;

  public BigDecimal getSalaryRate() {
    return salaryRate;
  }

  public void setSalaryRate(BigDecimal value) {
    this.salaryRate = value;
  }
}
