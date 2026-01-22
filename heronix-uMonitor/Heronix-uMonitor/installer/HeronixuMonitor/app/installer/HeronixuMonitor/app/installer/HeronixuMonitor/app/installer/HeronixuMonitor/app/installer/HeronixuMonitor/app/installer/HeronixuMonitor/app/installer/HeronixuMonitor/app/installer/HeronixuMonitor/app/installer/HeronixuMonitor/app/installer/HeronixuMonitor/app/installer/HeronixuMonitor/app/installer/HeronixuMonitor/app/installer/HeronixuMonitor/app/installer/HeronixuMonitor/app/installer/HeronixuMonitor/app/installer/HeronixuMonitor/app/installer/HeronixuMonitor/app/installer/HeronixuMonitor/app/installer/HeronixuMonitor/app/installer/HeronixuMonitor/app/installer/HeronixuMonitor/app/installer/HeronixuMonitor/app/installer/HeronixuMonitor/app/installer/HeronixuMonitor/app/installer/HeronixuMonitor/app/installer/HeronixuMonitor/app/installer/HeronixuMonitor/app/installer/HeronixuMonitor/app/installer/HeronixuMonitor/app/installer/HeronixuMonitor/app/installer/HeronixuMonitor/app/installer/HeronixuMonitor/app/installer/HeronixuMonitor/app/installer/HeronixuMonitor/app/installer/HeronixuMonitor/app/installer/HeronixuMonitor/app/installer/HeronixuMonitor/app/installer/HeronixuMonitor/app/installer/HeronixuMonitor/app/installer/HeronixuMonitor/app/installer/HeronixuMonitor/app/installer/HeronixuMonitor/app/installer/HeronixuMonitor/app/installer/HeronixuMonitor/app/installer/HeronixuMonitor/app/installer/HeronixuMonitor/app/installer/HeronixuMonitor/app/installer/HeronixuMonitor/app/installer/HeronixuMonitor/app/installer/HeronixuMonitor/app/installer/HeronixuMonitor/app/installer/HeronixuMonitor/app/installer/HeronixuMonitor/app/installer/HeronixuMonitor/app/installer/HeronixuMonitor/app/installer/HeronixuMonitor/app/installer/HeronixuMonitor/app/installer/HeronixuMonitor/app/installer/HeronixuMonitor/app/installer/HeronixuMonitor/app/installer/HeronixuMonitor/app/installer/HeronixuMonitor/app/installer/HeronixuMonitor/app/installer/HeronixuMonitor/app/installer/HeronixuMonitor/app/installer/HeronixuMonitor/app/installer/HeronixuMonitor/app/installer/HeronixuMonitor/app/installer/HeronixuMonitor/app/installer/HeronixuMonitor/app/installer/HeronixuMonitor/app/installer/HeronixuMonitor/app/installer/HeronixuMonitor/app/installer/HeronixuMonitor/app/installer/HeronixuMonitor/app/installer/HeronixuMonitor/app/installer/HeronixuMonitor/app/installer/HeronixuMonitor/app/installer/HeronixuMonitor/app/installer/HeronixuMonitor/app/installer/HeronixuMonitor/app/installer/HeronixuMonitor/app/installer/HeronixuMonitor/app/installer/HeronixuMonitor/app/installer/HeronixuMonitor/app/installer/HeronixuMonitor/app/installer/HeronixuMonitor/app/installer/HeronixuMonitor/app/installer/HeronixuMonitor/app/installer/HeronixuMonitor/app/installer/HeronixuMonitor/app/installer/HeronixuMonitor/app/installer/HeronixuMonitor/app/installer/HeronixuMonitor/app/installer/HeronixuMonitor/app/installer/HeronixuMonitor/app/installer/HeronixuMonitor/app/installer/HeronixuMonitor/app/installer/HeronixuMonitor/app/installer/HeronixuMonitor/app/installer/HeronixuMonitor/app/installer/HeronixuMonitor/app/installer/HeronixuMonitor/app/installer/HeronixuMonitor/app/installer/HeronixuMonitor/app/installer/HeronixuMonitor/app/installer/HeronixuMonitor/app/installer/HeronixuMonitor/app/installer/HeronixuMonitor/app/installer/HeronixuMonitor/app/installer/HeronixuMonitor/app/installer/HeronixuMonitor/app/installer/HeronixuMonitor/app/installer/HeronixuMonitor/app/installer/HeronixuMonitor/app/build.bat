@echo off
echo Building Heronix uMonitor...
echo.

REM Clean previous build
if exist bin rmdir /s /q bin
mkdir bin

REM Compile all Java files
echo Compiling Java files...
javac -d bin *.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo.
    echo To run the application:
    echo   java -cp bin HeronixuMonitor
    echo.
    echo To create JAR file, run: build-jar.bat
) else (
    echo.
    echo Build failed! Please check for errors.
)

pause
