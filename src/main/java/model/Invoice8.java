package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class Invoice8 {
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  private LocalDateTime createdDate;

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(LocalDateTime value) {
    this.createdDate = value;
  }

  private String paymentMethod;

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String value) {
    this.paymentMethod = value;
  }

  private SalesStaff8 salesStaff;

  public SalesStaff8 getSalesStaff() {
    return salesStaff;
  }

  public void setSalesStaff(SalesStaff8 value) {
    this.salesStaff = value;
  }

  private Customer8 customer;

  public Customer8 getCustomer() {
    return customer;
  }

  public void setCustomer(Customer8 value) {
    this.customer = value;
  }

  private List<InvoiceItem8> invoiceItems = new ArrayList<>();

  public List<InvoiceItem8> getInvoiceItems() {
    return invoiceItems;
  }

  public void setInvoiceItems(List<InvoiceItem8> value) {
    this.invoiceItems = value;
  }

  public BigDecimal calculateTotal() {
    return invoiceItems.stream()
        .map(x -> x.getPrice().multiply(BigDecimal.valueOf(x.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal getTotalAmount() {
    return calculateTotal();
  }
}
