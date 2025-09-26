@echo off
echo Testing Book Manager with Image Edit Feature...

echo.
echo Step 1: Compiling Java files...
javac -cp "src" src/client/BookManagerUI.java src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Starting Book Manager UI...
java -cp "src" client.BookManagerUI

echo.
echo Test completed!
pause