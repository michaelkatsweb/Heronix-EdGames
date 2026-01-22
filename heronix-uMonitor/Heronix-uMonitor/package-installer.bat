@echo off
REM ============================================================================
REM Create Windows Installer (MSI) - Heronix-uMonitor
REM Requires: Java 21+ (jpackage is included)
REM ============================================================================

echo ========================================
echo Creating Windows Installer (MSI)
echo ========================================
echo.

REM Find jpackage
echo Locating jpackage...
where jpackage >nul 2>&1
if errorlevel 1 (
    echo jpackage not in PATH, searching for Java installation...

    REM Try to find jpackage in JAVA_HOME
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jpackage.exe" (
            set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"
            echo Found jpackage at: %JPACKAGE%
        )
    )

    REM If still not found, try to locate via java.exe
    if not defined JPACKAGE (
        for /f "tokens=*" %%i in ('where java 2^>nul') do (
            set "JAVA_PATH=%%i"
            goto :found_java
        )
        :found_java
        if defined JAVA_PATH (
            for %%i in ("!JAVA_PATH!") do set "JAVA_DIR=%%~dpi"
            if exist "!JAVA_DIR!jpackage.exe" (
                set "JPACKAGE=!JAVA_DIR!jpackage.exe"
                echo Found jpackage at: !JPACKAGE!
            )
        )
    )

    REM If still not found, give up
    if not defined JPACKAGE (
        echo.
        echo ERROR: jpackage not found!
        echo.
        echo jpackage is included with Java 21+ JDK.
        echo Please ensure you have JDK 21+ installed (not just JRE).
        echo.
        echo Download from: https://adoptium.net/
        echo.
        echo Alternative: Use the portable ZIP package instead:
        echo   Run: package-portable.bat
        echo.
        pause
        exit /b 1
    )
) else (
    set "JPACKAGE=jpackage"
    echo Found jpackage in PATH
)

REM Build with Maven
echo [1/3] Building with Maven...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

REM Create output directory
echo [2/3] Preparing files...
if not exist "dist" mkdir dist

REM Create installer with jpackage
echo [3/3] Creating MSI installer...
echo This may take several minutes...
echo.

jpackage ^
  --input target ^
  --name HeronixuMonitor ^
  --main-jar HeronixuMonitor-1.1.4-optimized.jar ^
  --main-class HeronixuMonitor ^
  --type msi ^
  --dest dist ^
  --app-version 1.1.4 ^
  --vendor "Heronix" ^
  --description "Professional system monitoring application with Task Manager-level performance" ^
  --copyright "Copyright 2025 Heronix" ^
  --win-dir-chooser ^
  --win-menu ^
  --win-menu-group "Heronix Tools" ^
  --win-shortcut ^
  --win-per-user-install

if errorlevel 1 (
    echo.
    echo ERROR: Installer creation failed!
    echo.
    echo Common issues:
    echo - WiX Toolset not installed (jpackage needs it for MSI)
    echo - Download from: https://wixtoolset.org/
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Installer Created Successfully!
echo ========================================
echo.
echo Output: dist\HeronixuMonitor-1.1.4.msi
echo Size:
dir dist\*.msi | find ".msi"
echo.
echo Features:
echo - Bundled Java Runtime (no Java installation needed!)
echo - Start Menu integration
echo - Desktop shortcut
echo - Add/Remove Programs entry
echo - Professional installation experience
echo.
echo Users can now double-click the MSI to install!
echo.
pause
