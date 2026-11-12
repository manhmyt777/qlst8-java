package dao;

import java.sql.*;
import java.time.*;
import java.util.*;

public class StatisticDAO8 extends DAO8 {
  public Map<String, Object> report(LocalDate start, LocalDate end) throws SQLException {
    require(!end.isBefore(start), "Ngày kết thúc phải từ ngày bắt đầu");
    var a = start.atStartOfDay();
    var b = end.plusDays(1).atStartOfDay();
    String sold =
        "SELECT d.idItem8,d.quantity,d.price FROM tblInvoiceItem8 d JOIN tblInvoice8 h ON"
            + " h.id=d.idInvoice8 WHERE h.createdDate>=? AND h.createdDate<? UNION ALL SELECT"
            + " d.idItem8,d.quantity,d.price FROM tblOrderItem8 d JOIN tblOnlineOrder8 h ON"
            + " h.id=d.idOnlineOrder8 WHERE h.status='Completed' AND h.orderDate>=? AND"
            + " h.orderDate<?";
    try (var c = open()) {
      var items =
          query(
              c,
              "SELECT i.code,i.name,SUM(x.quantity) AS totalSold,SUM(x.quantity*x.price) AS"
                  + " totalRevenue FROM ("
                  + sold
                  + ") x JOIN tblItem8 i ON i.id=x.idItem8 GROUP BY i.id,i.code,i.name ORDER BY"
                  + " totalRevenue DESC",
              a,
              b,
              a,
              b);
      var suppliers =
          query(
              c,
              "SELECT s.id,s.name,SUM(d.quantity) AS"
                  + " totalImportedQuantity,SUM(d.quantity*d.importPrice) AS totalImportValue FROM"
                  + " tblImportDetail8 d JOIN tblImportNote8 n ON n.id=d.idImportNote8 JOIN"
                  + " tblSupplier8 s ON s.id=n.idSupplier8 WHERE n.importDate>=? AND n.importDate<?"
                  + " GROUP BY s.id,s.name ORDER BY totalImportValue DESC",
              a,
              b);
      var total =
          query(
                  c,
                  "SELECT COALESCE(SUM(x.quantity*x.price),0) AS totalRevenue FROM ("
                      + sold
                      + ") x",
                  a,
                  b,
                  a,
                  b)
              .get(0)
              .get("totalRevenue");
      return Map.of("items", items, "suppliers", suppliers, "total", total);
    }
  }
}
