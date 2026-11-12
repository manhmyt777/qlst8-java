package model;

import java.util.*;

public class Customer8 extends User8 {
  private String memberCard;

  public String getMemberCard() {
    return memberCard;
  }

  public void setMemberCard(String value) {
    this.memberCard = value;
  }

  private int loyaltyPoints;

  public int getLoyaltyPoints() {
    return loyaltyPoints;
  }

  public void setLoyaltyPoints(int value) {
    this.loyaltyPoints = value;
  }

  private List<OnlineOrder8> onlineOrders = new ArrayList<>();

  public List<OnlineOrder8> getOnlineOrders() {
    return onlineOrders;
  }

  public void setOnlineOrders(List<OnlineOrder8> value) {
    this.onlineOrders = value;
  }
}
