@echo off
chcp 65001 > nul
echo ============================================
echo   TEST UI FIX - Duplicate Buttons & Text
echo ============================================
echo.

cd quanlythuvien3

echo [1/2] Compiling ClientUI...
javac -encoding UTF-8 -d bin -cp "bin;." src/client/ClientUI.java src/client/DarkModeManager.java src/client/DatabaseManager.java src/client/BackgroundTaskManager.java src/model/*.java src/dao/*.java

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.

echo [2/2] Starting ClientUI...
echo.
echo 🧪 TEST CHECKLIST:
echo   1. Login với account bất kỳ
echo   2. Kiểm tra text "Xin chào, username" hiển thị đầy đủ
echo   3. Click vào User Profile panel - mở dialog
echo   4. Hover vào nút Notification - background thay đổi
echo   5. Click nút Notification - mở danh sách thông báo
echo   6. Click nút Dark/Light Mode - toggle theme
echo   7. Kiểm tra KHÔNG có nút trùng lặp
echo.
echo Starting in 2 seconds...
timeout /t 2 /nobreak > nul

java -cp bin client.ClientUI

echo.
echo Test completed!
pause
