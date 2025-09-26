@echo off
echo Setting up Library Database...

echo.
echo Step 1: Compiling Java files...
javac -cp "src" src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Initializing database and creating tables...
java -cp "src" server.InitDatabase

echo.
echo Step 3: Creating borrow_requests table...
java -cp "src" server.CreateBorrowRequestsTable

echo.
echo Step 4: Adding sample data...
java -cp "src" server.AddSampleBorrowRequests

echo.
echo Database setup completed successfully!
echo You can now run the application with: java -cp "src" app.MainApp
pause