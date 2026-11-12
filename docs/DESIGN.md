# Đối chiếu mô hình và cài đặt

Nguồn: chương 5 của bài giảng được cung cấp (trang in 115 trở đi) và bộ mô hình đề 08 đã chốt. Không sao chép mã ví dụ đăng ký học vì khác miền nghiệp vụ.

## Kiến trúc

Trình duyệt → AppServlet8 → DAO8 và DAO nghiệp vụ → JDBC → H2 hoặc MySQL. Servlet chuyển dữ liệu cho JSP dưới WEB-INF/views để render HTML. Java model chứa dữ liệu và phép tính tổng; không tự truy vấn CSDL. Không dùng framework ORM hoặc Spring.

| Thiết kế | Cài đặt |
|---|---|
| DAO8 | Mỗi tác vụ mở connection riêng; try-with-resources đóng connection/statement/result set |
| ItemDAO8.searchItem/getItemById/checkStock/updateItem | Giữ tên và trách nhiệm, lỗi SQL được ném riêng với kết quả không tìm thấy |
| OnlineOrderDAO8.saveOrder(order): boolean | Giữ hợp đồng; cập nhật id vào order khi commit; lỗi SQL hoặc nghiệp vụ không bị nuốt |
| Shopping8, ItemDetail8, Cart8, EditItem8 | Các JSP tương ứng dưới WEB-INF/views |
| CustomerHome8/WarehouseHome8 | Gộp thành Home8.jsp; menu render theo vai trò |
| SearchItem8 | Gộp phần tìm vào Shopping8.jsp dùng chung |
| UpdateItem8/SaveOnlineOrder8 là trang xử lý | Chuyển sang POST /app/item-save và /app/checkout trong AppServlet8 |
| 18 lớp và 15 bảng | Giữ kế thừa thực thể, bảng con có PK đồng thời là FK của bảng cha; Cart8 và lớp hạ tầng là lớp bổ sung |
| Các lớp thống kê | Không tạo bảng; truy vấn tổng hợp trả dữ liệu báo cáo |

Các tên route/lớp bổ sung là quyết định triển khai. Nếu báo cáo phải khớp tuyệt đối mã chạy, cập nhật sequence: JSP gửi HTTP POST → Servlet kiểm tra quyền/dữ liệu → DAO thực hiện giao dịch → redirect → JSP thông báo.

## Nguyên tắc dữ liệu

- Tiền dùng BigDecimal và DECIMAL(15,2), không dùng float/double.
- DDL thêm AUTO_INCREMENT, NOT NULL và CHECK cần cho ứng dụng chạy; giữ nguyên 15 bảng, cột và quan hệ của mô hình. Không thêm bảng thống kê, tổng tiền, category hoặc phiếu xuất.
- Mã hàng duy nhất, tên hàng không bắt buộc duy nhất; giỏ dùng ID số nội bộ.
- Mặt hàng mới có tồn 0; tăng tồn qua phiếu nhập. Sửa thông tin không sửa mã hoặc tồn. Xóa chỉ khi tồn 0 và chưa có chứng từ tham chiếu.
- Mật khẩu trong tblUser8.password là chuỗi PBKDF2, không phải mật khẩu rõ. Dùng PreparedStatement; không chặn từ khóa theo chuỗi như ví dụ minh họa cũ.
- Giỏ không trừ kho. SaveOrder khóa hàng theo ID tăng dần, kiểm tra giá/tồn, giảm lượng khả dụng và lưu đơn/chi tiết trong cùng transaction. Lỗi tại bất cứ dòng nào đều rollback.
- Giỏ theo session và đồng bộ thao tác ghi theo session. Double click đặt hàng sau commit gặp giỏ rỗng, không tạo thêm đơn. Chưa có idempotency key dùng chung nhiều session/dịch vụ.
- Giá trong giỏ là giá đã hiển thị. Khi giá đổi, từ chối đặt; người dùng bấm cập nhật giá rồi xác nhận lại. Giỏ giữ nguyên khi lưu thất bại.
- Pending → Exported → Delivering → Completed. Cập nhật trạng thái có điều kiện trạng thái cũ, tránh duyệt lặp. Các bước sau đặt hàng không giảm kho lần nữa.
- Checkout dùng địa chỉ nhận mới và số điện thoại của tài khoản. Không tạo số giao nhận riêng.
- Hóa đơn tại quầy, phiếu nhập là chứng từ nhiều dòng, cùng commit/rollback với thay đổi tồn kho.
- Doanh thu = hóa đơn tại quầy + đơn trực tuyến hiện ở Completed. Lọc Invoice.createdDate và OnlineOrder.orderDate, bao gồm cả ngày cuối. Không có completedAt, không diễn giải thành doanh thu theo ngày giao xong.
- Giá trị nhập và số lượng nhập lấy tổng các dòng ImportDetail; không đếm phiếu thành số lượng hàng, không suy ra lợi nhuận.

## Quyền

Khách chỉ đọc đơn của mình. Kho quản lý hàng/nhà cung cấp/nhập/đơn. Bán hàng lập hóa đơn. Quản lý xem ba báo cáo. Nhân viên giao hàng là tác nhân nhận bàn giao, chưa có UI riêng. Các kiểm tra quyền nằm ở Servlet cho cả GET và POST; ẩn menu không thay thế phân quyền.

## Ranh giới vận hành

Chế độ nhúng bind 127.0.0.1. Trước triển khai thật cần HTTPS, tài khoản nhân viên riêng, loại dữ liệu/mật khẩu mẫu, connection pool, quản lý secret, backup/migration, audit log, rate limit tập trung và kiểm thử MySQL. Giới hạn đăng nhập hiện chỉ áp dụng theo session. Không coi đây là cam kết an toàn production.
