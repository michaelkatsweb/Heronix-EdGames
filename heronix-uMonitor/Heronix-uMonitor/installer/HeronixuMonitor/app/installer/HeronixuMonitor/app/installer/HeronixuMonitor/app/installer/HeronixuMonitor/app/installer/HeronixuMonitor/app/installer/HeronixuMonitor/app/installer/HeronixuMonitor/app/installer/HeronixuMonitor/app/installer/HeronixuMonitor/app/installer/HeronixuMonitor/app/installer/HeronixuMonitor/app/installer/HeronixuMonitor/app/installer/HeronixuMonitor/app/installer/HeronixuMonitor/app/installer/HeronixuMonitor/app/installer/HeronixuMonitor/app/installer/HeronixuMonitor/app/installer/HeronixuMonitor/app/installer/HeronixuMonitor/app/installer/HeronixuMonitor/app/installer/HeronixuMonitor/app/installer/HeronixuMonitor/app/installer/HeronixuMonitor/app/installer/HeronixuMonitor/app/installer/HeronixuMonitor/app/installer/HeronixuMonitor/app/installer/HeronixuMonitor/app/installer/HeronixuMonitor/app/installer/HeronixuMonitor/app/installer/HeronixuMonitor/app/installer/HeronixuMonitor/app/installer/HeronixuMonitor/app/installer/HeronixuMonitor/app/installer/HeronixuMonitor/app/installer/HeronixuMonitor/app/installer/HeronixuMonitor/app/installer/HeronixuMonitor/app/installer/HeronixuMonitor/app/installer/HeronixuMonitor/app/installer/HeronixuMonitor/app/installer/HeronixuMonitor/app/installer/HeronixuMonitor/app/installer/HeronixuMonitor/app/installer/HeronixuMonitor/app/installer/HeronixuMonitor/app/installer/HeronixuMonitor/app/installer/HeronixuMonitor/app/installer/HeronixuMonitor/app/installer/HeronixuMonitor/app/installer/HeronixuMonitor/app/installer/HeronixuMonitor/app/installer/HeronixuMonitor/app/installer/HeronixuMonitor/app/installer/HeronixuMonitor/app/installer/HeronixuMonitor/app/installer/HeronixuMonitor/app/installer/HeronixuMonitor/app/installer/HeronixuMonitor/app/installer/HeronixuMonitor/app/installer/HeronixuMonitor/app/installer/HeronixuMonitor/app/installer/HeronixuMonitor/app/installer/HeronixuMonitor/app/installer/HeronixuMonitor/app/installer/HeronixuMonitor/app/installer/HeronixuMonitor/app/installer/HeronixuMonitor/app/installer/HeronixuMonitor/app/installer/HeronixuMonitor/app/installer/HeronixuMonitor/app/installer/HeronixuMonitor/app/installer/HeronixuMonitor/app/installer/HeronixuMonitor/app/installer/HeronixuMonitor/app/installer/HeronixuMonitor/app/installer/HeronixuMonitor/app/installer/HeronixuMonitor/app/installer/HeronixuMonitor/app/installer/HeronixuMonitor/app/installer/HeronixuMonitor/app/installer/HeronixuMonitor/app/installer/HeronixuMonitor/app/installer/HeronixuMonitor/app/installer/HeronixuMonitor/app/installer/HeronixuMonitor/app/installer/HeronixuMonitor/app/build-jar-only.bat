@echo off
echo ================================================================================
echo    Heronix-uMonitor v1.1 - JAR Builder
echo    By Michael Katsaros
echo ================================================================================
echo.

REM Clean previous builds
echo [1/3] Cleaning previous builds...
if exist bin rmdir /s /q bin
if exist HeronixuMonitor.jar del /q HeronixuMonitor.jar
mkdir bin
echo Done.
echo.

REM Compile Java files
echo [2/3] Compiling Java source files...
javac -d bin *.java 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo Compilation successful!
echo.

REM Create JAR
echo [3/3] Creating JAR file...
echo Main-Class: HeronixuMonitor > bin\MANIFEST.MF
cd bin
jar cvfm ..\HeronixuMonitor.jar MANIFEST.MF *.class
cd ..

if exist HeronixuMonitor.jar (
    echo.
    echo ================================================================================
    echo    SUCCESS! JAR file created!
    echo ================================================================================
    echo.
    echo File: HeronixuMonitor.jar
    echo Size:
    dir HeronixuMonitor.jar | find "HeronixuMonitor.jar"
    echo.
    echo To run on this PC:
    echo   java -jar HeronixuMonitor.jar
    echo.
    echo To run on any PC:
    echo   1. Copy HeronixuMonitor.jar to target PC
    echo   2. Make sure Java 11+ is installed
    echo   3. Double-click the JAR file or run: java -jar HeronixuMonitor.jar
    echo.
    echo To create Windows installer, run: build-installer.bat
    echo.
) else (
    echo ERROR: JAR creation failed!
)

echo ================================================================================
pause
