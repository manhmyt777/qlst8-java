package dao;

import java.sql.*;
import java.util.*;
import model.*;

public class ImportNoteDAO8 extends DAO8 {
  public int save(int staff, int supplier, String note, List<ImportDetail8> lines)
      throws SQLException {
    require(!lines.isEmpty(), "Cần ít nhất một mặt hàng");
    require(note.length() <= 500, "Ghi chú tối đa 500 ký tự");
    return transaction(
        c -> {
          int id =
              insert(
                  c,
                  "INSERT INTO tblImportNote8(importDate,note,idWarehouseStaff8,idSupplier8)"
                      + " VALUES(CURRENT_TIMESTAMP,?,?,?)",
                  note,
                  staff,
                  supplier);
          Set<Integer> seen = new HashSet<>();
          for (var line :
              lines.stream().sorted(Comparator.comparingInt(x -> x.getItem().getId())).toList()) {
            require(seen.add(line.getItem().getId()), "Mặt hàng bị lặp");
            require(
                line.getQuantity() > 0 && line.getQuantity() <= 100000, "Số lượng không hợp lệ");
            require(
                line.getImportPrice().signum() > 0
                    && line.getImportPrice().precision() <= 15
                    && line.getImportPrice().scale() <= 2,
                "Giá nhập không hợp lệ");
            var item = ItemDAO8.get(c, line.getItem().getId(), true);
            require(item != null, "Không tìm thấy mặt hàng");
            require(
                item.getStockQuantity() <= Integer.MAX_VALUE - line.getQuantity(),
                "Tồn kho vượt giới hạn");
            execute(
                c,
                "INSERT INTO tblImportDetail8(quantity,importPrice,idImportNote8,idItem8)"
                    + " VALUES(?,?,?,?)",
                line.getQuantity(),
                line.getImportPrice(),
                id,
                item.getId());
            execute(
                c,
                "UPDATE tblItem8 SET stockQuantity=stockQuantity+?,updatedAt=CURRENT_TIMESTAMP"
                    + " WHERE id=?",
                line.getQuantity(),
                item.getId());
          }
          return id;
        });
  }

  public List<Map<String, Object>> list() throws SQLException {
    return query(
        "SELECT n.*,s.name,COALESCE((SELECT SUM(quantity*importPrice) FROM tblImportDetail8 WHERE"
            + " idImportNote8=n.id),0) AS totalCost FROM tblImportNote8 n JOIN tblSupplier8 s ON"
            + " s.id=n.idSupplier8 ORDER BY n.id DESC");
  }

  public List<Map<String, Object>> details(int id) throws SQLException {
    return query(
        "SELECT i.code,i.name,d.quantity,d.importPrice,d.quantity*d.importPrice AS amount FROM"
            + " tblImportDetail8 d JOIN tblItem8 i ON i.id=d.idItem8 WHERE d.idImportNote8=?",
        id);
  }
}
