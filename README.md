<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
</h2>
<h2 align="center">
   Quản lý sách - thư viện qua mạng
</h2>
<div align="center">
    <p align="center">
        <img width="170" alt="aiotlab_logo" src="https://github.com/user-attachments/assets/41ef702b-3d6e-4ac4-beac-d8c9a874bca9" />
        <img width="180" alt="fitdnu_logo" src="https://github.com/user-attachments/assets/ec4815af-e477-480b-b9fa-c490b74772b8" />
        <img width="200" alt="dnu_logo" src="https://github.com/user-attachments/assets/2bcb1a6c-774c-4e7d-b14d-8c53dbb4067f" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 1. Giới thiệu hệ thống

📚 **Đề tài:** Quản lý sách - thư viện qua mạng

Hệ thống quản lý sách - thư viện qua mạng được xây dựng nhằm hỗ trợ các hoạt động quản lý, tra cứu, mượn/trả sách trong môi trường hiện đại, thân thiện và bảo mật.  
Ứng dụng giúp kết nối giữa người dùng và quản trị viên thông qua giao diện trực quan, dễ sử dụng, đồng thời đảm bảo dữ liệu luôn được cập nhật và đồng bộ.

### 🎯 **Mục tiêu hệ thống**
- Tối ưu hóa quy trình quản lý sách, người dùng, hoạt động mượn/trả.
- Đáp ứng nhu cầu tra cứu, mượn/trả sách nhanh chóng, chính xác.
- Hỗ trợ phân quyền (admin, user) để đảm bảo bảo mật và hiệu quả vận hành.
- Cung cấp giao diện hiện đại, dễ sử dụng, phù hợp với nhiều đối tượng người dùng.

### 🖥️ **Chức năng của Server**
<!-- New server features -->
- 🆕 Hỗ trợ `borrow_requests` và `expected_return_date`:
    - Server trả về hạn trả ưu tiên theo `borrow_request.expected_return_date` (SQL đã sử dụng COALESCE để chọn giá trị gần nhất hoặc fallback tính từ `borrow_date`).
    - Có script migration để thêm cột `expected_return_date` và cập nhật dữ liệu hiện có (`AddExpectedReturnDateColumn`, `FixBorrowRecords`).
- 🧾 Tạo/ghi nhận mẫu dữ liệu: các tiện ích server tiện lợi để chèn dữ liệu mẫu (ví dụ `AddSampleBooks`, `AddSampleBorrowRequests`).
- 🔁 Giao tiếp ổn định TCP socket qua `LibraryServer` / `ClientHandler` để xử lý nhiều client đồng thời.
- 🧰 Các công cụ quản trị cơ sở dữ liệu: `InitDatabase`, `CreateBorrowRequestsTable` để khởi tạo DB và cấu trúc bảng an toàn.
- �️ Hỗ trợ xác thực và phân quyền cơ bản (admin/user) ngay ở lớp server; có hooks để mở rộng bảo mật (hash mật khẩu, OTP recovery flow trong `MainApp`).


<!-- New client features -->
- 🌓 Dark Mode toàn diện (giao diện Client & Admin) với `DarkModeManager` và component base `DarkModeAwareComponent` để tự động áp dụng theme.
- �️ Avatar người dùng và preview ảnh bìa sách: hiển thị avatar trong header/profile và preview cover trong `BookManagerUI` / `UserProfileUI`.
- � Ngăn tạo yêu cầu mượn tự động: sửa luồng tạo `borrow_request` để chỉ lưu khi người dùng ấn nút "Gửi đăng ký"/"Lưu" (không còn lưu khi đóng dialog/hủy).
- 🔔 Thống báo & logs: `NotificationUI` hiển thị thông báo (mượn/trả, duyệt yêu cầu).
- � Quản lý mượn/trả (admin): `BorrowManagementUI` có:
    - Thẻ số liệu (cards), biểu đồ tháng (scale theo max), panel hoạt động gần đây.
    - Fix tương thích Java (loại bỏ String.repeat), và cải thiện hiển thị (fonts, gradients).
- 📋 `BorrowRequestManagerUI`: xử lý, tìm kiếm và duyệt các yêu cầu mượn từ client.
- 🧭 UI tiện ích: `AdminLauncher` (loading + theme), `ModernSidebarButton`, `ThemeSelector` để thay đổi sidebar theme, `LoadingDialog`/`LoadingUtils` cho thao tác nặng.
- 🧑‍💻 UX nâng cao: phím tắt Enter/Esc trong form chỉnh sửa sách, dialog mở rộng với scroll, responsive layout (GroupLayout/GridBagLayout).

- 🛠️ Quản lý người dùng (dành cho admin).
<!-- New system-level features -->
- 🔄 Background & stability:
    - `BackgroundTaskManager` để chạy tác vụ dài không block UI (SwingWorker + ExecutorService).
    - `KeepAliveManager` / `SystemTimeoutManager` để giữ kết nối DB/ứng dụng ổn định, tránh timeout.
- 🧪 Migration & maintenance:
    - Scripts tiện ích để migrate DB, cập nhật schema và seed sample data (`InitDatabase`, `UpdateDatabase`, `AddExpectedReturnDateColumn`).
- ♻️ Tương thích & triage lỗi:
    - Sửa các vấn đề tương thích Java (ví dụ thay vì `String.repeat` sử dụng `StringBuilder`), sửa `<>` diamond issue trong anonymous classes.
