package dao;

import java.sql.*;
import java.util.*;

public class SupplierDAO8 extends DAO8 {
  public List<Map<String, Object>> list() throws SQLException {
    return query("SELECT * FROM tblSupplier8 ORDER BY id DESC");
  }

  public void save(
      int id, String name, String phone, String email, String address, String description)
      throws SQLException {
    require(!name.isBlank() && name.length() <= 255, "Tên nhà cung cấp không hợp lệ");
    require(
        phone.length() <= 20
            && email.length() <= 255
            && address.length() <= 255
            && description.length() <= 500,
        "Thông tin quá dài");
    try (var c = open()) {
      if (id == 0)
        insert(
            c,
            "INSERT INTO tblSupplier8(name,phone,email,address,description) VALUES(?,?,?,?,?)",
            name,
            phone,
            email,
            address,
            description);
      else
        require(
            execute(
                    c,
                    "UPDATE tblSupplier8 SET name=?,phone=?,email=?,address=?,description=? WHERE"
                        + " id=?",
                    name,
                    phone,
                    email,
                    address,
                    description,
                    id)
                == 1,
            "Không tìm thấy nhà cung cấp");
    }
  }

  public void delete(int id) throws SQLException {
    try (var c = open()) {
      require(
          execute(c, "DELETE FROM tblSupplier8 WHERE id=?", id) == 1,
          "Không tìm thấy nhà cung cấp");
    }
  }
}
