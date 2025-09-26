@echo off
echo Compiling Java files...
javac -cp "." -d bin src\**\*.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Running sample data setup...
java -cp bin server.AddSampleBooks

echo Starting application...
java -cp bin app.MainApp

pause