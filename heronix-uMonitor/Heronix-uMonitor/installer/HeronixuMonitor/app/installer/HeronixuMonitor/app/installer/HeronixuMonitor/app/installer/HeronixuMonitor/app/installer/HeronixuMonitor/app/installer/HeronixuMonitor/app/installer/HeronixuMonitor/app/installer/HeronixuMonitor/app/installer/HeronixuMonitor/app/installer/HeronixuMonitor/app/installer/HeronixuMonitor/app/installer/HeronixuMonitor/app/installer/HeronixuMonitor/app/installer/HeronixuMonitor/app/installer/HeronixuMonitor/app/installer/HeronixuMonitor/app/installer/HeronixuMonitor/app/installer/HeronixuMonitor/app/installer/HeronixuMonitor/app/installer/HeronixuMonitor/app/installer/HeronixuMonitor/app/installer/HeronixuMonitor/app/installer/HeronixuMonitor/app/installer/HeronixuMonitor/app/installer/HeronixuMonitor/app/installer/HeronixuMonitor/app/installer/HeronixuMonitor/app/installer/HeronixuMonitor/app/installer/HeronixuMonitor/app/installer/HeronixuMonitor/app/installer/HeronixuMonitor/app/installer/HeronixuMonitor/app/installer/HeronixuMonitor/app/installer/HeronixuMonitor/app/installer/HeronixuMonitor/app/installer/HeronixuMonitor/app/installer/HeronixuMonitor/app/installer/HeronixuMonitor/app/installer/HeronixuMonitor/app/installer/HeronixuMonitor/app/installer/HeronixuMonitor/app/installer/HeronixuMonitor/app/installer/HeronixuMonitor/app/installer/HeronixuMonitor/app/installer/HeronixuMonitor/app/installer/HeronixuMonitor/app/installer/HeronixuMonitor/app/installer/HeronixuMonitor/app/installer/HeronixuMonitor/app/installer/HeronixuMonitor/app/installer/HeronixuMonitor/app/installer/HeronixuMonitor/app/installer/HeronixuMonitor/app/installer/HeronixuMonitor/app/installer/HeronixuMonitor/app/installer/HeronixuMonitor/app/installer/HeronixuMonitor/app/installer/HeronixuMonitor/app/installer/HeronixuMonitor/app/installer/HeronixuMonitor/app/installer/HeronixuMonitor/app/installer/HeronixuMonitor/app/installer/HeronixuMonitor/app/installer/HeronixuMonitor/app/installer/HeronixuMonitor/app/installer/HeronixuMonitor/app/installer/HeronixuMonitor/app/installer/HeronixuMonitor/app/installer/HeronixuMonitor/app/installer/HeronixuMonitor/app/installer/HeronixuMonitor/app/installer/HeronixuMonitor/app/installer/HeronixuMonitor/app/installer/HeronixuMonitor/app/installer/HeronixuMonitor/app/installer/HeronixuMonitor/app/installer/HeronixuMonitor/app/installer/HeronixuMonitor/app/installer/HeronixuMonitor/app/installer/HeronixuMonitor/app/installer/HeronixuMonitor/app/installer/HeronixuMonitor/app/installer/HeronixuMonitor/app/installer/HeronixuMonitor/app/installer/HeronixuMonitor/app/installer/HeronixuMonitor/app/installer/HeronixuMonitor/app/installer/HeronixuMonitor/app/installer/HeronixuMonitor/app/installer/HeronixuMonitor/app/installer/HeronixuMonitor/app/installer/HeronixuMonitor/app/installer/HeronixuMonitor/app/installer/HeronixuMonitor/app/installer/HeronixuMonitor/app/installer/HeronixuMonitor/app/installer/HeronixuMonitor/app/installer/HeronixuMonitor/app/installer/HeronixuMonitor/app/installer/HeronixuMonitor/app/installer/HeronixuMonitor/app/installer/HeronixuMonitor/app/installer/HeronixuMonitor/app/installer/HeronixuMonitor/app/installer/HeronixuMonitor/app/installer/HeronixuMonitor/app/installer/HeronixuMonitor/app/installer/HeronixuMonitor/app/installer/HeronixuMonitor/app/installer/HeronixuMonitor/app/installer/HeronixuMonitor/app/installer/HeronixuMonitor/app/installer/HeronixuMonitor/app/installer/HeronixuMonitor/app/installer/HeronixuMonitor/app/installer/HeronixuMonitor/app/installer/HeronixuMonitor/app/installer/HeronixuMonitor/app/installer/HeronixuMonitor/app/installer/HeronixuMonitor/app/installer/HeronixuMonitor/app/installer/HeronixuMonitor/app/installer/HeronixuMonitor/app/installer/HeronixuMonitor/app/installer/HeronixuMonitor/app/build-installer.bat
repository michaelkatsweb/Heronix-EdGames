@echo off
echo ================================================================================
echo    Heronix-uMonitor v1.1 - Professional Installer Builder
echo    By Michael Katsaros
echo ================================================================================
echo.

