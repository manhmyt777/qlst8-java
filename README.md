# QLST8 — Quản lý siêu thị điện máy

Ứng dụng Java Web cho đề 08, dựa trên bộ phân tích và thiết kế đã chốt. Java 17+, JSP, Servlet, JDBC; đóng gói WAR bằng Maven. Giao diện tiếng Việt, dùng được trên máy tính và màn hình nhỏ.

## Chạy ngay trên Windows

Máy cần JDK 17 trở lên và Internet ở lần chạy đầu. Không cần cài thủ công Maven, Tomcat, MySQL hay các file JAR để chạy demo.

Mở PowerShell tại thư mục chứa README này:

```powershell
.
un.cmd
```

Mở **http://127.0.0.1:8080/app/login**. Dừng bằng Ctrl+C trong cửa sổ đang chạy. Lần đầu script tải Maven chính thức và đối chiếu SHA-512; Maven tải các dependency được khai báo trong `pom.xml`. Các lần sau tận dụng bộ nhớ đệm `tools/`.

| Tài khoản demo | Vai trò | Chức năng |
|---|---|---|
| customer | Khách hàng | Tìm hàng, xem chi tiết, giỏ, đặt hàng, xem đơn của mình |
| warehouse | Nhân viên kho | Thêm/sửa/xóa hàng, nhà cung cấp, nhập kho, duyệt và bàn giao đơn |
| sales | Nhân viên bán hàng | Chọn hàng, giỏ bán tại quầy, lập và xem hóa đơn |
| manager | Quản lý | Thống kê mặt hàng, nhà cung cấp và doanh thu theo kỳ |
| delivery | Nhân viên giao hàng | Tác nhân nhận hàng; chưa có màn hình tác nghiệp riêng theo thiết kế |

Mật khẩu **chỉ dành cho dữ liệu mẫu**: `Demo@12345`. Có thể tự đăng ký tài khoản khách hàng mới. Mật khẩu lưu dưới dạng PBKDF2 có salt, không lưu bản rõ. Dữ liệu mặc định nằm trong `data/qlst8.mv.db`, được giữ lại khi khởi động lại và không đưa lên GitHub. Dữ liệu mẫu chỉ được tạo khi bảng người dùng đang rỗng.

Đổi cổng trước khi chạy:

```powershell
$env:QLST_PORT='8081'
.
un.cmd
```

## Build và kiểm thử

```powershell
.
un.cmd test
.
un.cmd build
```

Kết quả build: `target/qlst8.war`. Nếu đã cài Maven: `mvn test`, `mvn package`, `mvn compile exec:java`. Script riêng dùng repository cache trong `tools/repository`; lệnh Maven thông thường dùng cache mặc định của Maven.

14 kiểm thử tích hợp kiểm tra JDBC, rollback, cạnh tranh tồn kho, đăng ký/đăng nhập, thay đổi giá, chuyển trạng thái, tính doanh thu, CSRF, phân quyền và biên dịch/thực thi tất cả màn hình JSP trên Tomcat thật. Đọc thêm [kết quả kiểm thử](docs/TESTING.md).

## Chức năng đã triển khai

- Đăng nhập, đăng xuất, đăng ký thành viên, phân quyền phía máy chủ.
- Tìm mặt hàng theo tên; xem chi tiết, thêm, sửa, xóa có ràng buộc.
- Giỏ theo phiên: thêm cộng dồn lượng, sửa, xóa, cập nhật lại giá.
- Đặt trực tuyến nguyên tử: kiểm tra giá/tồn, lưu đơn và chi tiết, giảm lượng khả dụng; chỉ xóa giỏ sau commit.
- Nhà cung cấp: thêm, sửa, xóa khi chưa có phiếu tham chiếu.
- Nhập nhiều mặt hàng trong một phiếu; tăng kho cùng giao dịch.
- Bán tại quầy: khách thành viên hoặc khách vãng lai; lập hóa đơn và giảm kho cùng giao dịch.
- Đơn trực tuyến: Pending → Exported → Delivering → Completed, chọn nhân viên giao khi bàn giao; không trừ kho lần hai.
- Thống kê mặt hàng, lượng/giá trị nhập theo nhà cung cấp, doanh thu theo ngày lập chứng từ. Đơn trực tuyến chỉ được cộng khi hiện ở Completed.

## Cấu trúc mã nguồn

```text
src/main/java/
  model/   18 thực thể/báo cáo theo mô hình và Cart8
  dao/     JDBC, truy vấn tham số, giao dịch
  web/     AppServlet8: định tuyến, phân quyền, CSRF, điều phối
  app/     Khởi chạy Tomcat, khởi tạo dữ liệu demo
  util/    Mã hóa mật khẩu, escape HTML, định dạng tiền
src/main/webapp/
  WEB-INF/views/   JSP theo nghiệp vụ, không truy cập trực tiếp
  assets/         CSS
src/main/resources/schema.sql  15 bảng
src/test/java/integration/     Kiểm thử CSDL và HTTP
 database/mysql.sql            DDL MySQL
```

Chương 5 của bài giảng tổ chức entity trong `model`, truy cập CSDL trong `dao`, giao diện trong JSP, dùng JDBC và giao dịch. Dự án giữ cách chia này; Maven thay việc copy JAR thủ công vào WEB-INF/lib. Servlet được thêm để tập trung xử lý, kiểm tra quyền và tránh đặt SQL/xử lý ghi trong JSP. Đọc [đối chiếu thiết kế](docs/DESIGN.md) trước khi đối chiếu sequence trong báo cáo: đây là một điều chỉnh triển khai cần phản ánh trên sơ đồ.

## Chạy với MySQL

Hướng dẫn cụ thể tại [docs/MYSQL.md](docs/MYSQL.md). Demo và 14 kiểm thử đã chạy với H2 ở chế độ tương thích MySQL. Chưa kiểm thử trực tiếp trên MySQL Server trong môi trường hiện tại.

## GitHub

Repository: https://github.com/manhmyt777/qlst8-java

Hướng dẫn cấu hình, commit và push tại [docs/GITHUB.md](docs/GITHUB.md). Chỉ đẩy thư mục dự án này, không thêm các bài nộp/tài liệu ở thư mục cha. `.gitignore` loại dữ liệu cục bộ, công cụ tải về, mật khẩu trong `.env` và sản phẩm build.

## Giới hạn phạm vi

Đây là ứng dụng phục vụ học tập, chưa đánh giá để triển khai công khai. Chế độ chạy nhúng chỉ lắng nghe localhost. Không có cổng thanh toán, hoàn/hủy đơn, tích hợp vận chuyển, quản trị tài khoản nhân viên, phục hồi mật khẩu hay lịch sử giá; các phần này chưa có quy tắc đầy đủ trong bộ thiết kế. Nhân viên kho xác nhận các mốc giao hàng. Bản ghi phương thức thanh toán không chứng minh đã nhận tiền. Không tự suy ra giảm giá từ điểm thành viên. Tên lớp và bảng dùng hậu tố 8; thuộc tính giữ camelCase.

Không đưa tài liệu môn học gốc hoặc thông tin nhận dạng sinh viên vào repository này.
