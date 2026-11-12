package integration;

import static org.junit.jupiter.api.Assertions.*;

import app.Database8;
import dao.*;
import java.math.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import model.*;
import org.junit.jupiter.api.*;
import util.Password8;

class SystemTest8 {
  @BeforeEach
  void setup() throws Exception {
    System.setProperty(
        "QLST_DB_URL",
        "jdbc:h2:mem:t"
            + UUID.randomUUID()
            + ";MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1");
    System.setProperty("QLST_DB_USER", "sa");
    System.setProperty("QLST_DB_PASSWORD", "");
    Database8.initialize();
  }

  OnlineOrder8 order(int id, int qty) throws Exception {
    var o = new OnlineOrder8();
    Customer8 customer = new Customer8();
    customer.setId(5);
    o.setCustomer(customer);
    o.setDeliveryAddress("Địa chỉ thử nghiệm");
    o.setPaymentMethod("COD");
    o.getOrderItems().add(line(id, qty));
    return o;
  }

  OrderItem8 line(int id, int qty) throws Exception {
    var l = new OrderItem8();
    var i = new ItemDAO8().getItemById(id);
    l.setItem(i);
    l.setPrice(i.getPrice());
    l.setQuantity(qty);
    return l;
  }

  int stock(int id) throws Exception {
    return new ItemDAO8().getItemById(id).getStockQuantity();
  }

  int count(String table) throws Exception {
    return ((Number) DAO8.query("SELECT COUNT(*) AS n FROM " + table).get(0).get("n")).intValue();
  }

  @Test
  void loginAndRegistration() throws Exception {
    var dao = new UserDAO8();
    assertNotNull(dao.login("warehouse", "Demo@12345"));
    assertNull(dao.login("warehouse", "wrong"));
    assertNull(dao.login("' OR 1=1 --", "wrong"));
    int n = count("tblUser8");
    dao.register("newuser", "Password@123", "Người dùng", "0900000000", "n@example.test", "Hà Nội");
    assertNotNull(dao.login("newuser", "Password@123"));
    assertThrows(
        SQLException.class,
        () -> dao.register("newuser", "Password@123", "Trùng", "0900000000", "", ""));
    assertEquals(n + 1, count("tblUser8"));
    assertEquals(2, count("tblCustomer8"));
    assertTrue(Password8.verify("abc", Password8.hash("abc")));
  }

  @Test
  void cartDoesNotReserveAndOrderReservesOnce() throws Exception {
    Cart8 c = new Cart8();
    c.set(new ItemDAO8().getItemById(1), 2);
    assertEquals(10, stock(1));
    assertEquals(0, c.getTotal().compareTo(new BigDecimal("31300000")));
    var o = order(1, 2);
    assertTrue(new OnlineOrderDAO8().saveOrder(o));
    assertEquals(8, stock(1));
    assertEquals(1, count("tblOnlineOrder8"));
    assertEquals("Pending", o.getStatus());
  }

  @Test
  void partialReservationRollsBack() throws Exception {
    var o = order(1, 2);
    o.getOrderItems().add(line(2, 11));
    assertThrows(IllegalArgumentException.class, () -> new OnlineOrderDAO8().saveOrder(o));
    assertEquals(10, stock(1));
    assertEquals(10, stock(2));
    assertEquals(0, count("tblOnlineOrder8"));
    assertEquals(0, count("tblOrderItem8"));
  }

  @Test
  void priceChangeRejectsEntireOrder() throws Exception {
    var o = order(1, 2);
    try (var c = DAO8.open()) {
      DAO8.execute(c, "UPDATE tblItem8 SET price=price+1 WHERE id=1");
    }
    assertThrows(IllegalArgumentException.class, () -> new OnlineOrderDAO8().saveOrder(o));
    assertEquals(10, stock(1));
    assertEquals(0, count("tblOnlineOrder8"));
  }

