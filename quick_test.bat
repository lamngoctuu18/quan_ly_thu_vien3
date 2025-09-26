@echo off
echo ========================================
echo Quick Test - Favorites and Activities Fix
echo ========================================

cd "c:\ReactJS\bankingWeb\bankingwebsite\quan_ly_thu_vien3\quanlythuvien3"

echo Compiling...
javac -cp "src" src/app/MainApp.java src/client/*.java src/model/*.java src/dao/*.java src/server/*.java

echo Initializing database...
java -cp "src" server.InitDatabase

echo Starting application...
echo.
echo DEBUG INFO ENABLED:
echo - Console will show data format for favorites and activities
echo - Test the delete functions and check console for debugging
echo.
echo LOGIN INFO:
echo - Admin: admin / admin
echo - Create a test user or use existing ones
echo.

java -cp "src" app.MainApp

pause