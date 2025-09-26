package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.io.File;

public class InitDatabase {
    private static final String DB_PATH = "C:/data/library.db";

    public static void main(String[] args) {
        try {
            // Tạo thư mục C:/data nếu chưa có
            File dir = new File("C:/data");
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("Đã tạo thư mục: " + dir.getAbsolutePath());
            }

            // Kết nối SQLite (nếu file chưa có thì tự tạo)
            String url = "jdbc:sqlite:" + DB_PATH;
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();

        // Tạo bảng users (thêm cột created_at)
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "username TEXT UNIQUE," +
            "password TEXT," +
            "role TEXT," +
            "phone TEXT," +
            "email TEXT," +
            "avatar TEXT," +
            "created_at TEXT DEFAULT (datetime('now'))"
            + ")");

            // Tạo bảng books
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "author TEXT," +
                    "publisher TEXT," +      
                    "year TEXT," +          
                    "quantity INTEGER," +
                    "category TEXT," +
                    "favorite INTEGER DEFAULT 0" +
                    ")");

            // Modify books table to add cover_image column
            try {
                stmt.execute("ALTER TABLE books ADD COLUMN cover_image TEXT DEFAULT ''");
            } catch (Exception e) {
                // Column already exists
            }
            
            // Add description column
            try {
                stmt.execute("ALTER TABLE books ADD COLUMN description TEXT DEFAULT ''");
            } catch (Exception e) {
                // Column already exists
            }
            
            // Add rating column for books (average rating)
            try {
                stmt.execute("ALTER TABLE books ADD COLUMN rating REAL DEFAULT 0.0");
            } catch (Exception e) {
                // Column already exists
            }
            
            // Create ratings table for individual user ratings
            stmt.execute("CREATE TABLE IF NOT EXISTS ratings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "book_id INTEGER," +
                    "rating INTEGER," +  // 1-5 stars
                    "review TEXT," +
                    "created_at TEXT DEFAULT (datetime('now'))," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(book_id) REFERENCES books(id)," +
                    "UNIQUE(user_id, book_id)" +
                    ")");

            // Tạo bảng favorites (sách yêu thích)
            stmt.execute("CREATE TABLE IF NOT EXISTS favorites (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "book_id INTEGER," +
                    "added_date TEXT DEFAULT (datetime('now'))," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(book_id) REFERENCES books(id)," +
                    "UNIQUE(user_id, book_id)" +
                    ")");

            // Tạo bảng borrows
            stmt.execute("CREATE TABLE IF NOT EXISTS borrows (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "book_id INTEGER," +
                    "borrow_date TEXT," +
                    "return_date TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(book_id) REFERENCES books(id))");

            // Tạo bảng activities (lịch sử hoạt động)
            stmt.execute("CREATE TABLE IF NOT EXISTS activities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "book_id INTEGER," +
                    "action TEXT," + // borrow, return, favorite
                    "action_time TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(book_id) REFERENCES books(id))");

            // Tạo bảng borrow_requests (đăng ký mượn sách)
            stmt.execute("CREATE TABLE IF NOT EXISTS borrow_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "book_id INTEGER NOT NULL," +
                    "request_date TEXT NOT NULL," +
                    "expected_return_date TEXT," +
                    "notes TEXT," +
                    "status TEXT DEFAULT 'PENDING'," +
                    "admin_notes TEXT," +
                    "approved_date TEXT," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (book_id) REFERENCES books(id)" +
                    ")");
            
            // Thêm các cột mới vào bảng borrow_requests nếu chưa có
            try {
                stmt.execute("ALTER TABLE borrow_requests ADD COLUMN expected_return_date TEXT");
            } catch (Exception e) {
                // Cột đã tồn tại
            }
            
            try {
                stmt.execute("ALTER TABLE borrow_requests ADD COLUMN notes TEXT");
            } catch (Exception e) {
                // Cột đã tồn tại
            }

            // Tạo bảng notifications (thông báo)
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "type TEXT NOT NULL," + // borrow_approved, borrow_rejected, new_book, reminder, system
                    "title TEXT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "created_at TEXT NOT NULL," +
                    "is_read INTEGER DEFAULT 0," + // 0 = chưa đọc, 1 = đã đọc
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")");

            // Tạo admin mặc định
            stmt.execute("DELETE FROM users WHERE username='admin'"); // Xóa nếu đã tồn tại
            stmt.execute("INSERT INTO users(username, password, role, phone, email) " + 
                        "VALUES('admin', 'admin', 'admin', '', '')");

            conn.close();
            System.out.println("CSDL đã khởi tạo thành công tại: " + DB_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
