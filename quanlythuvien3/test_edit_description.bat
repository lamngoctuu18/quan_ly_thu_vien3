@echo off
echo Testing Book Manager with Description Field in Edit Dialog...

echo.
echo Step 1: Compiling Java files...
javac -cp "src" src/client/BookManagerUI.java src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Starting Book Manager with Description Edit Feature...
java -cp "src" client.BookManagerUI

echo.
echo Test completed!
echo.
echo Features to test:
echo - Open edit dialog for any book
echo - Check if description field is populated with existing data
echo - Edit description and save
echo - Verify changes are saved to database
pause