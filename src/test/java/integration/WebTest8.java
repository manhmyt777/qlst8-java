package integration;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;

class WebTest8 {
  static Tomcat tomcat;
  static String root;

  @BeforeAll
  static void start() throws Exception {
    System.setProperty(
        "QLST_DB_URL", "jdbc:h2:mem:web;MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1");
    System.setProperty("QLST_DB_USER", "sa");
    System.setProperty("QLST_DB_PASSWORD", "");
    tomcat = new Tomcat();
    tomcat.setBaseDir("target/test-tomcat");
    tomcat.setPort(0);
    tomcat.getConnector();
    var c = tomcat.addWebapp("", Path.of("src/main/webapp").toAbsolutePath().toString());
    c.setParentClassLoader(WebTest8.class.getClassLoader());
    tomcat.start();
    root = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort();
  }

  @AfterAll
  static void stop() throws Exception {
    tomcat.stop();
    tomcat.destroy();
  }

  HttpClient browser() {
    return HttpClient.newBuilder()
        .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
  }

  HttpResponse<String> get(HttpClient c, String path) throws Exception {
    return c.send(
        HttpRequest.newBuilder(URI.create(root + path)).GET().build(),
        HttpResponse.BodyHandlers.ofString());
  }

  HttpResponse<String> post(HttpClient c, String path, Map<String, String> data) throws Exception {
    StringJoiner body = new StringJoiner("&");
    data.forEach(
        (k, v) ->
            body.add(
                URLEncoder.encode(k, StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(v, StandardCharsets.UTF_8)));
    return c.send(
        HttpRequest.newBuilder(URI.create(root + path))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }

  String token(String html) {
    Matcher m = Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);
    assertTrue(m.find(), html.substring(0, Math.min(500, html.length())));
    return m.group(1);
  }

  HttpClient login(String user) throws Exception {
    var c = browser();
    var page = get(c, "/app/login");
    assertEquals(200, page.statusCode());
    var result =
        post(
            c,
            "/app/login",
            Map.of("csrf", token(page.body()), "username", user, "password", "Demo@12345"));
    assertEquals(302, result.statusCode());
    assertEquals("/app/home", result.headers().firstValue("location").orElse(""));
    return c;
  }

  @Test
  void rendersEveryRolePageAndJsp() throws Exception {
    Map<String, List<String>> pages =
        Map.of(
            "customer",
            List.of("home", "items", "item?id=1", "cart", "orders"),
            "warehouse",
            List.of(
                "home",
                "items",
                "item?id=1",
                "edit",
                "edit?id=1",
                "orders",
                "suppliers",
                "imports"),
            "sales",
            List.of("home", "items", "item?id=1", "cart", "invoices"),
            "manager",
            List.of("home", "statistics"),
            "delivery",
            List.of("home"));
    for (var role : pages.entrySet()) {
      var c = login(role.getKey());
      for (var path : role.getValue()) {
        var result = get(c, "/app/" + path);
        assertEquals(200, result.statusCode(), role.getKey() + " " + path + " " + result.body());
        assertFalse(result.body().contains("Exception"));
        assertFalse(result.body().contains("Ã"), "UTF-8: " + path);
      }
    }
    var guest = browser();
    assertEquals(200, get(guest, "/app/register").statusCode());
    assertEquals(302, get(guest, "/app/home").statusCode());
  }

  @Test
  void roleAndCsrfEnforced() throws Exception {
    var c = login("customer");
    assertEquals(403, get(c, "/app/imports").statusCode());
    assertEquals(403, post(c, "/app/checkout", Map.of()).statusCode());
    assertEquals(404, get(c, "/WEB-INF/views/Cart8.jsp").statusCode());
  }

  @Test
  void checkoutPreservesCartOnPriceChangeAndClearsAfterCommit() throws Exception {
    var c = login("customer");
    String csrf = token(get(c, "/app/item?id=1").body());
    assertEquals(
        302,
        post(c, "/app/cart-add", Map.of("csrf", csrf, "id", "1", "quantity", "2")).statusCode());
    try (var db = dao.DAO8.open()) {
      dao.DAO8.execute(db, "UPDATE tblItem8 SET price=price+1 WHERE id=1");
    }
    Map<String, String> order =
        Map.of("csrf", csrf, "deliveryAddress", "Hà Nội", "paymentMethod", "COD");
    assertEquals(302, post(c, "/app/checkout", order).statusCode());
    String failed = get(c, "/app/cart").body();
    assertTrue(failed.contains("Giá đã thay đổi"));
    assertTrue(failed.contains("MH01"));
    assertEquals(10, new dao.ItemDAO8().getItemById(1).getStockQuantity());
    post(c, "/app/cart-refresh", Map.of("csrf", csrf));
    assertEquals(302, post(c, "/app/checkout", order).statusCode());
    assertEquals(8, new dao.ItemDAO8().getItemById(1).getStockQuantity());
    String empty = get(c, "/app/cart").body();
    Files.writeString(Path.of("target/empty-cart.html"), empty);
    assertTrue(empty.contains("Giỏ hàng đang trống"), "See target/empty-cart.html");
    assertEquals(200, get(c, "/app/orders?id=1").statusCode());
  }

  @Test
  void warehouseImportAndCounterSaleThroughHttp() throws Exception {
    var warehouse = login("warehouse");
    String csrf = token(get(warehouse, "/app/home").body());
    assertEquals(
        302,
        post(
                warehouse,
                "/app/supplier-save",
                Map.of(
                    "csrf",
                    csrf,
                    "id",
                    "0",
                    "name",
                    "Nhà cung cấp E2E",
                    "phone",
                    "0900000001",
                    "email",
                    "e2e@example.test",
                    "address",
                    "Hà Nội",
                    "description",
                    "Kiểm thử"))
            .statusCode());
    assertEquals(
        302,
        post(
                warehouse,
                "/app/item-save",
                Map.of(
                    "csrf",
                    csrf,
                    "id",
                    "0",
                    "code",
                    "E2E01",
                    "name",
                    "Mặt hàng E2E",
                    "manufacturer",
                    "Demo",
                    "specs",
                    "Thử nghiệm",
                    "price",
                    "1000",
                    "warrantyPeriod",
                    "12",
                    "unit",
                    "Chiếc"))
            .statusCode());
    var item = new dao.ItemDAO8().searchItem("Mặt hàng E2E").get(0);
    int supplier =
        ((Number)
                dao.DAO8
                    .query("SELECT id FROM tblSupplier8 WHERE name=?", "Nhà cung cấp E2E")
                    .get(0)
                    .get("id"))
            .intValue();
    assertEquals(
        302,
        post(
                warehouse,
                "/app/import-save",
                Map.of(
                    "csrf",
                    csrf,
                    "supplier",
                    String.valueOf(supplier),
                    "note",
                    "Nhập E2E",
                    "qty_" + item.getId(),
                    "3",
                    "price_" + item.getId(),
                    "500"))
            .statusCode());
    assertEquals(3, new dao.ItemDAO8().getItemById(item.getId()).getStockQuantity());
    assertEquals(200, get(warehouse, "/app/imports?id=1").statusCode());
    var sales = login("sales");
    String salesToken = token(get(sales, "/app/home").body());
    post(
        sales,
        "/app/cart-add",
        Map.of("csrf", salesToken, "id", String.valueOf(item.getId()), "quantity", "1"));
    assertEquals(
        302,
        post(
                sales,
                "/app/checkout",
                Map.of("csrf", salesToken, "paymentMethod", "CASH", "customer", ""))
            .statusCode());
    assertEquals(2, new dao.ItemDAO8().getItemById(item.getId()).getStockQuantity());
    assertEquals(200, get(sales, "/app/invoices?id=1").statusCode());
    assertTrue(get(sales, "/app/cart").body().contains("Giỏ hàng đang trống"));
  }
}
