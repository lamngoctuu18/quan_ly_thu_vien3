package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Database Migration - Thêm cột expected_return_date vào bảng borrows
 */
public class AddExpectedReturnDateColumn {
    
    public static void main(String[] args) {
        updateDatabase();
    }
    
    public static void updateDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            Statement stmt = conn.createStatement();
            
            System.out.println("🔄 Updating database schema...");
            
            // Add expected_return_date column to borrows table if it doesn't exist
            try {
                stmt.execute("ALTER TABLE borrows ADD COLUMN expected_return_date TEXT");
                System.out.println("✅ Added expected_return_date column to borrows table");
            } catch (Exception e) {
                System.out.println("⚠️ expected_return_date column already exists or error: " + e.getMessage());
            }
            
            // Update existing borrows with calculated expected_return_date (14 days from borrow_date)
            try {
                String updateSQL = "UPDATE borrows SET expected_return_date = date(borrow_date, '+14 days') WHERE expected_return_date IS NULL";
                int rowsUpdated = stmt.executeUpdate(updateSQL);
                System.out.println("✅ Updated " + rowsUpdated + " existing borrow records with expected return dates");
            } catch (Exception e) {
                System.out.println("❌ Error updating existing records: " + e.getMessage());
            }
            
            // Verify the changes
            System.out.println("\n📋 Checking borrows table structure:");
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(borrows)");
            while (rs.next()) {
                String columnName = rs.getString("name");
                String dataType = rs.getString("type");
                System.out.println("  📝 Column: " + columnName + " (" + dataType + ")");
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("\n🎉 Database update completed!");
            
        } catch (Exception e) {
            System.err.println("💥 Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}