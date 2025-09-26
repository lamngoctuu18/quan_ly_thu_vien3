@echo off
echo ========================================
echo Test Enhanced Favorites and Activities
echo ========================================

cd "c:\ReactJS\bankingWeb\bankingwebsite\quan_ly_thu_vien3\quanlythuvien3"

REM Compile all Java files
echo [1/3] Compiling Java files...
javac -cp "src" src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo Compilation successful!

REM Initialize database
echo [2/3] Initializing database...
java -cp "src" server.InitDatabase
if errorlevel 1 (
    echo ERROR: Database initialization failed!
    pause
    exit /b 1
)
echo Database initialized successfully!

REM Start the main application
echo [3/3] Starting application...
echo.
echo NEW FEATURES ADDED:
echo.
echo === FAVORITES MANAGEMENT ===
echo - Add books to favorites (records activity)
echo - Delete individual favorite books
echo - Delete all favorites at once
echo - Activity logging for favorites
echo.
echo === ACTIVITIES MANAGEMENT ===
echo - View all activities in table format
echo - Delete individual activities
echo - Clear all activities
echo - Auto-record: Add/Remove favorites, Borrow requests
echo.
echo TEST STEPS:
echo 1. Login as regular user
echo 2. Add some books to favorites
echo 3. Make borrow requests
echo 4. Check "Sach yeu thich" - should show delete buttons
echo 5. Check "Hoat dong" - should show activities with actions
echo 6. Test delete functions
echo.
java -cp "src" app.MainApp

pause