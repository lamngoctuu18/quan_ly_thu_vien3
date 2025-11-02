@echo off
chcp 65001 >nul
echo ============================================
echo   TEST AVATAR UPLOAD FEATURE
echo ============================================
echo.

cd /d "%~dp0quanlythuvien3"

echo [1/3] Compiling AvatarManager...
javac -encoding UTF-8 -d bin src/client/AvatarManager.java
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compile AvatarManager failed!
    pause
    exit /b 1
)

echo [2/3] Compiling TestAvatarManager...
javac -encoding UTF-8 -cp bin -d bin src/app/TestAvatarManager.java
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compile TestAvatarManager failed!
    pause
    exit /b 1
)

echo [3/3] Running Test...
echo.
echo ============================================
echo   HƯỚNG DẪN:
echo   1. Click "Upload Avatar" button
echo   2. Chọn file ảnh (.jpg, .png, .gif)
echo   3. Ảnh sẽ tự động crop thành vuông
echo   4. Resize 200x200 và lưu vào C:/data/avatars/
echo ============================================
echo.

java -cp bin;lib/sqlite-jdbc-3.42.0.0.jar app.TestAvatarManager

pause
