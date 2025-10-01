# Quản lý Thư viện (quan_ly_thu_vien3)

Phiên bản này là một ứng dụng Java Swing để quản lý thư viện (mượn/trả, quản lý sách, người dùng) với vài nâng cấp giao diện và sửa lỗi tương thích.

---

## Những chức năng & cải tiến chính (mới)
- Dark Mode cho cả giao diện người dùng và admin, kèm theo xử lý để giữ màu nút đặc biệt (ví dụ: nút Clear Filter) khi chuyển theme.
- Hiển thị ảnh đại diện (avatar) và bản xem trước ảnh bìa sách trong các form.
- Thay đổi luồng tạo yêu cầu mượn: không còn tự động tạo `borrow_request` khi đóng tab hoặc huỷ dialog — chỉ tạo khi người dùng bấm nút "Gửi đăng ký"/"Lưu".
- Cột "Hạn trả" giờ lấy theo `borrow_request.expected_return_date` (server đã cập nhật SQL để trả về giá trị expected_return_date gần nhất nếu có, hoặc fallback tính từ borrow_date).
- Highlight (màu) cho các mục sắp đến hạn (trong 1-3 ngày tuỳ chỗ) và quá hạn — giúp quản trị phát hiện nhanh.
- Giao diện Thống kê (Borrow Management) được nâng cấp: thẻ thông kê hiện đại, biểu đồ tháng dạng text được cải thiện, bảng Hoạt động gần đây đẹp hơn.
- Sửa lỗi tương thích Java: thay thế `String.repeat(...)` (Java 11+) bằng hàm dựng chuỗi an toàn để chạy trên Java 8+.
- Sửa lỗi generic/diamond: loại bỏ/viết rõ các `<>` không hợp lệ với anonymous classes (ví dụ `new JList<String>(...) { ... }`).
- Nâng kích thước mặc định của dialog "Chỉnh sửa thông tin sách" để hiển thị đầy đủ trường thông tin; thêm scroll khi cần và phím tắt Enter/Esc.

---

## Cấu trúc file (mô tả ngắn)
- `bin/` — Thư mục chứa các .class (những file biên dịch). Không commit file nhị phân nếu bạn thay đổi source.
- `src/` — Mã nguồn Java (chính). Các package chính:
  - `src/app/` — `MainApp.java` (entry point, nếu ứng dụng khởi chạy từ đây)
  - `src/client/` — Giao diện người dùng client + admin (Swing UIs)
    - `ClientUI.java` — Giao diện chính người dùng; đã sửa luồng tạo borrow request và theme handling.
    - `BorrowManagementUI.java` — Giao diện quản lý mượn/trả; chứa dialog Thống kê (đã nâng cấp) và fix `String.repeat`.
    - `BookManagerUI.java` — Giao diện quản lý sách; đã mở rộng dialog chỉnh sửa sách và nâng kích thước hiển thị.
    - `UserManagerUI.java`, `UserProfileUI.java`, ... — Các UI liên quan đến người dùng.
  - `src/server/` — Mã server xử lý request (nếu sử dụng kiến trúc client/server nội bộ)
    - `ClientHandler.java` — Xử lý truy vấn mượn/tra cứu; SQL đã thay đổi để sử dụng `borrow_request.expected_return_date` khi phù hợp.
  - `src/dao/` — Data Access Objects (BookDAO, BorrowDAO, UserDAO) — thao tác SQLite.
  - `src/model/` — Các class model: `Book`, `Borrow`, `User`.

- Hình ảnh giao diện / assets: (các file .jpg/.png ở root) — dùng trong demo/preview.

---

## Yêu cầu (Requirements)
- Java: JRE/JDK 8+ (ứng dụng về mặt tương thích đã được điều chỉnh để chạy trên Java 8; tuy nhiên khuyến nghị Java 11+ để được hiệu năng & API mới).
- SQLite DB file: `C:/data/library.db` được sử dụng mặc định (đường dẫn này nằm trong các kết nối JDBC của project). Bạn có thể chỉnh trong code nếu muốn đặt DB ở vị trí khác.
- Không có dependencies bên ngoài; project dùng Swing + JDBC (sqlite-jdbc nếu chạy standalone).

