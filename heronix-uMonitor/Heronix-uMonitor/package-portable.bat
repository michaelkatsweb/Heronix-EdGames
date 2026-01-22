@echo off
REM ============================================================================
REM Create Portable ZIP Package - Heronix-uMonitor
REM ============================================================================

echo ========================================
echo Creating Portable Package
echo ========================================
echo.

REM Build with Maven
echo [1/5] Building with Maven...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

REM Create dist directory
echo [2/5] Creating distribution directory...
if not exist "dist" mkdir dist
if exist "dist\HeronixuMonitor-Portable" rmdir /s /q "dist\HeronixuMonitor-Portable"
mkdir dist\HeronixuMonitor-Portable

REM Copy JAR
echo [3/5] Copying JAR file...
copy target\HeronixuMonitor-1.1.4-optimized.jar dist\HeronixuMonitor-Portable\

REM Create Windows run script
echo [4/5] Creating run scripts...
echo @echo off > dist\HeronixuMonitor-Portable\run.bat
echo echo Starting Heronix-uMonitor... >> dist\HeronixuMonitor-Portable\run.bat
echo java -XX:+UseZGC -XX:+UseStringDeduplication -Xmx256m -jar HeronixuMonitor-1.1.4-optimized.jar >> dist\HeronixuMonitor-Portable\run.bat
echo pause >> dist\HeronixuMonitor-Portable\run.bat

REM Create Linux/Mac run script
echo #!/bin/bash > dist\HeronixuMonitor-Portable\run.sh
echo echo "Starting Heronix-uMonitor..." >> dist\HeronixuMonitor-Portable\run.sh
echo java -XX:+UseZGC -XX:+UseStringDeduplication -Xmx256m -jar HeronixuMonitor-1.1.4-optimized.jar >> dist\HeronixuMonitor-Portable\run.sh

REM Create README
echo Heronix-uMonitor v1.1.4-optimized > dist\HeronixuMonitor-Portable\README.txt
echo ================================== >> dist\HeronixuMonitor-Portable\README.txt
echo. >> dist\HeronixuMonitor-Portable\README.txt
echo Professional system monitoring application >> dist\HeronixuMonitor-Portable\README.txt
echo. >> dist\HeronixuMonitor-Portable\README.txt
echo REQUIREMENTS: >> dist\HeronixuMonitor-Portable\README.txt
echo - Java 21 or newer >> dist\HeronixuMonitor-Portable\README.txt
echo. >> dist\HeronixuMonitor-Portable\README.txt
echo TO RUN: >> dist\HeronixuMonitor-Portable\README.txt
echo - Windows: Double-click run.bat >> dist\HeronixuMonitor-Portable\README.txt
echo - Linux/Mac: chmod +x run.sh and then ./run.sh >> dist\HeronixuMonitor-Portable\README.txt
echo - Or manually: java -jar HeronixuMonitor-1.1.4-optimized.jar >> dist\HeronixuMonitor-Portable\README.txt
echo. >> dist\HeronixuMonitor-Portable\README.txt
echo FEATURES: >> dist\HeronixuMonitor-Portable\README.txt
echo - 7 monitoring panels (Overview, Performance, Processes, Network, Ports, CPU-ID, Diagnostics) >> dist\HeronixuMonitor-Portable\README.txt
echo - Task Manager-level performance >> dist\HeronixuMonitor-Portable\README.txt
echo - Native Windows API integration >> dist\HeronixuMonitor-Portable\README.txt
echo - 75%% less CPU usage, 95%% faster updates >> dist\HeronixuMonitor-Portable\README.txt
echo. >> dist\HeronixuMonitor-Portable\README.txt
echo For more information, visit the documentation folder. >> dist\HeronixuMonitor-Portable\README.txt

REM Copy documentation
echo [5/5] Creating ZIP package...
copy README.md dist\HeronixuMonitor-Portable\ > nul 2>&1
copy QUICK_START.md dist\HeronixuMonitor-Portable\ > nul 2>&1

REM Create ZIP
cd dist
if exist HeronixuMonitor-1.1.4-Portable.zip del HeronixuMonitor-1.1.4-Portable.zip
powershell -ExecutionPolicy Bypass -Command "Compress-Archive -Path HeronixuMonitor-Portable -DestinationPath HeronixuMonitor-1.1.4-Portable.zip -Force"
cd ..

echo.
echo ========================================
echo Portable Package Created Successfully!
echo ========================================
echo.
echo Output: dist\HeronixuMonitor-1.1.4-Portable.zip
echo Size:
dir dist\HeronixuMonitor-1.1.4-Portable.zip | find "HeronixuMonitor"
echo.
echo This package contains:
echo - HeronixuMonitor-1.1.4-optimized.jar
echo - run.bat (Windows)
echo - run.sh (Linux/Mac)
echo - README.txt
echo - Documentation
echo.
echo Users just need Java 21+ to run!
echo.
pause