  @Test
  void concurrentOrdersCannotOversell() throws Exception {
    var a = order(1, 7);
    var b = order(1, 7);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for (var o : List.of(a, b))
        results.add(
            pool.submit(
                () -> {
                  start.await();
                  try {
                    return new OnlineOrderDAO8().saveOrder(o);
                  } catch (IllegalArgumentException e) {
                    return false;
                  }
                }));
      start.countDown();
      int success = 0;
      for (var f : results) if (f.get(10, TimeUnit.SECONDS)) success++;
      assertEquals(1, success);
      assertEquals(3, stock(1));
      assertEquals(1, count("tblOnlineOrder8"));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void foreignKeyFailureRollsBackReservation() throws Exception {
    var o = order(1, 2);
    o.getCustomer().setId(9999);
    assertThrows(SQLException.class, () -> new OnlineOrderDAO8().saveOrder(o));
    assertEquals(10, stock(1));
    assertEquals(0, count("tblOnlineOrder8"));
  }

  @Test
  void itemUpdateCannotOverwriteStockOrCode() throws Exception {
    var dao = new ItemDAO8();
    var i = dao.getItemById(1);
    i.setStockQuantity(999);
    i.setCode("TAMPERED");
    i.setName("Tên mới");
    assertTrue(dao.updateItem(i));
    assertEquals(10, stock(1));
    assertEquals("MH01", dao.getItemById(1).getCode());
    assertThrows(IllegalArgumentException.class, () -> dao.deleteItem(1));
    assertTrue(dao.searchItem("Tên mới").size() == 1);
  }

  @Test
  void invoicesAreAtomic() throws Exception {
    var dao = new InvoiceDAO8();
    assertThrows(
        IllegalArgumentException.class,
        () -> dao.sell(2, null, "CASH", List.of(line(1, 2), line(2, 99))));
    assertEquals(0, count("tblInvoice8"));
    assertEquals(10, stock(1));
    int id = dao.sell(2, null, "CASH", List.of(line(1, 2)));
    assertTrue(id > 0);
    assertEquals(8, stock(1));
    assertEquals(1, dao.details(id).size());
  }

  @Test
  void importsAreAtomicAndAggregateQuantity() throws Exception {
    var a = new ImportDetail8();
    a.setItem(new ItemDAO8().getItemById(1));
    a.setQuantity(4);
    a.setImportPrice(new BigDecimal("100"));
    var b = new ImportDetail8();
    b.setItem(new ItemDAO8().getItemById(2));
    b.setQuantity(-1);
    b.setImportPrice(BigDecimal.TEN);
    var dao = new ImportNoteDAO8();
    assertThrows(IllegalArgumentException.class, () -> dao.save(1, 1, "test", List.of(a, b)));
    assertEquals(10, stock(1));
    assertEquals(0, count("tblImportNote8"));
    dao.save(1, 1, "test", List.of(a));
    assertEquals(14, stock(1));
    var report = new StatisticDAO8().report(LocalDate.now(), LocalDate.now());
    var suppliers = (List<Map<String, Object>>) report.get("suppliers");
    assertEquals(4, ((Number) suppliers.get(0).get("totalImportedQuantity")).intValue());
  }

  @Test
  void statusTransitionsAndRevenuePolicy() throws Exception {
    var o = order(1, 1);
    var dao = new OnlineOrderDAO8();
    dao.saveOrder(o);
    var stats = new StatisticDAO8();
    assertEquals(
        0, ((BigDecimal) stats.report(LocalDate.now(), LocalDate.now()).get("total")).signum());
    dao.advance(o.getId(), "Pending", 1, 4);
    assertThrows(IllegalArgumentException.class, () -> dao.advance(o.getId(), "Pending", 1, 4));
    dao.advance(o.getId(), "Exported", 1, 0);
    dao.advance(o.getId(), "Delivering", 1, 0);
    assertEquals(9, stock(1));
    assertEquals(
        0,
        new BigDecimal("15650000")
            .compareTo((BigDecimal) stats.report(LocalDate.now(), LocalDate.now()).get("total")));
  }
}
