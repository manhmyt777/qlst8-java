package dao;

import java.sql.*;
import java.util.*;

public class DAO8 {
  public static String setting(String key, String fallback) {
    return System.getProperty(key, System.getenv().getOrDefault(key, fallback));
  }

  public static Connection open() throws SQLException {
    return DriverManager.getConnection(
        setting(
            "QLST_DB_URL",
            "jdbc:h2:file:./data/qlst8;MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_ON_EXIT=FALSE"),
        setting("QLST_DB_USER", "sa"),
        setting("QLST_DB_PASSWORD", ""));
  }

  @FunctionalInterface
  public interface Work8<T> {
    T run(Connection c) throws SQLException;
  }

  public static <T> T transaction(Work8<T> work) throws SQLException {
    try (Connection c = open()) {
      c.setAutoCommit(false);
      try {
        T value = work.run(c);
        c.commit();
        return value;
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  public static PreparedStatement statement(Connection c, String sql, Object... args)
      throws SQLException {
    PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
    return p;
  }

  public static int execute(Connection c, String sql, Object... args) throws SQLException {
    try (var p = statement(c, sql, args)) {
      return p.executeUpdate();
    }
  }

  public static int insert(Connection c, String sql, Object... args) throws SQLException {
    try (var p = statement(c, sql, args)) {
      p.executeUpdate();
      try (var rs = p.getGeneratedKeys()) {
        if (!rs.next()) throw new SQLException("Không lấy được mã mới");
        return rs.getInt(1);
      }
    }
  }

  public static List<Map<String, Object>> query(Connection c, String sql, Object... args)
      throws SQLException {
    try (var p = statement(c, sql, args);
        var rs = p.executeQuery()) {
      List<Map<String, Object>> list = new ArrayList<>();
      var md = rs.getMetaData();
      while (rs.next()) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= md.getColumnCount(); i++)
          row.put(md.getColumnLabel(i), rs.getObject(i));
        list.add(row);
      }
      return list;
    }
  }

  public static List<Map<String, Object>> query(String sql, Object... args) throws SQLException {
    try (var c = open()) {
      return query(c, sql, args);
    }
  }

  public static void require(boolean condition, String message) {
    if (!condition) throw new IllegalArgumentException(message);
  }
}