- 📈 Thống kê & báo cáo:
    - Dashboard/Realtime stats (cards) và textual charts trong `BorrowManagementUI` giúp admin nhìn tổng quan nhanh.
- 🔧 Công cụ hỗ trợ phát triển:
    - `DatabaseManager` connection-pool, `AvatarDatabaseSetup` để tạo test users, `FixBorrowRecords` để xử lý dữ liệu cũ.

- 🛡️ Phân quyền người dùng (admin, user).
- 🖼️ Giao diện thân thiện, dễ sử dụng, sinh động với icon và màu sắc.

## 2. Công nghệ sử dụng

- ☕ Java (Swing, JDBC)
- 🗃️ SQLite
- 🌐 TCP Socket (Client/Server)
- 🖼️ GroupLayout, GridBagLayout (giao diện responsive)
- 📦 Các thư viện Java chuẩn

## 3. Hình ảnh các chức năng

**Hình 1: <img src="https://img.icons8.com/ios-filled/24/000000/login-rounded-right.png"/> Giao diện đăng nhập**
<p align="center">
    <img width="398" height="301" alt="image" src="https://github.com/user-attachments/assets/ccf3e979-8caa-4072-9bef-500fc2d8bf28" />
</p>

**Hình 2: <img src="https://img.icons8.com/ios-filled/24/000000/login-rounded-right.png"/> Giao diện đăng ký**
<p align="center">
    <img width="468" height="381" alt="image" src="https://github.com/user-attachments/assets/ef5211ac-ef03-4bbd-aefb-25b2e784a27d" />
   </p>

**Hình 3: <img src="https://img.icons8.com/ios-filled/24/000000/books.png"/> Giao diện admin**
<p align="center">
   <img width="872" height="569" alt="image" src="https://github.com/user-attachments/assets/e79b3b06-ccd3-47cb-8440-ce89c974c3b0" />
</p>

**Hình 4: <img src="https://img.icons8.com/ios-filled/24/000000/user-group-man-man.png"/> Giao diện quản lý người dùng**
<p align="center">
    <img width="600" alt="image" src="https://github.com/user-attachments/assets/b51d41fd-d870-439f-8d7b-97397fc1d1b3" />
</p>

**Hình 5: <img src="https://img.icons8.com/ios-filled/24/000000/borrow-book.png"/> Giao diện quản lý mượn/trả sách**
<p align="center">
<img width="1174" height="674" alt="image" src="https://github.com/user-attachments/assets/5e95d1dc-110d-45e7-b278-e852123ab339" />
</p>

**Hình 6: <img src="https://img.icons8.com/ios-filled/24/000000/borrow-book.png"/> Giao diện quản lý sách**
<p align="center">
    <img width="859" height="579" alt="image" src="https://github.com/user-attachments/assets/c477e656-acca-4453-b306-bb9f9f4d7ba0" />
</p> 

**Hình 7: <img src="https://img.icons8.com/ios-filled/24/000000/invoice.png"/> Giao diện hóa đơn mượn sách**
<p align="center">
   <img width="875" height="381" alt="image" src="https://github.com/user-attachments/assets/7061d8d5-ebc2-4751-bddb-0d77c26e7048" />
</p>

**Hình 8: <img src="https://img.icons8.com/ios-filled/24/000000/invoice.png"/> Giao diện đăng ký mượn sách**
<p align="center">
 <img width="570" height="507" alt="image" src="https://github.com/user-attachments/assets/52195c7a-605c-4362-95df-710694587615" />
</p>

**Hình 9: <img src="https://img.icons8.com/ios-filled/24/000000/invoice.png"/> Giao diện thêm sách vào yêu thích**
<p align="center">
   <img width="673" height="388" alt="image" src="https://github.com/user-attachments/assets/7f37321a-66d0-4af3-9d74-57de8c82a956" />
</p>

**Hình 10: <img src="https://img.icons8.com/ios-filled/24/000000/invoice.png"/> Giao diện lịch sử hoạt động**
<p align="center">
   <img width="674" height="382" alt="image" src="https://github.com/user-attachments/assets/3f78437e-f2e0-4d54-aa95-5cfba6ef9d39" />
</p>

## 4. Hướng dẫn cài đặt và sử dụng

### Yêu cầu hệ thống

- 💻 Máy tính cài đặt Java 8 trở lên
- 🗃️ SQLite JDBC driver
- 🖥️ Hệ điều hành Windows (khuyến nghị)

### Cài đặt và triển khai

1. 📥 Clone hoặc tải source code về máy.
2. 🏗️ Chạy file `InitDatabase.java` để khởi tạo CSDL SQLite tại `C:/data/library.db`.
3. 🚀 Chạy `LibraryServer.java` để khởi động server.
4. 🖱️ Chạy `MainApp.java` để mở giao diện client.

### Sử dụng ứng dụng

- 🔑 Đăng nhập bằng tài khoản admin mặc định:  
  - Tên đăng nhập: `admin`  
  - Mật khẩu: `admin`
- 📝 Đăng ký tài khoản mới nếu chưa có.
- 📚 Quản lý sách, người dùng, mượn/trả, yêu thích, hóa đơn qua giao diện.

## Thông tin liên hệ

- **👤 Họ tên:** Lâm Ngọc Tú
- **🏫 Lớp:** CNTT 16-01
- **✉️ Email:** lamngoctuk55@gmail.com
