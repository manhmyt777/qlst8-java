# Kiểm chứng bản cài đặt

Môi trường kiểm tra: Windows, Java 24, biên dịch target Java 17, Maven 3.9.11; Tomcat 10.1.61; H2 2.3.232 trong chế độ tương thích MySQL.

Lệnh tái lập: `run.cmd test`. Bộ kiểm thử dùng H2 in-memory riêng, không sửa data/qlst8 của bản demo. Các test mở Tomcat trên cổng ngẫu nhiên rồi tắt sau khi xong.

## Kết quả

14 test, 0 failure, 0 error, 0 skipped.

| Nhóm | Điều được kiểm tra |
|---|---|
| Danh tính | Đăng ký, trùng username không tạo bản ghi con dở, đăng nhập đúng/sai, SQL injection ở username, PBKDF2 |
| Giỏ/đặt hàng | Giỏ chưa giảm tồn, đặt thành công giảm đúng lượng, tính tổng BigDecimal |
| Rollback | Dòng sau thiếu hàng, FK khách không tồn tại: không có đơn/chi tiết dở và hoàn nguyên tồn |
| Cạnh tranh | Hai connection cùng mua 7 khi tồn 10: chỉ một đơn được commit, tồn cuối bằng 3 |
| Giá | Giá thay đổi làm từ chối cả đơn |
| Mặt hàng | Sửa thông tin không ghi đè tồn/mã, tìm tên, không xóa hàng còn tồn |
| Bán hàng | Hóa đơn nhiều dòng lỗi rollback; bán hợp lệ tạo hóa đơn/chi tiết và giảm tồn |
| Nhập | Phiếu nhiều dòng lỗi rollback; nhập hợp lệ tăng tồn, thống kê cộng lượng nhập |
| Trạng thái | Chuyển tuần tự, chống duyệt lặp; không giảm kho lần hai; chỉ Completed tính doanh thu |
| Web | Các màn hình của 5 vai trò biên dịch/thực thi JSP với HTTP 200; tiếng Việt đúng UTF-8 |
| Quyền/CSRF | Khách không truy cập nhập kho, POST thiếu CSRF bị chặn, không truy cập trực tiếp JSP dưới WEB-INF |
| HTTP nhập/bán | Tạo nhà cung cấp và mặt hàng, nhập kho, lập hóa đơn tại quầy qua HTTP; đối chiếu tồn cuối |
| HTTP checkout | Giá đổi giữ giỏ, cập nhật giá rồi đặt thành công làm rỗng giỏ |

Báo cáo JUnit được tạo trong target/surefire-reports khi chạy test. Không đưa kết quả máy cục bộ vào Git.

## Giới hạn kiểm chứng

Chưa chạy trực tiếp trên MySQL Server. H2 MySQL mode không chứng minh toàn bộ hành vi khóa, collation hoặc DDL trên MySQL giống hệt. Công cụ trình duyệt của phiên làm việc hiện không khả dụng nên chưa có ảnh chụp xác nhận bố cục. Các trang đã được kiểm tra qua HTTP/Tomcat và nội dung UTF-8, chưa thay thế kiểm tra giao diện trực quan trên trình duyệt của người dùng.

## Thử thủ công

1. `run.cmd`, đăng nhập warehouse, tìm Samsung, sửa tên/giá/bảo hành; kiểm tra tồn và mã chỉ đọc.
2. Thêm hàng mới (tồn 0), thêm nhà cung cấp rồi nhập nhiều hàng trong một phiếu.
3. Đăng nhập customer ở cửa sổ trình duyệt khác, thêm cùng mặt hàng hai lần; sửa/xóa dòng giỏ.
4. Kho đổi giá khi khách đang có giỏ; khách đặt sẽ nhận yêu cầu cập nhật giá và vẫn giữ giỏ.
5. Khách cập nhật giá, nhập địa chỉ và đặt lại; đơn Pending, tồn giảm và giỏ trống.
6. Kho chọn nhân viên giao, duyệt/bàn giao, xác nhận đang giao, hoàn thành; kiểm tra tồn không giảm thêm.
7. Sales chọn hàng, lập hóa đơn cho khách vãng lai hoặc thành viên, xem chi tiết. Có thể in trang bằng Ctrl+P.
8. Manager chọn khoảng ngày, đối chiếu doanh thu hóa đơn + đơn Completed và lượng nhập theo nhà cung cấp.
9. Kiểm tra màn hình hẹp và tắt/mở server để xác nhận dữ liệu được giữ lại.
