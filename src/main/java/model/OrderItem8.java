package model;

import java.math.BigDecimal;
import java.util.*;

public class OrderItem8 {
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  private int quantity;

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int value) {
    this.quantity = value;
  }

  private BigDecimal price = BigDecimal.ZERO;

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal value) {
    this.price = value;
  }

  private Item8 item;

  public Item8 getItem() {
    return item;
  }

  public void setItem(Item8 value) {
    this.item = value;
  }
}
