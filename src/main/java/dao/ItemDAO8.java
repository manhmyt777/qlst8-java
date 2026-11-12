package dao;

import java.sql.*;
import java.util.*;
import model.Item8;

public class ItemDAO8 extends DAO8 {
  public static Item8 map(ResultSet r) throws SQLException {
    Item8 i = new Item8();
    i.setId(r.getInt("id"));
    i.setCode(r.getString("code"));
    i.setName(r.getString("name"));
    i.setManufacturer(r.getString("manufacturer"));
    i.setSpecs(r.getString("specs"));
    i.setPrice(r.getBigDecimal("price"));
    i.setStockQuantity(r.getInt("stockQuantity"));
    i.setWarrantyPeriod(r.getInt("warrantyPeriod"));
    i.setUnit(r.getString("unit"));
    var t = r.getTimestamp("updatedAt");
    i.setUpdatedAt(t == null ? null : t.toLocalDateTime());
    return i;
  }

  public List<Item8> searchItem(String keyword) throws SQLException {
    try (var c = open();
        var p =
            statement(
                c,
                "SELECT * FROM tblItem8 WHERE LOWER(name) LIKE ? ESCAPE '!' ORDER BY id",
                "%"
                    + keyword
                        .toLowerCase(Locale.ROOT)
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_")
                    + "%");
        var rs = p.executeQuery()) {
      List<Item8> a = new ArrayList<>();
      while (rs.next()) a.add(map(rs));
      return a;
    }
  }

  public Item8 getItemById(int id) throws SQLException {
    try (var c = open()) {
      return get(c, id, false);
    }
  }

  public static Item8 get(Connection c, int id, boolean lock) throws SQLException {
    try (var p =
            statement(c, "SELECT * FROM tblItem8 WHERE id=?" + (lock ? " FOR UPDATE" : ""), id);
        var r = p.executeQuery()) {
      return r.next() ? map(r) : null;
    }
  }

  public boolean checkStock(int id, int quantity) throws SQLException {
    Item8 i = getItemById(id);
    return i != null && quantity > 0 && i.getStockQuantity() >= quantity;
  }

  private void validate(Item8 i) {
    require(!i.getName().isBlank() && i.getName().length() <= 255, "Tên mặt hàng không hợp lệ");
    require(
        i.getPrice().signum() > 0 && i.getPrice().precision() <= 15 && i.getPrice().scale() <= 2,
        "Giá phải dương, tối đa 13 chữ số và 2 chữ số thập phân");
    require(i.getWarrantyPeriod() >= 0 && i.getWarrantyPeriod() <= 1200, "Bảo hành không hợp lệ");
    require(
        i.getSpecs().length() <= 500
            && i.getManufacturer().length() <= 255
            && i.getUnit().length() <= 255,
        "Thông tin quá dài");
  }

  public boolean updateItem(Item8 i) throws SQLException {
    validate(i);
    try (var c = open()) {
      return execute(
              c,
              "UPDATE tblItem8 SET"
                  + " name=?,manufacturer=?,specs=?,price=?,warrantyPeriod=?,unit=?,updatedAt=CURRENT_TIMESTAMP"
                  + " WHERE id=?",
              i.getName(),
              i.getManufacturer(),
              i.getSpecs(),
              i.getPrice(),
              i.getWarrantyPeriod(),
              i.getUnit(),
              i.getId())
          == 1;
    }
  }

  public int addItem(Item8 i) throws SQLException {
    validate(i);
    require(i.getCode().matches("[A-Za-z0-9_-]{1,50}"), "Mã mặt hàng không hợp lệ");
    try (var c = open()) {
      return insert(
          c,
          "INSERT INTO"
              + " tblItem8(code,name,manufacturer,specs,price,stockQuantity,warrantyPeriod,unit,updatedAt)"
              + " VALUES(?,?,?,?,?,0,?,?,CURRENT_TIMESTAMP)",
          i.getCode(),
          i.getName(),
          i.getManufacturer(),
          i.getSpecs(),
          i.getPrice(),
          i.getWarrantyPeriod(),
          i.getUnit());
    }
  }

  public boolean deleteItem(int id) throws SQLException {
    return transaction(
        c -> {
          var i = get(c, id, true);
          require(
              i != null && i.getStockQuantity() == 0,
              "Chỉ xóa mặt hàng hết tồn kho và chưa có chứng từ");
          return execute(c, "DELETE FROM tblItem8 WHERE id=?", id) == 1;
        });
  }
}