---

## Cách build & chạy (Windows PowerShell)
1. Biên dịch tất cả file `.java` (tạo thư mục `bin` nếu chưa có):

```powershell
cd C:\ReactJS\bankingWeb\bankingwebsite\quan_ly_thu_vien3\quanlythuvien3
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d bin -cp bin $files
```

2. Chạy ứng dụng (ví dụ `MainApp` hoặc UI chính):

```powershell
java -cp bin app.MainApp
```

Ghi chú:
- Nếu bạn chạy từng class riêng (ví dụ `java -cp bin client.BorrowManagementUI`), đảm bảo class đó có `main` hoặc được khởi tạo từ `MainApp`.
- Nếu gặp lỗi thiếu driver SQLite, đảm bảo `sqlite-jdbc` jar có trong classpath hoặc dùng driver JDBC tích hợp.

---

## Cấu hình DB & đường dẫn
- Mặc định kết nối SQLite nằm ở các chỗ như: `jdbc:sqlite:C:/data/library.db`. Nếu bạn muốn dùng file DB khác, tìm chuỗi `jdbc:sqlite:` trong `src/` và thay đường dẫn.

---

## Lưu ý tương thích & lỗi đã fix
- Lỗi runtime/compile "The method repeat(int) is undefined for the type String" đã được sửa bằng cách thay `String.repeat(...)` bằng đoạn code dựng chuỗi sử dụng `StringBuilder` (tương thích Java 8).
- Lỗi "`<>` can not be used with anonymous classes" xuất hiện nếu dùng diamond operator `<>` khi tạo anonymous class; mình đã thay bằng khai báo kiểu rõ ràng (ví dụ `new JList<String>(...) { ... }`).

---

## Troubleshooting nhanh
- Biên dịch lỗi: đọc thông báo `javac` và fix imports/diamond/calls với API cao hơn Java 8. Nếu bạn muốn target Java 11+, cài JDK 11+ và chạy `javac`/`java` bằng JDK đó.
- Lỗi kết nối DB: kiểm tra file `C:/data/library.db` tồn tại và có quyền đọc/ghi. Nếu DB rỗng, chạy script khởi tạo DB (nếu có) hoặc dùng `InitDatabase` class (nếu có trong `src/server`).
- Lỗi GUI không hiển thị đầy đủ: nhiều dialog đã được tăng kích thước và có scroll. Nếu màn hình nhỏ, thử phóng to cửa sổ hoặc chỉnh `editDialog.setSize(...)` trong code.

---

## Gợi ý phát triển tiếp
- Tiếp tục tách UI và logic (MVC) để dễ test.
- Thay các báo cáo text-based bằng thư viện chart (JFreeChart hoặc JavaFX) để có biểu đồ đẹp hơn.
- Thêm test unit cho DAO và so sánh migrations DB.

---

Nếu muốn, mình có thể:
- Tạo script PowerShell sẵn để biên dịch/chạy và backup DB.
- Thêm hướng dẫn setting Java (đổi PATH/JAVA_HOME) cho Windows.
- Tách một README chi tiết cho developer (cách debug, chạy unit test, mô tả DB schema).

Chỉ cần nói "Tạo script" hoặc "Thêm developer README" và mình sẽ làm tiếp.
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
- 🗄️ Quản lý dữ liệu người dùng, sách, mượn/trả, hoạt động.
- 🔗 Xử lý các yêu cầu từ Client: đăng nhập, đăng ký, tìm kiếm, mượn/trả sách, quản lý yêu thích, lịch sử hoạt động.
- 🔒 Đảm bảo an toàn và đồng bộ dữ liệu.

### 👤 **Chức năng của Client**
- 🔑 Đăng nhập, đăng ký tài khoản.
- 🔍 Tìm kiếm sách, xem thông tin chi tiết, mượn/trả sách.
- ❤️ Quản lý sách yêu thích, xem lịch sử hoạt động, hóa đơn mượn trả.
- 🛠️ Quản lý người dùng (dành cho admin).

### ⚙️ **Chức năng hệ thống**
- 📚 Quản lý sách, người dùng, hoạt động mượn/trả, yêu thích.
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
