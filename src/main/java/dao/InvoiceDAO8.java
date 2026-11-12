package dao;

import java.sql.*;
import java.util.*;
import model.*;

public class InvoiceDAO8 extends DAO8 {
  public int sell(int staff, Integer customer, String payment, List<OrderItem8> lines)
      throws SQLException {
    OnlineOrderDAO8.payment(payment);
    return transaction(
        c -> {
          OnlineOrderDAO8.reserve(c, lines);
          int id =
              insert(
                  c,
                  "INSERT INTO tblInvoice8(createdDate,paymentMethod,idSalesStaff8,idCustomer8)"
                      + " VALUES(CURRENT_TIMESTAMP,?,?,?)",
                  payment,
                  staff,
                  customer);
          for (var line : lines)
            execute(
                c,
                "INSERT INTO tblInvoiceItem8(quantity,price,idInvoice8,idItem8) VALUES(?,?,?,?)",
                line.getQuantity(),
                line.getPrice(),
                id,
                line.getItem().getId());
          return id;
        });
  }

  public List<Map<String, Object>> list() throws SQLException {
    return query(
        "SELECT v.*,u.fullName,COALESCE((SELECT SUM(quantity*price) FROM tblInvoiceItem8 WHERE"
            + " idInvoice8=v.id),0) AS totalAmount FROM tblInvoice8 v JOIN tblUser8 u ON"
            + " u.id=v.idSalesStaff8 ORDER BY v.id DESC");
  }

  public List<Map<String, Object>> details(int id) throws SQLException {
    return query(
        "SELECT i.code,i.name,d.quantity,d.price,d.quantity*d.price AS amount FROM tblInvoiceItem8"
            + " d JOIN tblItem8 i ON i.id=d.idItem8 WHERE d.idInvoice8=?",
        id);
  }
}
