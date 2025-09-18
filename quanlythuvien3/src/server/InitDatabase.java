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

            // Tạo bảng users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE," +
                    "password TEXT," +
                    "role TEXT," +
                    "phone TEXT," +
                    "email TEXT)");

            // Tạo bảng books
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "author TEXT," +
                    "publisher TEXT," +      
                    "year TEXT," +          
                    "quantity INTEGER)");

            // Tạo bảng borrows
            stmt.execute("CREATE TABLE IF NOT EXISTS borrows (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "book_id INTEGER," +
                    "borrow_date TEXT," +
                    "return_date TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(book_id) REFERENCES books(id))");

            // Tạo admin mặc định
            stmt.execute("INSERT OR IGNORE INTO users(username, password, role, phone, email) VALUES('admin','admin','admin','','')");

            conn.close();
            System.out.println("CSDL đã khởi tạo thành công tại: " + DB_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
