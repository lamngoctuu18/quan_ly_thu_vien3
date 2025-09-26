package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateBorrowRequestsTable {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            Statement stmt = conn.createStatement();
            
            // Create borrow_requests table
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
            
            stmt.execute(createTable);
            System.out.println("Created borrow_requests table successfully!");
            
            // Add some indexes for better performance
            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_borrow_requests_user_id ON borrow_requests(user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_borrow_requests_book_id ON borrow_requests(book_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_borrow_requests_status ON borrow_requests(status)");
                System.out.println("Added indexes successfully!");
            } catch (Exception e) {
                System.out.println("Indexes might already exist: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}