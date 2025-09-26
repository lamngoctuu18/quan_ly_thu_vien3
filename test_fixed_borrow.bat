@echo off
echo ========================================
echo Test Fixed Borrow Request System
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

REM Start the main application
echo [2/3] Starting application...
echo.
echo FIXED ISSUES:
echo - Removed due_date column from INSERT query
echo - Added database timeout (30 seconds)
echo - Improved connection management
echo.
echo TEST STEPS:
echo 1. Login as admin
echo 2. Go to "Quan ly dang ky muon sach"
echo 3. Try to approve a borrow request
echo 4. Check if no SQL errors occur
echo.
java -cp "src" app.MainApp

pause