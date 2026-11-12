package app;

import dao.*;
import java.nio.charset.StandardCharsets;
import util.Password8;

public class Database8 {
  public static void initialize() throws Exception {
    boolean demo = DAO8.setting("QLST_DB_URL", "jdbc:h2:").startsWith("jdbc:h2:");
    if (demo) {
      try (var c = DAO8.open()) {
        var tables = c.getMetaData().getTables(null, null, "tblUser8", null);
        if (!tables.next()) {
          String sql =
              new String(
                  Database8.class.getResourceAsStream("/schema.sql").readAllBytes(),
                  StandardCharsets.UTF_8);
          for (String statement : sql.split(";"))
            if (!statement.isBlank()) DAO8.execute(c, statement);
        }
      }
    }
    if (demo || DAO8.setting("QLST_SEED", "false").equals("true")) seed();
  }

  public static void seed() throws Exception {
    DAO8.transaction(
        c -> {
          if (!DAO8.query(c, "SELECT id FROM tblUser8").isEmpty()) return false;
          String pass = Password8.hash("Demo@12345");
          String[] roles = {"WAREHOUSE", "SALES", "MANAGER", "DELIVERY", "CUSTOMER"};
          String[] names = {"warehouse", "sales", "manager", "delivery", "customer"};
          for (int i = 0; i < roles.length; i++) {
            int id =
                DAO8.insert(
                    c,
                    "INSERT INTO tblUser8(username,password,fullName,phone,email,address,role)"
                        + " VALUES(?,?,?,?,?,?,?)",
                    names[i],
                    pass,
                    "Tài khoản mẫu " + names[i],
                    "0900000000",
                    names[i] + "@example.test",
                    "Hà Nội",
                    roles[i]);
            if (i == 4)
              DAO8.execute(
                  c,
                  "INSERT INTO tblCustomer8(id,memberCard,loyaltyPoints) VALUES(?,?,0)",
                  id,
                  "MEM8-" + id);
            else {
              DAO8.execute(
                  c, "INSERT INTO tblStaff8(id,position,salaryRate) VALUES(?,?,1)", id, roles[i]);
              String table =
                  new String[] {
                        "tblWarehouseStaff8", "tblSalesStaff8", "tblManager8", "tblDeliveryStaff8"
                      }
                      [i];
              if (i == 0)
                DAO8.execute(
                    c, "INSERT INTO " + table + "(id,warehouseArea) VALUES(?,'Kho A')", id);
              else DAO8.execute(c, "INSERT INTO " + table + "(id) VALUES(?)", id);
            }
          }
          DAO8.execute(
              c,
              "INSERT INTO tblSupplier8(name,phone,email,address,description) VALUES('Nhà cung cấp"
                  + " mẫu','0900000001','supplier@example.test','Hà Nội','Dữ liệu minh họa')");
          String[] products = {
            "Tivi Samsung 55 inch",
            "Tủ lạnh Panasonic 300L",
            "Máy giặt LG 9kg",
            "Điều hòa Daikin 12000BTU",
            "Laptop Dell 15 inch",
            "Lò vi sóng Sharp 23L"
          };
          String[] brands = {"Samsung", "Panasonic", "LG", "Daikin", "Dell", "Sharp"};
          int[] prices = {15650000, 9800000, 7200000, 11500000, 18900000, 2100000};
          for (int i = 0; i < products.length; i++)
            DAO8.execute(
                c,
                "INSERT INTO"
                    + " tblItem8(code,name,manufacturer,specs,price,stockQuantity,warrantyPeriod,unit,updatedAt)"
                    + " VALUES(?,?,?,?,?,10,24,'Chiếc',CURRENT_TIMESTAMP)",
                "MH0" + (i + 1),
                products[i],
                brands[i],
                "Thông số minh họa cho bài tập",
                prices[i]);
          return true;
        });
  }
}
