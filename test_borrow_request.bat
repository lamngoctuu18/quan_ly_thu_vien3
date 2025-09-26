@echo off
echo ========================================
echo Test Dang Ky Muon Sach System
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
echo INSTRUCTIONS:
echo 1. Login with user account (not admin)
echo 2. Click on a book to view details
echo 3. Click "Dang ky muon" button
echo 4. Check if request is created successfully
echo.
java -cp "src" app.MainApp

pause