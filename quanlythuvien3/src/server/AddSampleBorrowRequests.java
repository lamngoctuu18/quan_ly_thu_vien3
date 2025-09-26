package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;

public class AddSampleBorrowRequests {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            // Create borrow_requests table if it doesn't exist
            String createTable = "CREATE TABLE IF NOT EXISTS borrow_requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "book_id INTEGER NOT NULL, " +
                "request_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "status TEXT DEFAULT 'PENDING', " +
                "approved_date DATETIME, " +
                "admin_notes TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (book_id) REFERENCES books(id)" +
                ")";
            
            conn.createStatement().execute(createTable);
            System.out.println("Ensured borrow_requests table exists!");
            
            // Clear existing sample requests
            conn.createStatement().execute("DELETE FROM borrow_requests WHERE id < 100");
            
            // Add sample borrow requests
            String[] sampleRequests = {
                "INSERT INTO borrow_requests (user_id, book_id, request_date, status, admin_notes) VALUES (1, 1, ?, 'PENDING', null)",
                "INSERT INTO borrow_requests (user_id, book_id, request_date, status, admin_notes) VALUES (2, 2, ?, 'APPROVED', 'Đã duyệt cho sinh viên xuất sắc')",
                "INSERT INTO borrow_requests (user_id, book_id, request_date, status, admin_notes) VALUES (1, 3, ?, 'REJECTED', 'Sách này chỉ dành cho nghiên cứu sinh')",
                "INSERT INTO borrow_requests (user_id, book_id, request_date, status, admin_notes) VALUES (3, 4, ?, 'PENDING', null)",
                "INSERT INTO borrow_requests (user_id, book_id, request_date, status, admin_notes) VALUES (2, 5, ?, 'PENDING', null)"
            };
            
            for (int i = 0; i < sampleRequests.length; i++) {
                PreparedStatement ps = conn.prepareStatement(sampleRequests[i]);
                // Add different request dates (some recent, some older)
                LocalDateTime requestDate = LocalDateTime.now().minusDays(i * 2);
                ps.setString(1, requestDate.toString());
                ps.executeUpdate();
                System.out.println("Added sample borrow request " + (i + 1));
            }
            
            System.out.println("Added sample borrow requests successfully!");
            
        } catch (Exception e) {
            System.out.println("Error adding sample requests: " + e.getMessage());
            e.printStackTrace();
        }
    }
}