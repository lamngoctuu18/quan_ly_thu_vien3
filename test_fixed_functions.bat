@echo off
echo ========================================
echo Test Fixed Favorites and Activities
echo ========================================

cd "c:\ReactJS\bankingWeb\bankingwebsite\quan_ly_thu_vien3\quanlythuvien3"

REM Initialize database first
echo [1/4] Initializing database...
java -cp "src" server.InitDatabase
if errorlevel 1 (
    echo ERROR: Database initialization failed!
    pause
    exit /b 1
)

REM Add sample data
echo [2/4] Adding sample data...
java -cp "src" -Dsqlite.database_path=C:/data/library.db -c "
try {
    Class.forName(\"org.sqlite.JDBC\");
    java.sql.Connection conn = java.sql.DriverManager.getConnection(\"jdbc:sqlite:C:/data/library.db\");
    
    // Add sample books
    String[] books = {
        \"('Clean Code', 'Robert C. Martin', 'Prentice Hall', '2008', 5, 'Khoa học – Công nghệ', '', 'Agile software craftsmanship')\",
        \"('Sapiens', 'Yuval Noah Harari', 'Văn hóa dân tộc', '2018', 3, 'Lịch sử – Địa lý', '', 'History of humanity')\",
        \"('Đắc Nhân Tâm', 'Dale Carnegie', 'NXB Tổng Hợp', '2016', 7, 'Tâm lý – Kỹ năng sống', '', 'How to win friends')\"
    };
    
    for (String book : books) {
        String sql = \"INSERT OR IGNORE INTO books (title, author, publisher, year, quantity, category, cover_image, description) VALUES \" + book;
        conn.createStatement().execute(sql);
    }
    
    // Add sample user
    conn.createStatement().execute(\"INSERT OR IGNORE INTO users (username, password, role, phone, email) VALUES ('testuser', 'password', 'user', '0123456789', 'test@test.com')\");
    
    conn.close();
    System.out.println(\"Sample data added!\");
} catch (Exception e) {
    e.printStackTrace();
}
" 2>nul || echo Sample data creation skipped

REM Compile all Java files
echo [3/4] Compiling Java files...
javac -cp "src" src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

REM Start the application
echo [4/4] Starting application...
echo.
echo FIXES APPLIED:
echo ===============
echo 1. Server now returns ID-Title-Author format for favorites
echo 2. Server returns ID-Title-Action-Time format for activities  
echo 3. addToFavorite now uses database directly
echo 4. Activities are recorded for add_favorite and borrow_request
echo 5. Delete functions should now work properly
echo.
echo TEST PROCEDURE:
echo ===============
echo 1. Login as 'testuser' / 'password'
echo 2. Add some books to favorites (heart icon)
echo 3. Make borrow requests (register button)
echo 4. Check "Sach yeu thich" - test delete functions
echo 5. Check "Hoat dong" - should show recorded activities
echo 6. Test delete individual and delete all functions
echo.

java -cp "src" app.MainApp

pause