REM Clean previous builds
echo [1/5] Cleaning previous builds...
if exist bin rmdir /s /q bin
if exist installer rmdir /s /q installer
if exist HeronixuMonitor.jar del /q HeronixuMonitor.jar
mkdir bin
echo Done.
echo.

REM Compile Java files
echo [2/5] Compiling Java source files...
javac -d bin *.java 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    echo Please check for errors above.
    pause
    exit /b 1
)
echo Compilation successful!
echo.

REM Create JAR manifest
echo [3/5] Creating JAR file...
echo Main-Class: HeronixuMonitor > bin\MANIFEST.MF
echo.

REM Build JAR file
cd bin
jar cvfm ..\HeronixuMonitor.jar MANIFEST.MF *.class
cd ..

if not exist HeronixuMonitor.jar (
    echo ERROR: JAR file creation failed!
    pause
    exit /b 1
)

echo JAR file created successfully!
echo.

REM Create installer directory
mkdir installer

REM Build Windows installer with jpackage
echo [4/5] Building Windows installer (this may take a few minutes)...
echo.

jpackage ^
    --input . ^
    --name "HeronixuMonitor" ^
    --main-jar HeronixuMonitor.jar ^
    --main-class HeronixuMonitor ^
    --type exe ^
    --dest installer ^
    --app-version 1.1 ^
    --description "Heronix-uMonitor - Professional System Monitoring Tool by Michael Katsaros" ^
    --vendor "Michael Katsaros" ^
    --copyright "Copyright 2025 Michael Katsaros" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --icon icon.ico

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo WARNING: jpackage encountered an issue.
    echo This might be because icon.ico is missing (optional).
    echo The installer may still have been created.
    echo.
)

echo.
echo [5/5] Finalizing...
echo.

REM Check if installer was created
if exist installer\HeronixuMonitor-1.1.exe (
    echo ================================================================================
    echo    SUCCESS! Installer created successfully!
    echo ================================================================================
    echo.
    echo Location: installer\HeronixuMonitor-1.1.exe
    echo Size:
    dir installer\HeronixuMonitor-1.1.exe | find "HeronixuMonitor-1.1.exe"
    echo.
    echo The installer includes:
    echo   - Full application with all features
    echo   - Bundled Java Runtime (no Java installation needed!)
    echo   - Start Menu shortcuts
    echo   - Desktop shortcut option
    echo   - Uninstaller
    echo.
    echo You can now:
    echo   1. Double-click installer\HeronixuMonitor-1.1.exe to install
    echo   2. Copy the .exe file to any PC and install
    echo   3. No Java required on target PC!
    echo.
    echo The JAR file is also available: HeronixuMonitor.jar
    echo   - Run with: java -jar HeronixuMonitor.jar
    echo   - Requires Java 11+ on target PC
    echo.
) else (
    echo ================================================================================
    echo    Installer not found, but JAR file is ready!
    echo ================================================================================
    echo.
    echo JAR Location: HeronixuMonitor.jar
    echo.
    echo The JAR file can be run on any PC with Java installed:
    echo   java -jar HeronixuMonitor.jar
    echo.
    echo To create the Windows installer, you may need:
    echo   - WiX Toolset (for .exe generation on Windows)
    echo   - Download from: https://wixtoolset.org/
    echo.
)

echo.
echo ================================================================================
pause
