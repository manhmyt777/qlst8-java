package model;

import java.math.BigDecimal;
import java.util.*;

public class Cart8 {
  private final SortedMap<Integer, OrderItem8> lines = new TreeMap<>();

  public synchronized List<OrderItem8> getLines() {
    return new ArrayList<>(lines.values());
  }

  public synchronized int quantity(int id) {
    return lines.containsKey(id) ? lines.get(id).getQuantity() : 0;
  }

  public synchronized void set(Item8 item, int quantity) {
    if (quantity < 0 || quantity > 100000)
      throw new IllegalArgumentException("Số lượng không hợp lệ");
    if (quantity == 0) {
      lines.remove(item.getId());
      return;
    }
    OrderItem8 line = new OrderItem8();
    line.setItem(item);
    line.setPrice(item.getPrice());
    line.setQuantity(quantity);
    lines.put(item.getId(), line);
  }

  public synchronized void clear() {
    lines.clear();
  }

  public synchronized BigDecimal getTotal() {
    return lines.values().stream()
        .map(x -> x.getPrice().multiply(BigDecimal.valueOf(x.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
