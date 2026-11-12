package web;

import dao.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.math.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import model.*;

public class AppServlet8 extends HttpServlet {
  private String text(HttpServletRequest q, String key) {
    return Optional.ofNullable(q.getParameter(key)).orElse("").trim();
  }

  private int number(HttpServletRequest q, String key) {
    return Integer.parseInt(text(q, key));
  }

  private void role(User8 u, String... allowed) {
    if (u == null || !Set.of(allowed).contains(u.getRole())) throw new SecurityException();
  }

  private Cart8 cart(HttpSession s) {
    Cart8 c = (Cart8) s.getAttribute("cart");
    if (c == null) {
      c = new Cart8();
      s.setAttribute("cart", c);
    }
    return c;
  }

  private void redirect(HttpServletRequest q, HttpServletResponse p, String path)
      throws IOException {
    p.sendRedirect(q.getContextPath() + "/app/" + path);
  }

  private void flash(HttpSession s, String m) {
    s.setAttribute("flash", m);
  }

  @Override
  protected void service(HttpServletRequest q, HttpServletResponse p)
      throws ServletException, IOException {
    q.setCharacterEncoding("UTF-8");
    p.setCharacterEncoding("UTF-8");
    p.setHeader("X-Content-Type-Options", "nosniff");
    p.setHeader("X-Frame-Options", "DENY");
    p.setHeader("Cache-Control", "no-store");
    p.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; style-src 'self'; script-src 'self'; form-action 'self';"
            + " frame-ancestors 'none'");
    if (!Set.of("GET", "POST").contains(q.getMethod())) {
      p.sendError(405);
      return;
    }
    HttpSession s = q.getSession();
    if (s.getAttribute("csrf") == null) s.setAttribute("csrf", UUID.randomUUID().toString());
    User8 u = (User8) s.getAttribute("user");
    String path = Optional.ofNullable(q.getPathInfo()).orElse("/home").substring(1);
    if (u == null && !Set.of("login", "register").contains(path)) {
      redirect(q, p, "login");
      return;
    }
    try {
      if (q.getMethod().equals("POST")) {
        if (!s.getAttribute("csrf").equals(q.getParameter("csrf"))) {
          p.sendError(403);
          return;
        }
        synchronized (s) {
          post(path, q, p, s, u);
        }
        return;
      }
      switch (path) {
        case "login", "register" -> view(q, p, path.equals("login") ? "Login8" : "Register8");
        case "home" -> {
          q.setAttribute("items", new ItemDAO8().searchItem(""));
          view(q, p, "Home8");
        }
        case "items" -> {
          role(u, "CUSTOMER", "WAREHOUSE", "SALES");
          q.setAttribute("items", new ItemDAO8().searchItem(text(q, "keyword")));
          view(q, p, "Shopping8");
        }
        case "item" -> {
          role(u, "CUSTOMER", "WAREHOUSE", "SALES");
          Item8 item = new ItemDAO8().getItemById(number(q, "id"));
          if (item == null) {
            p.sendError(404);
            return;
          }
          q.setAttribute("item", item);
          view(q, p, "ItemDetail8");
        }
        case "edit" -> {
          role(u, "WAREHOUSE");
          Item8 item =
              text(q, "id").isEmpty() ? new Item8() : new ItemDAO8().getItemById(number(q, "id"));
          DAO8.require(item != null, "Không tìm thấy mặt hàng");
          q.setAttribute("item", item);
          view(q, p, "EditItem8");
        }
        case "cart" -> {
          role(u, "CUSTOMER", "SALES");
          cart(s);
          if (u.getRole().equals("SALES"))
            q.setAttribute(
                "customers",
                DAO8.query(
                    "SELECT id,fullName,phone FROM tblUser8 WHERE role='CUSTOMER' ORDER BY id"));
          view(q, p, "Cart8");
        }
        case "orders" -> {
          role(u, "CUSTOMER", "WAREHOUSE");
          q.setAttribute(
              "rows",
              new OnlineOrderDAO8().list(u.getRole().equals("CUSTOMER") ? u.getId() : null));
          q.setAttribute(
              "delivery",
              DAO8.query("SELECT id,fullName FROM tblUser8 WHERE role='DELIVERY' ORDER BY id"));
          if (!text(q, "id").isEmpty())
            q.setAttribute(
                "details",
                new OnlineOrderDAO8()
                    .details(number(q, "id"), u.getRole().equals("CUSTOMER") ? u.getId() : null));
          view(q, p, "Orders8");
        }
        case "suppliers" -> {
          role(u, "WAREHOUSE");
          q.setAttribute("rows", new SupplierDAO8().list());
          view(q, p, "Suppliers8");
        }
        case "imports" -> {
          role(u, "WAREHOUSE");
          q.setAttribute("items", new ItemDAO8().searchItem(""));
          q.setAttribute("suppliers", new SupplierDAO8().list());
          q.setAttribute("rows", new ImportNoteDAO8().list());
          if (!text(q, "id").isEmpty())
            q.setAttribute("details", new ImportNoteDAO8().details(number(q, "id")));
          view(q, p, "Imports8");
        }
        case "invoices" -> {
          role(u, "SALES");
          q.setAttribute("rows", new InvoiceDAO8().list());
          if (!text(q, "id").isEmpty())
            q.setAttribute("details", new InvoiceDAO8().details(number(q, "id")));
          view(q, p, "Invoices8");
        }
        case "statistics" -> {
          role(u, "MANAGER");
          LocalDate start =
              text(q, "start").isEmpty()
                  ? LocalDate.now().withDayOfMonth(1)
                  : LocalDate.parse(text(q, "start"));
          LocalDate end =
              text(q, "end").isEmpty() ? LocalDate.now() : LocalDate.parse(text(q, "end"));
          q.setAttribute("start", start);
          q.setAttribute("end", end);
          q.setAttribute("report", new StatisticDAO8().report(start, end));
          view(q, p, "Statistics8");
        }
        default -> p.sendError(404);
      }
    } catch (SecurityException e) {
      p.sendError(403);
    } catch (IllegalArgumentException | DateTimeException e) {
      flash(s, e.getMessage());
      redirect(q, p, safePage(path));
    } catch (SQLException e) {
      log("Database operation failed", e);
      flash(
          s,
          "Không thể ghi/đọc dữ liệu. Kiểm tra dữ liệu trùng, chứng từ tham chiếu hoặc kết nối"
              + " CSDL.");
      redirect(q, p, safePage(path));
    }
  }

  private String safePage(String path) {
    return switch (path) {
      case "login", "register", "suppliers", "imports", "orders", "invoices", "cart" -> path;
      case "checkout", "cart-add", "cart-update", "cart-remove", "cart-refresh" -> "cart";
      case "statistics" -> "home";
      default -> "items";
    };
  }

  private void view(HttpServletRequest q, HttpServletResponse p, String name)
      throws ServletException, IOException {
    q.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(q, p);
  }

  private void post(
      String path, HttpServletRequest q, HttpServletResponse p, HttpSession s, User8 u)
      throws SQLException, IOException {
    switch (path) {
      case "login" -> {
        Long blocked = (Long) s.getAttribute("blockedUntil");
        if (blocked != null && blocked > System.currentTimeMillis()) {
          flash(s, "Thử lại sau một phút");
          redirect(q, p, "login");
          return;
        }
        User8 user = new UserDAO8().login(text(q, "username"), text(q, "password"));
        if (user == null) {
          int attempts = Optional.ofNullable((Integer) s.getAttribute("attempts")).orElse(0) + 1;
          s.setAttribute("attempts", attempts);
          if (attempts >= 5) {
            s.setAttribute("blockedUntil", System.currentTimeMillis() + 60000);
            s.setAttribute("attempts", 0);
          }
          flash(s, "Tên đăng nhập hoặc mật khẩu không đúng");
          redirect(q, p, "login");
          return;
        }
        q.changeSessionId();
        s.setAttribute("user", user);
        s.removeAttribute("attempts");
        s.removeAttribute("blockedUntil");
        s.setAttribute("csrf", UUID.randomUUID().toString());
        redirect(q, p, "home");
      }
      case "register" -> {
        new UserDAO8()
            .register(
                text(q, "username"),
                text(q, "password"),
                text(q, "fullName"),
                text(q, "phone"),
                text(q, "email"),
                text(q, "address"));
        flash(s, "Đăng ký thành công. Hãy đăng nhập.");
        redirect(q, p, "login");
      }
      case "logout" -> {
        s.invalidate();
        redirect(q, p, "login");
      }
      case "item-save" -> {
        role(u, "WAREHOUSE");
        Item8 i = new Item8();
        i.setId(number(q, "id"));
        i.setCode(text(q, "code"));
        i.setName(text(q, "name"));
        i.setManufacturer(text(q, "manufacturer"));
        i.setSpecs(text(q, "specs"));
        i.setPrice(new BigDecimal(text(q, "price")));
        i.setWarrantyPeriod(number(q, "warrantyPeriod"));
        i.setUnit(text(q, "unit"));
        if (i.getId() == 0) new ItemDAO8().addItem(i);
        else DAO8.require(new ItemDAO8().updateItem(i), "Không tìm thấy mặt hàng");
        flash(s, "Đã lưu mặt hàng");
        redirect(q, p, "items");
      }
      case "item-delete" -> {
        role(u, "WAREHOUSE");
        new ItemDAO8().deleteItem(number(q, "id"));
        flash(s, "Đã xóa mặt hàng");
        redirect(q, p, "items");
      }
      case "cart-add", "cart-update" -> {
        role(u, "CUSTOMER", "SALES");
        var c = cart(s);
        int id = number(q, "id");
        int qty = number(q, "quantity");
        DAO8.require(qty > 0 && qty <= 100000, "Số lượng phải dương");
        if (path.equals("cart-add")) qty = Math.addExact(c.quantity(id), qty);
        Item8 item = new ItemDAO8().getItemById(id);
        DAO8.require(item != null && item.getStockQuantity() >= qty, "Không đủ tồn kho");
        c.set(item, qty);
        flash(s, "Đã cập nhật giỏ");
        redirect(q, p, "cart");
      }
      case "cart-remove" -> {
        role(u, "CUSTOMER", "SALES");
        var c = cart(s);
        int id = number(q, "id");
        for (var line : c.getLines()) if (line.getItem().getId() == id) c.set(line.getItem(), 0);
        redirect(q, p, "cart");
      }
      case "cart-refresh" -> {
        role(u, "CUSTOMER", "SALES");
        var c = cart(s);
        List<Item8> fresh = new ArrayList<>();
        for (var l : c.getLines()) {
          var item = new ItemDAO8().getItemById(l.getItem().getId());
          DAO8.require(item != null, "Mặt hàng không còn, hãy xóa khỏi giỏ");
          fresh.add(item);
        }
        for (var item : fresh) c.set(item, c.quantity(item.getId()));
        flash(s, "Đã tải giá hiện tại. Kiểm tra tổng tiền rồi xác nhận lại.");
        redirect(q, p, "cart");
      }
      case "checkout" -> {
        role(u, "CUSTOMER", "SALES");
        Cart8 c = cart(s);
        if (u.getRole().equals("CUSTOMER")) {
          OnlineOrder8 o = new OnlineOrder8();
          Customer8 customer = new Customer8();
          customer.setId(u.getId());
          o.setCustomer(customer);
          o.setDeliveryAddress(text(q, "deliveryAddress"));
          o.setPaymentMethod(text(q, "paymentMethod"));
          o.setOrderItems(c.getLines());
          new OnlineOrderDAO8().saveOrder(o);
          c.clear();
          flash(s, "Đã đặt đơn #" + o.getId());
          redirect(q, p, "orders");
        } else {
          Integer customer = text(q, "customer").isEmpty() ? null : number(q, "customer");
          int id =
              new InvoiceDAO8().sell(u.getId(), customer, text(q, "paymentMethod"), c.getLines());
          c.clear();
          flash(s, "Đã lập hóa đơn #" + id);
          redirect(q, p, "invoices?id=" + id);
        }
      }
      case "order-advance" -> {
        role(u, "WAREHOUSE");
        new OnlineOrderDAO8()
            .advance(
                number(q, "id"),
                text(q, "status"),
                u.getId(),
                text(q, "delivery").isEmpty() ? 0 : number(q, "delivery"));
        flash(s, "Đã chuyển trạng thái đơn");
        redirect(q, p, "orders");
      }
      case "supplier-save" -> {
        role(u, "WAREHOUSE");
        new SupplierDAO8()
            .save(
                number(q, "id"),
                text(q, "name"),
                text(q, "phone"),
                text(q, "email"),
                text(q, "address"),
                text(q, "description"));
        flash(s, "Đã lưu nhà cung cấp");
        redirect(q, p, "suppliers");
      }
      case "supplier-delete" -> {
        role(u, "WAREHOUSE");
        new SupplierDAO8().delete(number(q, "id"));
        flash(s, "Đã xóa nhà cung cấp");
        redirect(q, p, "suppliers");
      }
      case "import-save" -> {
        role(u, "WAREHOUSE");
        List<ImportDetail8> lines = new ArrayList<>();
        for (var item : new ItemDAO8().searchItem("")) {
          String raw = text(q, "qty_" + item.getId());
          if (raw.isEmpty() || raw.equals("0")) continue;
          ImportDetail8 d = new ImportDetail8();
          d.setItem(item);
          d.setQuantity(Integer.parseInt(raw));
          d.setImportPrice(new BigDecimal(text(q, "price_" + item.getId())));
          lines.add(d);
        }
        int id =
            new ImportNoteDAO8().save(u.getId(), number(q, "supplier"), text(q, "note"), lines);
        flash(s, "Đã nhập kho theo phiếu #" + id);
        redirect(q, p, "imports?id=" + id);
      }
      default -> throw new IllegalArgumentException("Thao tác không hợp lệ");
    }
  }
}
