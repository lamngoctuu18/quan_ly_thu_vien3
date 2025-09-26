@echo off
echo ========================================
echo Reset Database and Test System
echo ========================================

cd "c:\ReactJS\bankingWeb\bankingwebsite\quan_ly_thu_vien3\quanlythuvien3"

REM Compile all Java files first
echo [1/4] Compiling Java files...
javac -cp "src" src/server/InitDatabase.java src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo Compilation successful!

REM Initialize database
echo [2/4] Initializing database...
java -cp "src" server.InitDatabase
if errorlevel 1 (
    echo ERROR: Database initialization failed!
    pause
    exit /b 1
)
echo Database initialized successfully!

REM Start the server in background
echo [3/4] Starting server...
start /B java -cp "src" server.LibraryServer
timeout /t 3 /nobreak >nul

REM Start the main application
echo [4/4] Starting client application...
echo.
echo DATABASE STRUCTURE FIXED:
echo - borrows table has: id, user_id, book_id, borrow_date, return_date
echo - No due_date column (which was causing the error)
echo - Added proper timeout handling
echo.
echo TEST PROCEDURE:
echo 1. Login as admin (username: admin, password: admin)
echo 2. Go to "Quan ly dang ky muon sach"
echo 3. Try to approve a borrow request
echo 4. Should work without SQL errors now
echo.
java -cp "src" app.MainApp

pause