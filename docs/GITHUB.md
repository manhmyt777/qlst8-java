# Cấu hình và push GitHub trên Windows

Đứng tại thư mục `code` chứa `pom.xml` và README; không chạy `git init` ở thư mục tài liệu cha.

## 1. Danh tính commit

Máy hiện đã có Git. Nếu chưa cấu hình:

```powershell
git config --global user.name "Tên của bạn"
git config --global user.email "Email đã xác minh trên GitHub hoặc email noreply"
```

Có thể lấy email noreply từ GitHub → Settings → Emails. Email này được ghi vào commit. Không dùng mật khẩu GitHub làm thông tin cấu hình Git.

## 2. Repository và remote

Repository đã được tạo: `https://github.com/manhmyt777/qlst8-java`, private. Thư mục code đã được khởi tạo Git với nhánh main và remote origin. Kiểm tra:

```powershell
git status
git remote -v
```

Khi tự tạo một checkout khác chưa có Git:

```powershell
git init -b main
git remote add origin https://github.com/manhmyt777/qlst8-java.git
```

Nếu origin có sẵn nhưng sai URL, dùng `git remote set-url origin ...`.

## 3. Kiểm tra rồi push

```powershell
.
un.cmd test
git status --short
git add .
git diff --cached --stat
git commit -m "Implement electronics store management system"
git push -u origin main
```

Nếu commit đầu đã có sẵn, chỉ commit khi có thay đổi mới. Không dùng force push để xử lý lỗi nhánh từ xa.

Git Credential Manager của Git for Windows có thể mở trình duyệt để xác thực. Hoàn thành đăng nhập GitHub trong trình duyệt; không gửi token/mật khẩu vào chat và không đặt token trong URL remote. GitHub dùng HTTPS hoặc SSH; mật khẩu tài khoản không được dùng trực tiếp để push HTTPS.

## 4. Các lần cập nhật sau

```powershell
git status
git add .
git commit -m "Describe the actual change"
git push
```

Nếu Git báo remote có commit mới, kiểm tra thay đổi rồi `git pull --rebase origin main` trước khi push. Nếu rebase có xung đột, xử lý từng tệp và kiểm thử lại; không ghi đè lịch sử từ xa.

## Tệp được loại khỏi Git

`tools/`, `target/`, `data/`, `.env`, log và cấu hình IDE cá nhân. Không thêm mật khẩu MySQL, token, bài nộp gốc, PDF bài giảng hay cơ sở dữ liệu người dùng vào commit.

Tham chiếu: [GitHub — Set up Git](https://docs.github.com/en/get-started/git-basics/set-up-git).
