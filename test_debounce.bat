@echo off
chcp 65001 > nul
echo ============================================
echo   TEST DEBOUNCE FEATURE
echo ============================================
echo.

cd quanlythuvien3

echo [1/3] Compiling ClientUI with debounce...
javac -encoding UTF-8 -d bin -cp "bin;." src/client/ClientUI.java src/client/DarkModeManager.java src/client/DatabaseManager.java src/client/BackgroundTaskManager.java src/model/*.java src/dao/*.java

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.

echo [2/3] Starting LibraryServer...
start "Library Server" cmd /c "cd /d %CD% && javac -encoding UTF-8 -d bin src/server/*.java && java -cp bin server.LibraryServer"
timeout /t 3 /nobreak > nul

echo [3/3] Starting ClientUI...
echo.
echo 📋 TEST SCENARIOS:
echo   1. Gõ nhanh vào ô tìm kiếm - chỉ nên thấy 1 query sau 300ms
echo   2. Nhập vào "Tác giả" - refresh sau 500ms khi ngừng gõ
echo   3. Di chuột vào nút "Tìm kiếm" nhiều lần - không lỗi
echo   4. Chuyển Dark/Light Mode và hover lại - vẫn hoạt động tốt
echo.
echo Starting in 2 seconds...
timeout /t 2 /nobreak > nul

java -cp bin client.ClientUI

echo.
echo Test completed!
pause
