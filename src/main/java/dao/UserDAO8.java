package dao;

import java.sql.*;
import model.*;
import util.Password8;

public class UserDAO8 extends DAO8 {
  public User8 login(String username, String password) throws SQLException {
    try (var c = open();
        var p = statement(c, "SELECT * FROM tblUser8 WHERE username=?", username);
        var rs = p.executeQuery()) {
      if (!rs.next() || !Password8.verify(password, rs.getString("password"))) return null;
      User8 u =
          switch (rs.getString("role")) {
            case "WAREHOUSE" -> new WarehouseStaff8();
            case "SALES" -> new SalesStaff8();
            case "MANAGER" -> new Manager8();
            case "DELIVERY" -> new DeliveryStaff8();
            default -> new Customer8();
          };
      u.setId(rs.getInt("id"));
      u.setUsername(rs.getString("username"));
      u.setRole(rs.getString("role"));
      u.setFullName(rs.getString("fullName"));
      u.setPhone(rs.getString("phone"));
      u.setEmail(rs.getString("email"));
      u.setAddress(rs.getString("address"));
      return u;
    }
  }

  public int register(
      String username, String password, String name, String phone, String email, String address)
      throws SQLException {
    require(
        username.matches("[A-Za-z0-9_.-]{3,50}"),
        "Tên đăng nhập cần 3–50 ký tự chữ, số, _, . hoặc -");
    require(password.length() >= 10 && password.length() <= 128, "Mật khẩu cần 10–128 ký tự");
    require(!name.isBlank() && name.length() <= 255, "Họ tên không hợp lệ");
    require(phone.matches("[+0-9 .()-]{8,20}"), "Số điện thoại không hợp lệ");
    require(email.length() <= 255 && address.length() <= 255, "Thông tin quá dài");
    return transaction(
        c -> {
          int id =
              insert(
                  c,
                  "INSERT INTO tblUser8(username,password,fullName,phone,email,address,role)"
                      + " VALUES(?,?,?,?,?,?,?)",
                  username,
                  Password8.hash(password),
                  name,
                  phone,
                  email,
                  address,
                  "CUSTOMER");
          execute(
              c,
              "INSERT INTO tblCustomer8(id,memberCard,loyaltyPoints) VALUES(?,?,0)",
              id,
              "MEM8-" + id);
          return id;
        });
  }
}
