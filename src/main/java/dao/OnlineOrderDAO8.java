package dao;

import java.sql.*;
import java.util.*;
import model.*;

public class OnlineOrderDAO8 extends DAO8 {
  /** Validate every line under locks before writing any document. Lock ordering is stable. */
  public static void reserve(Connection c, List<OrderItem8> lines) throws SQLException {
    require(lines != null && !lines.isEmpty(), "Giỏ hàng trống");
    Set<Integer> seen = new HashSet<>();
    for (var line :
        lines.stream().sorted(Comparator.comparingInt(x -> x.getItem().getId())).toList()) {
      int id = line.getItem().getId();
      require(seen.add(id), "Mặt hàng bị lặp");
      require(line.getQuantity() > 0 && line.getQuantity() <= 100000, "Số lượng không hợp lệ");
      Item8 current = ItemDAO8.get(c, id, true);
      require(current != null, "Mặt hàng đã bị xóa");
      require(
          current.getPrice().compareTo(line.getPrice()) == 0,
          "Giá đã thay đổi. Hãy cập nhật giá giỏ rồi xác nhận lại.");
      require(
          current.getStockQuantity() >= line.getQuantity(),
          "Không đủ tồn kho: " + current.getName());
      require(
          execute(
                  c,
                  "UPDATE tblItem8 SET stockQuantity=stockQuantity-? WHERE id=? AND"
                      + " stockQuantity>=?",
                  line.getQuantity(),
                  id,
                  line.getQuantity())
              == 1,
          "Tồn kho vừa thay đổi");
    }
  }

  public boolean saveOrder(OnlineOrder8 order) throws SQLException {
    require(order.getCustomer() != null, "Thiếu khách hàng");
    require(
        order.getDeliveryAddress() != null
            && !order.getDeliveryAddress().isBlank()
            && order.getDeliveryAddress().length() <= 255,
        "Địa chỉ giao hàng không hợp lệ");
    payment(order.getPaymentMethod());
    int id =
        transaction(
            c -> {
              reserve(c, order.getOrderItems());
              int key =
                  insert(
                      c,
                      "INSERT INTO"
                          + " tblOnlineOrder8(orderDate,status,deliveryAddress,paymentMethod,idCustomer8)"
                          + " VALUES(CURRENT_TIMESTAMP,'Pending',?,?,?)",
                      order.getDeliveryAddress(),
                      order.getPaymentMethod(),
                      order.getCustomer().getId());
              for (var line : order.getOrderItems())
                execute(
                    c,
                    "INSERT INTO tblOrderItem8(quantity,price,idOnlineOrder8,idItem8)"
                        + " VALUES(?,?,?,?)",
                    line.getQuantity(),
                    line.getPrice(),
                    key,
                    line.getItem().getId());
              return key;
            });
    order.setId(id);
    order.setStatus("Pending");
    return true;
  }

  public static void payment(String p) {
    require(
        Set.of("COD", "CASH", "BANK_TRANSFER").contains(p), "Phương thức thanh toán không hợp lệ");
  }

  public void advance(int id, String expected, int warehouse, int delivery) throws SQLException {
    String next =
        switch (expected) {
          case "Pending" -> "Exported";
          case "Exported" -> "Delivering";
          case "Delivering" -> "Completed";
          default -> throw new IllegalArgumentException("Trạng thái không hợp lệ");
        };
    transaction(
        c -> {
          if (expected.equals("Pending")) {
            require(delivery > 0, "Chọn nhân viên giao hàng");
            require(
                execute(
                        c,
                        "UPDATE tblOnlineOrder8 SET status=?,idWarehouseStaff8=?,idDeliveryStaff8=?"
                            + " WHERE id=? AND status=?",
                        next,
                        warehouse,
                        delivery,
                        id,
                        expected)
                    == 1,
                "Đơn đã thay đổi, hãy tải lại");
          } else
            require(
                execute(
                        c,
                        "UPDATE tblOnlineOrder8 SET status=? WHERE id=? AND status=?",
                        next,
                        id,
                        expected)
                    == 1,
                "Đơn đã thay đổi, hãy tải lại");
          return true;
        });
  }

  public List<Map<String, Object>> list(Integer customer) throws SQLException {
    return query(
        "SELECT o.*,u.fullName,u.phone,COALESCE((SELECT SUM(quantity*price) FROM tblOrderItem8"
            + " WHERE idOnlineOrder8=o.id),0) AS totalAmount FROM tblOnlineOrder8 o JOIN tblUser8 u"
            + " ON u.id=o.idCustomer8"
            + (customer == null ? "" : " WHERE o.idCustomer8=?")
            + " ORDER BY o.id DESC",
        customer == null ? new Object[] {} : new Object[] {customer});
  }

  public List<Map<String, Object>> details(int id, Integer customer) throws SQLException {
    return query(
        "SELECT i.code,i.name,d.quantity,d.price,d.quantity*d.price AS amount FROM tblOrderItem8 d"
            + " JOIN tblItem8 i ON i.id=d.idItem8 JOIN tblOnlineOrder8 o ON o.id=d.idOnlineOrder8"
            + " WHERE o.id=?"
            + (customer == null ? "" : " AND o.idCustomer8=?"),
        customer == null ? new Object[] {id} : new Object[] {id, customer});
  }
}
