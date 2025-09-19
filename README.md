<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   Quản lý sách - thư viện qua mạng
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 1. Giới thiệu hệ thống

**Đề tài:** Quản lý sách - thư viện qua mạng

- **Chức năng của Server:**
  - Quản lý dữ liệu người dùng, sách, mượn/trả, hoạt động.
  - Xử lý các yêu cầu từ Client: đăng nhập, đăng ký, tìm kiếm, mượn/trả sách, quản lý yêu thích, lịch sử hoạt động.
  - Đảm bảo an toàn và đồng bộ dữ liệu.

- **Chức năng của Client:**
  - Đăng nhập, đăng ký tài khoản.
  - Tìm kiếm sách, xem thông tin chi tiết, mượn/trả sách.
  - Quản lý sách yêu thích, xem lịch sử hoạt động, hóa đơn mượn trả.
  - Quản lý người dùng (dành cho admin).

- **Chức năng hệ thống:**
  - Quản lý sách, người dùng, hoạt động mượn/trả, yêu thích.
  - Phân quyền người dùng (admin, user).
  - Giao diện thân thiện, dễ sử dụng.

## 2. Công nghệ sử dụng

- Java (Swing, JDBC)
- SQLite
- TCP Socket (Client/Server)
- GroupLayout, GridBagLayout (giao diện responsive)
- Các thư viện Java chuẩn

## 3. Hình ảnh các chức năng

**Hình 1: Giao diện đăng nhập**
<p align="center">
    <img src="docs/screenshot_login.png" alt="Giao diện đăng nhập" width="600"/>
</p>

**Hình 2: Giao diện quản lý sách**
<p align="center">
    <img src="docs/screenshot_bookmanager.png" alt="Giao diện quản lý sách" width="600"/>
</p>

**Hình 3: Giao diện quản lý người dùng**
<p align="center">
    <img src="docs/screenshot_usermanager.png" alt="Giao diện quản lý người dùng" width="600"/>
</p>

**Hình 4: Giao diện mượn/trả sách**
<p align="center">
    <img src="docs/screenshot_borrowclient.png" alt="Giao diện mượn trả sách" width="600"/>
</p>

**Hình 5: Giao diện hóa đơn mượn sách**
<p align="center">
    <img src="docs/screenshot_invoice.png" alt="Giao diện hóa đơn mượn sách" width="600"/>
</p>

## 4. Hướng dẫn cài đặt và sử dụng

### Yêu cầu hệ thống

- Máy tính cài đặt Java 8 trở lên
- SQLite JDBC driver
- Hệ điều hành Windows (khuyến nghị)

### Cài đặt và triển khai

1. Clone hoặc tải source code về máy.
2. Chạy file `InitDatabase.java` để khởi tạo CSDL SQLite tại `C:/data/library.db`.
3. Chạy `LibraryServer.java` để khởi động server.
4. Chạy `MainApp.java` để mở giao diện client.

### Sử dụng ứng dụng

- Đăng nhập bằng tài khoản admin mặc định:  
  - Tên đăng nhập: `admin`  
  - Mật khẩu: `admin`
- Đăng ký tài khoản mới nếu chưa có.
- Quản lý sách, người dùng, mượn/trả, yêu thích, hóa đơn qua giao diện.

## Thông tin liên hệ

- **Họ tên:** Lâm Ngọc Tú
- **Lớp:** CNTT 16-01
- **Email:** lamngoctuk55@gmail.com