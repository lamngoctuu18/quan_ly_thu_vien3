package client;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Fix Existing Borrow Records - Cập nhật ngày trả dự kiến cho các bản ghi hiện có
 */
public class FixBorrowRecords {
    
    public static void main(String[] args) {
        fixExistingRecords();
    }
    
    public static void fixExistingRecords() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            System.out.println("🔄 Fixing existing borrow records...");
            
            // Ensure expected_return_date column exists
            try {
                conn.createStatement().execute("ALTER TABLE borrows ADD COLUMN expected_return_date TEXT");
                System.out.println("✅ Added expected_return_date column to borrows table");
            } catch (Exception e) {
                System.out.println("⚠️ expected_return_date column already exists");
            }
            
            // Check current borrows without expected_return_date
            String checkSQL = "SELECT id, user_id, book_id, borrow_date, expected_return_date FROM borrows WHERE return_date IS NULL";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(checkSQL);
            
            int recordsToFix = 0;
            System.out.println("\n📋 Current active borrow records:");
            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                int bookId = rs.getInt("book_id");
                String borrowDate = rs.getString("borrow_date");
                String expectedReturnDate = rs.getString("expected_return_date");
                
                System.out.println("  📚 Borrow ID: " + id + " | User: " + userId + " | Book: " + bookId + 
                    " | Borrow Date: " + borrowDate + " | Expected Return: " + 
                    (expectedReturnDate != null ? expectedReturnDate : "NULL"));
                
                if (expectedReturnDate == null || expectedReturnDate.trim().isEmpty()) {
                    recordsToFix++;
                }
            }
            rs.close();
            
            if (recordsToFix > 0) {
                System.out.println("\n🔧 Found " + recordsToFix + " records to fix...");
                
                // Update records without expected_return_date
                String updateSQL = "UPDATE borrows SET expected_return_date = date(borrow_date, '+14 days') " +
                                 "WHERE return_date IS NULL AND (expected_return_date IS NULL OR expected_return_date = '')";
                
                int rowsUpdated = stmt.executeUpdate(updateSQL);
                System.out.println("✅ Updated " + rowsUpdated + " borrow records with calculated expected return dates");
                
                // Try to match with borrow_requests for more accurate dates
                String matchSQL = "UPDATE borrows SET expected_return_date = (" +
                    "SELECT br.expected_return_date FROM borrow_requests br " +
                    "WHERE br.user_id = borrows.user_id AND br.book_id = borrows.book_id " +
                    "AND br.status = 'APPROVED' " +
                    "ORDER BY br.request_date DESC LIMIT 1" +
                    ") WHERE return_date IS NULL AND EXISTS (" +
                    "SELECT 1 FROM borrow_requests br " +
                    "WHERE br.user_id = borrows.user_id AND br.book_id = borrows.book_id " +
                    "AND br.status = 'APPROVED' AND br.expected_return_date IS NOT NULL" +
                    ")";
                
                int matchedRows = stmt.executeUpdate(matchSQL);
                System.out.println("✅ Matched " + matchedRows + " records with original borrow request dates");
            } else {
                System.out.println("✅ All records already have expected return dates");
            }
            
            // Show final results
            System.out.println("\n📋 Updated borrow records:");
            rs = stmt.executeQuery(checkSQL);
            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                int bookId = rs.getInt("book_id");
                String borrowDate = rs.getString("borrow_date");
                String expectedReturnDate = rs.getString("expected_return_date");
                
                // Get user and book names for better display
                String userQuery = "SELECT username FROM users WHERE id = ?";
                PreparedStatement userPs = conn.prepareStatement(userQuery);
                userPs.setInt(1, userId);
                ResultSet userRs = userPs.executeQuery();
                String username = userRs.next() ? userRs.getString("username") : "Unknown";
                userRs.close();
                userPs.close();
                
                String bookQuery = "SELECT title FROM books WHERE id = ?";
                PreparedStatement bookPs = conn.prepareStatement(bookQuery);
                bookPs.setInt(1, bookId);
                ResultSet bookRs = bookPs.executeQuery();
                String bookTitle = bookRs.next() ? bookRs.getString("title") : "Unknown";
                bookRs.close();
                bookPs.close();
                
                System.out.println("  📚 " + username + " borrowed '" + bookTitle + "' on " + 
                    borrowDate + " | Expected return: " + expectedReturnDate);
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("\n🎉 Fix completed!");
            
        } catch (Exception e) {
            System.err.println("💥 Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}