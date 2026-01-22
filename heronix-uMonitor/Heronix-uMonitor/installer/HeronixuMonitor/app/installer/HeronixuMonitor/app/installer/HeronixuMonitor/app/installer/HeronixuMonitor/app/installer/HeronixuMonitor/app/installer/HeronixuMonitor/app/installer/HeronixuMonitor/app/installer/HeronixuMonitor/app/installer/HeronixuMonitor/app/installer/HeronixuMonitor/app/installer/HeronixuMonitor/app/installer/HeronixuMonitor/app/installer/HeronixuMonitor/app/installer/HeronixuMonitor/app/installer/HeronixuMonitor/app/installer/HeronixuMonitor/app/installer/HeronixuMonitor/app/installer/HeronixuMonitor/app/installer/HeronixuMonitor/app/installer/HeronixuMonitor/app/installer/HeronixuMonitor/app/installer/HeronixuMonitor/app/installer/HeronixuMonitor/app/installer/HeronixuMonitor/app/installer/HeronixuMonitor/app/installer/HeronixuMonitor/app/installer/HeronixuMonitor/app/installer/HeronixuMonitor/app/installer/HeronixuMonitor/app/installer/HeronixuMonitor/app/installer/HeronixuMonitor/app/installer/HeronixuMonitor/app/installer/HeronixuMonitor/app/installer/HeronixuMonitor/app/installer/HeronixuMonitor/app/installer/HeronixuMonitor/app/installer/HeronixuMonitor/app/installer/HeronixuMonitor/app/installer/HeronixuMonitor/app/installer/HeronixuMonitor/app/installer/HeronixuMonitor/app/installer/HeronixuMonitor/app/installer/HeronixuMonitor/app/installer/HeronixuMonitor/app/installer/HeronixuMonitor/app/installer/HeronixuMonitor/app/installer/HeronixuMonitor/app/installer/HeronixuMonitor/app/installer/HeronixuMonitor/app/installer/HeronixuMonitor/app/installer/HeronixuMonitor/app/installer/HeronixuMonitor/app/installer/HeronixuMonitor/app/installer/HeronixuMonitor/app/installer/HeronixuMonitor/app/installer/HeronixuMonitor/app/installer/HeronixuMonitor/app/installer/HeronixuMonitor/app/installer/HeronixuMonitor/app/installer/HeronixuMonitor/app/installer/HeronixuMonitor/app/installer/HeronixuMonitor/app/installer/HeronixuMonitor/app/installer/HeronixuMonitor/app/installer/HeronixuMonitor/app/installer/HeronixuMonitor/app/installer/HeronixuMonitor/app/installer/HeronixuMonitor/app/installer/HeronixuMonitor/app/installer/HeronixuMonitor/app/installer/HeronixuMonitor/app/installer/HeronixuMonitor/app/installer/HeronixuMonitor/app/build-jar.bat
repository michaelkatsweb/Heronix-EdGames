@echo off
echo Building Heronix uMonitor JAR...
echo.

REM Clean and compile
if exist bin rmdir /s /q bin
mkdir bin

echo Compiling Java files...
javac -d bin *.java

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

REM Create manifest
echo Main-Class: HeronixuMonitor > bin\MANIFEST.MF
echo.

REM Create JAR
echo Creating JAR file...
cd bin
jar cvfm ..\HeronixuMonitor.jar MANIFEST.MF *.class
cd ..

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! JAR file created!
    echo ========================================
    echo.
    echo File: HeronixuMonitor.jar
    echo Size:
    dir HeronixuMonitor.jar | find "HeronixuMonitor.jar"
    echo.
    echo To run the application:
    echo   java -jar HeronixuMonitor.jar
    echo.
    echo Or just double-click HeronixuMonitor.jar
    echo.
) else (
    echo.
    echo JAR creation failed!
)

pause
