@echo off
echo ========================================
echo Add Sample Data for Testing
echo ========================================

cd "c:\ReactJS\bankingWeb\bankingwebsite\quan_ly_thu_vien3\quanlythuvien3"

REM Compile and run database initialization
echo [1/2] Initializing database with sample data...
javac -cp "src" src/server/InitDatabase.java
java -cp "src" server.InitDatabase

echo [2/2] Adding sample books...
java -cp "src" -Djava.awt.headless=true -c "
import java.sql.*;
try {
    Connection conn = DriverManager.getConnection(\"jdbc:sqlite:C:/data/library.db\");
    
    // Add sample books
    String[] books = {
        \"Clean Code|Robert C. Martin|Prentice Hall|2008|5|Khoa học – Công nghệ|https://m.media-amazon.com/images/I/41SH-SvWPxL.jpg|A handbook of agile software craftsmanship\",
        \"Sapiens: Lược sử loài người|Yuval Noah Harari|Văn hóa dân tộc|2018|3|Lịch sử – Địa lý|https://salt.tikicdn.com/cache/750x750/ts/product/5e/18/24/2a6154ba08df6ce6161c13f4303fa19e.jpg|Câu chuyện về sự tiến hóa của loài người\",
        \"Đắc Nhân Tâm|Dale Carnegie|NXB Tổng Hợp|2016|7|Tâm lý – Kỹ năng sống|https://salt.tikicdn.com/cache/750x750/ts/product/3c/d0/85/b5b0c5f04d22b4b6f0b22db9e7c7e0f0.jpg|Nghệ thuật đối nhân xử thế\",
        \"Lịch sử Việt Nam|Đào Duy Anh|Văn hóa dân tộc|2019|4|Lịch sử – Địa lý|https://salt.tikicdn.com/cache/750x750/ts/product/f8/89/45/2c8c7b7e47c6d9c3b09d42e12a8b84a2.jpg|Lịch sử hình thành và phát triển của dân tộc Việt Nam\"
    };
    
    for (String book : books) {
        String[] parts = book.split(\"\\|\");
        String sql = \"INSERT OR IGNORE INTO books (title, author, publisher, year, quantity, category, cover_image, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)\";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, parts[0]);
        ps.setString(2, parts[1]);
        ps.setString(3, parts[2]);
        ps.setString(4, parts[3]);
        ps.setInt(5, Integer.parseInt(parts[4]));
        ps.setString(6, parts[5]);
        ps.setString(7, parts[6]);
        ps.setString(8, parts[7]);
        ps.executeUpdate();
    }
    
    // Add sample user
    String userSql = \"INSERT OR IGNORE INTO users (username, password, role, phone, email) VALUES ('user1', 'password', 'user', '0123456789', 'user1@example.com')\";
    Statement stmt = conn.createStatement();
    stmt.execute(userSql);
    
    conn.close();
    System.out.println(\"Sample data added successfully!\");
} catch (Exception e) {
    e.printStackTrace();
}
"

echo.
echo SAMPLE DATA ADDED:
echo - 4 sample books with covers and descriptions
echo - Sample user: username='user1', password='password'
echo - Admin user: username='admin', password='admin'
echo.
echo Now you can test:
echo 1. Login as 'user1' / 'password'
echo 2. Add books to favorites
echo 3. Make borrow requests
echo 4. Check activities and favorites with delete functions
echo.

pause