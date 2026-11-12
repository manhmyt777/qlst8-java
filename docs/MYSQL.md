# Chạy với MySQL

Chế độ mặc định H2 phục vụ chạy nhanh và kiểm thử. Để dùng MySQL, cần MySQL Server 8+ và database riêng. DDL dành cho database mới, không chạy lặp trên database đang có bảng/dữ liệu. Chưa có cơ chế migrate dữ liệu H2 sang MySQL tự động.

1. Mở MySQL Workbench, kết nối bằng tài khoản được phép tạo database, mở `database/mysql.sql` và chạy toàn bộ. Script tạo database qlst8 với utf8mb4 và 15 bảng.
2. Tạo tài khoản ứng dụng riêng trên database này theo cấu hình của máy. Ứng dụng chỉ cần SELECT, INSERT, UPDATE, DELETE sau khi đã tạo schema. Không ghi mật khẩu tài khoản vào repository.
3. Trong PowerShell, tại thư mục code, cấu hình biến môi trường cho phiên hiện tại:

```powershell
$env:QLST_DB_URL='jdbc:mysql://localhost:3306/qlst8?connectionTimeZone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8'
$env:QLST_DB_USER='qlst8_app'
$taskSecret=Read-Host 'Mat khau MySQL' -AsSecureString
$env:QLST_DB_PASSWORD=[System.Net.NetworkCredential]::new('', $taskSecret).Password
$env:QLST_SEED='true'
.
un.cmd
```

QLST_SEED=true tạo tài khoản và hàng mẫu chỉ khi bảng người dùng đang rỗng. Chỉ bật cho database học tập mới. Sau khi kiểm tra, đóng server, bỏ biến seed và chạy lại. Với database dùng thật cần quy trình cấp tài khoản riêng; không dùng các tài khoản demo làm tài khoản thật.

```powershell
Remove-Item Env:QLST_SEED -ErrorAction SilentlyContinue
.
un.cmd
```

Trở về H2 ở phiên PowerShell này:

```powershell
Remove-Item Env:QLST_DB_URL,Env:QLST_DB_USER,Env:QLST_DB_PASSWORD,Env:QLST_SEED -ErrorAction SilentlyContinue
.
un.cmd
```

## Eclipse / Tomcat ngoài

Import → Existing Maven Projects → chọn thư mục code, chọn JDK 17+, cập nhật Maven. Build WAR rồi triển khai `target/qlst8.war` lên Tomcat 10.1. Biến môi trường DB phải nằm trong tiến trình khởi động Tomcat. Truy cập `/qlst8/app/login` nếu context path là /qlst8. Không dùng Tomcat 9 cho WAR này vì ứng dụng dùng Jakarta Servlet.

Maven đóng gói thư viện vào WAR. Main8 chỉ dùng cho chế độ Tomcat nhúng; Startup8 khởi tạo DB khi web context chạy, nên WAR triển khai độc lập vẫn hoạt động.

Tham chiếu: [Tomcat 10.1 documentation](https://tomcat.apache.org/tomcat-10.1-doc/index.html).
