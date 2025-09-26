@echo off
echo Khoi tao co so du lieu...
cd /d "c:\ReactJS\bankingWeb\bankingwebsite\quan_ly_thu_vien3\quanlythuvien3"

echo Bien dich cac file...
javac -cp "src" src/server/InitDatabase.java
javac -cp "src" src/server/AddSampleBooks.java
javac -cp "src" src/app/MainApp.java src/client/ClientUI.java src/model/*.java src/dao/*.java src/server/*.java

echo Khoi tao co so du lieu...
java -cp "src" server.InitDatabase

echo Them du lieu mau...
java -cp "src" server.AddSampleBooks

echo Hoan thanh! Co the chay ung dung bang lenh:
echo java -cp "src" app.MainApp

pause