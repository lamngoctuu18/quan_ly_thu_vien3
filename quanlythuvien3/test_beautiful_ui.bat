@echo off
echo Testing Beautiful Book Manager UI...

echo.
echo Step 1: Compiling all Java files...
javac -cp "src" src/client/BookManagerUI.java src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Setting up database...
java -cp "src" server.InitDatabase

echo.
echo Step 3: Creating borrow_requests table...
java -cp "src" server.CreateBorrowRequestsTable

echo.
echo Step 4: Starting Beautiful Book Manager UI...
java -cp "src" client.BookManagerUI

echo.
echo Test completed!
pause