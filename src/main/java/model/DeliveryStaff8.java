package model;

import java.util.*;

public class DeliveryStaff8 extends Staff8 {
  private List<OnlineOrder8> onlineOrders = new ArrayList<>();

  public List<OnlineOrder8> getOnlineOrders() {
    return onlineOrders;
  }

  public void setOnlineOrders(List<OnlineOrder8> value) {
    this.onlineOrders = value;
  }
}
