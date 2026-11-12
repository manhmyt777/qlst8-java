package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class Item8 {
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  private String code;

  public String getCode() {
    return code;
  }

  public void setCode(String value) {
    this.code = value;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  private String manufacturer;

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String value) {
    this.manufacturer = value;
  }

  private String specs;

  public String getSpecs() {
    return specs;
  }

  public void setSpecs(String value) {
    this.specs = value;
  }

  private BigDecimal price = BigDecimal.ZERO;

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal value) {
    this.price = value;
  }

  private int stockQuantity;

  public int getStockQuantity() {
    return stockQuantity;
  }

  public void setStockQuantity(int value) {
    this.stockQuantity = value;
  }

  private int warrantyPeriod;

  public int getWarrantyPeriod() {
    return warrantyPeriod;
  }

  public void setWarrantyPeriod(int value) {
    this.warrantyPeriod = value;
  }

  private String unit;

  public String getUnit() {
    return unit;
  }

  public void setUnit(String value) {
    this.unit = value;
  }

  private LocalDateTime updatedAt;

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime value) {
    this.updatedAt = value;
  }
}
