@echo off
setlocal

echo ================================================================================
echo    Building Heronix-uMonitor v1.1
echo    By Michael Katsaros
echo ================================================================================
echo.

REM Find Java home
for /f "tokens=*" %%i in ('where java') do set JAVA_EXE=%%i
for %%i in ("%JAVA_EXE%") do set JAVA_DIR=%%~dpi
set JAVA_HOME=%JAVA_DIR%..\..\..

echo Using Java from: %JAVA_HOME%
echo.

REM Clean
echo Cleaning...
if exist bin rmdir /s /q bin
if exist HeronixuMonitor.jar del /q HeronixuMonitor.jar
mkdir bin

REM Compile
echo Compiling...
"%JAVA_HOME%\bin\javac" -d bin *.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Create manifest
echo Main-Class: HeronixuMonitor > bin\MANIFEST.MF

REM Create JAR
echo Creating JAR...
cd bin
"%JAVA_HOME%\bin\jar" cvfm ..\HeronixuMonitor.jar MANIFEST.MF *.class
cd ..

if exist HeronixuMonitor.jar (
    echo.
    echo ================================================================================
    echo SUCCESS! File created: HeronixuMonitor.jar
    echo ================================================================================
    echo.
    echo To run: java -jar HeronixuMonitor.jar
    echo Or just double-click: HeronixuMonitor.jar
    echo.
) else (
    echo JAR creation failed!
)

pause
