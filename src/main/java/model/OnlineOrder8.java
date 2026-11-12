package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class OnlineOrder8 {
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  private LocalDateTime orderDate;

  public LocalDateTime getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDateTime value) {
    this.orderDate = value;
  }

  private String status;

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  private String deliveryAddress;

  public String getDeliveryAddress() {
    return deliveryAddress;
  }

  public void setDeliveryAddress(String value) {
    this.deliveryAddress = value;
  }

  private String paymentMethod;

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String value) {
    this.paymentMethod = value;
  }

  private Customer8 customer;

  public Customer8 getCustomer() {
    return customer;
  }

  public void setCustomer(Customer8 value) {
    this.customer = value;
  }

  private WarehouseStaff8 warehouseStaff;

  public WarehouseStaff8 getWarehouseStaff() {
    return warehouseStaff;
  }

  public void setWarehouseStaff(WarehouseStaff8 value) {
    this.warehouseStaff = value;
  }

  private DeliveryStaff8 deliveryStaff;

  public DeliveryStaff8 getDeliveryStaff() {
    return deliveryStaff;
  }

  public void setDeliveryStaff(DeliveryStaff8 value) {
    this.deliveryStaff = value;
  }

  private List<OrderItem8> orderItems = new ArrayList<>();

  public List<OrderItem8> getOrderItems() {
    return orderItems;
  }

  public void setOrderItems(List<OrderItem8> value) {
    this.orderItems = value;
  }

  public BigDecimal calculateTotal() {
    return orderItems.stream()
        .map(x -> x.getPrice().multiply(BigDecimal.valueOf(x.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal getTotalAmount() {
    return calculateTotal();
  }
}
