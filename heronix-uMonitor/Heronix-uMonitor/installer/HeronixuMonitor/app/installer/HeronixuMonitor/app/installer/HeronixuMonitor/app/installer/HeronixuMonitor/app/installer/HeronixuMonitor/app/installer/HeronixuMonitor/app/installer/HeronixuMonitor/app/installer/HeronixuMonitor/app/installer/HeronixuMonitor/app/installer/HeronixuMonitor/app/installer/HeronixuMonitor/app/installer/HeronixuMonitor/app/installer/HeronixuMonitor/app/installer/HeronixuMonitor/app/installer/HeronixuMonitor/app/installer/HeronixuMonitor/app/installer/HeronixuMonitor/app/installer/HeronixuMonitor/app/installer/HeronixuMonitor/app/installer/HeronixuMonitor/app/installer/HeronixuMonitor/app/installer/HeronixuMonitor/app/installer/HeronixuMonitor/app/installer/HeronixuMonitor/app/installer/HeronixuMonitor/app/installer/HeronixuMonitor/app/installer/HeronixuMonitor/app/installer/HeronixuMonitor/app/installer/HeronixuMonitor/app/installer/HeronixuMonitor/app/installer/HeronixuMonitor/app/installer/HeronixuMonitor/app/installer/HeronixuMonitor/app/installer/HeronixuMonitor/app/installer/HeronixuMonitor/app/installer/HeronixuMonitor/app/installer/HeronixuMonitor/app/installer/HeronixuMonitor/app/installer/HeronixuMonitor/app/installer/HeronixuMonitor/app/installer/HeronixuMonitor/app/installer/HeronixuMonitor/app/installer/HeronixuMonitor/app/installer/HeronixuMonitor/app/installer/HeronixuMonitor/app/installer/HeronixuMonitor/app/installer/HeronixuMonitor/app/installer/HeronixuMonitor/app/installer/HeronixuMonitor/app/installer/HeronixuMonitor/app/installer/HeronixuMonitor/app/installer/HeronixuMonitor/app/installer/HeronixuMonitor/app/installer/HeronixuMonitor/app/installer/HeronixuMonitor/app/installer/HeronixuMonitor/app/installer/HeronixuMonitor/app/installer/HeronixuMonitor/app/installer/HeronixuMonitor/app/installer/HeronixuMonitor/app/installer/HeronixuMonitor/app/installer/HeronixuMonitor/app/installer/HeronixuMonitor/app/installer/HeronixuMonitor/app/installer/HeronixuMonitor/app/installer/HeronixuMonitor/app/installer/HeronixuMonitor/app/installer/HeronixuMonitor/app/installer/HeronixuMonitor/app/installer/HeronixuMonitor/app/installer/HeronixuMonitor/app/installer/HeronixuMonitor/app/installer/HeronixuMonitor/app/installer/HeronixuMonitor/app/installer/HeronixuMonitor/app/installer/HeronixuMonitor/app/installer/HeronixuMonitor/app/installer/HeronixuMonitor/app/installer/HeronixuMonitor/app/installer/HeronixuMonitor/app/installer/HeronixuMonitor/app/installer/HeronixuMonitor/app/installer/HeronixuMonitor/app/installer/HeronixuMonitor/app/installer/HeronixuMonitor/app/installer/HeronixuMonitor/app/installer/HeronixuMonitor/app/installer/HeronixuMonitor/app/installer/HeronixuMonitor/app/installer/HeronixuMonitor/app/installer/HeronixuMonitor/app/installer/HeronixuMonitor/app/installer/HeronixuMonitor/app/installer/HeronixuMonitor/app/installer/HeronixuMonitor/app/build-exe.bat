@echo off
echo Building Heronix uMonitor Native Executable...
echo.

REM First create the JAR
call build-jar.bat

if not exist HeronixuMonitor.jar (
    echo JAR file not found! Build failed.
    pause
    exit /b 1
)

echo.
echo Creating Windows executable...
echo.

REM Check if jpackage is available
jpackage --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: jpackage not found!
    echo.
    echo jpackage requires JDK 14 or higher.
    echo Current Java version:
    java -version
    echo.
    echo Please install JDK 14+ from:
    echo https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Create output directory
if exist installer rmdir /s /q installer
mkdir installer

REM Build Windows executable
jpackage ^
    --input . ^
    --name "HeronixuMonitor" ^
    --main-jar HeronixuMonitor.jar ^
    --main-class HeronixuMonitor ^
    --type exe ^
    --dest installer ^
    --win-console ^
    --app-version 1.0 ^
    --description "Heronix System Monitor - Real-time monitoring tool" ^
    --vendor "Heronix"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Executable created!
    echo ========================================
    echo.
    echo Location: installer\HeronixuMonitor-1.0.exe
    echo.
    dir installer\*.exe
    echo.
    echo You can now install and run the application!
    echo.
) else (
    echo.
    echo Executable creation failed!
)

pause
