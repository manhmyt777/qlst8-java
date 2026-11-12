package model;

import java.util.*;

public class SalesStaff8 extends Staff8 {
  private List<Invoice8> invoices = new ArrayList<>();

  public List<Invoice8> getInvoices() {
    return invoices;
  }

  public void setInvoices(List<Invoice8> value) {
    this.invoices = value;
  }
}
