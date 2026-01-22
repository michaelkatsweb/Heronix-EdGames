@echo off
REM ============================================================================
REM Heronix-uMonitor v1.1 - Windows Installer Creator
REM By Michael Katsaros
REM ============================================================================

echo.
echo ============================================================================
echo   HERONIX-uMONITOR v1.1 - INSTALLER CREATOR
echo   Creating Windows installer package...
echo ============================================================================
echo.

REM Set variables
set JAVA_HOME=C:\Program Files\Java\jdk-21
set JPACKAGE="%JAVA_HOME%\bin\jpackage.exe"
set APP_VERSION=1.1
set APP_NAME=Heronix-uMonitor
set MAIN_JAR=HeronixuMonitor.jar
set MAIN_CLASS=HeronixuMonitor
set VENDOR=Michael Katsaros
set COPYRIGHT=Copyright 2025 Michael Katsaros

REM Check if JAR exists
if not exist %MAIN_JAR% (
    echo ERROR: %MAIN_JAR% not found!
    echo Please compile the project first using compile-java21.bat
    pause
    exit /b 1
)

echo [1/4] Preparing installer resources...
echo.

REM Create icon if it doesn't exist (using a simple approach)
REM Note: You can replace icon.ico with a custom icon later

REM Clean previous installer outputs
if exist "installer-output" rd /s /q "installer-output"
mkdir "installer-output"

echo [2/4] Creating Windows EXE installer...
echo.
echo This may take a few minutes...
echo.

REM Create EXE installer with embedded JRE
%JPACKAGE% ^
    --type exe ^
    --input . ^
    --name "%APP_NAME%" ^
    --main-jar %MAIN_JAR% ^
    --main-class %MAIN_CLASS% ^
    --app-version %APP_VERSION% ^
    --vendor "%VENDOR%" ^
    --copyright "%COPYRIGHT%" ^
    --description "Professional System Monitoring Tool" ^
    --dest installer-output ^
    --win-menu ^
    --win-shortcut ^
    --win-dir-chooser ^
    --win-menu-group "Heronix-uMonitor"

if errorlevel 1 (
    echo.
    echo ============================================================================
    echo ERROR: Installer creation failed!
    echo.
    echo Possible causes:
    echo 1. JDK not found at: %JAVA_HOME%
    echo 2. JAR file is corrupted
    echo 3. Insufficient disk space
    echo.
    echo ============================================================================
    pause
    exit /b 1
)

echo.
echo [3/4] Installer created successfully!
echo.

REM Check output
if exist "installer-output\*.exe" (
    echo [4/4] Finalizing...
    echo.
    echo ============================================================================
    echo   SUCCESS! Windows Installer Created
    echo ============================================================================
    echo.
    echo Location: installer-output\
    for %%F in (installer-output\*.exe) do (
        echo File: %%~nxF
        echo Size: %%~zF bytes
    )
    echo.
    echo INSTALLATION FEATURES:
    echo  - Installs to Program Files
    echo  - Creates Start Menu shortcut
    echo  - Creates Desktop shortcut
    echo  - Includes embedded Java runtime (no Java install needed!)
    echo  - Professional Windows installer experience
    echo  - Easy uninstall via Control Panel
    echo.
    echo ============================================================================
    echo.
    echo The installer is ready to distribute!
    echo Users can double-click the .exe to install your application.
    echo.
) else (
    echo ERROR: Installer file not found in output directory!
)

pause
