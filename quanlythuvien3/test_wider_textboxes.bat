@echo off
echo Testing Improved Book Manager UI with Wider TextBoxes...

echo.
echo Step 1: Compiling Java files...
javac -cp "src" src/client/BookManagerUI.java src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Starting Book Manager with Wider TextBoxes...
java -cp "src" client.BookManagerUI

echo.
echo UI Test completed!
pause