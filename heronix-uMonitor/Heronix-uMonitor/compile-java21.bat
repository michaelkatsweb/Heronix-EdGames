@echo off
echo ================================================================================
echo    Heronix-uMonitor v1.1 - Java 21 Compilation
echo    By Michael Katsaros
echo ================================================================================
echo.

REM Clean
echo [1/3] Cleaning previous build...
if exist bin rmdir /s /q bin
if exist HeronixuMonitor.jar del /q HeronixuMonitor.jar
mkdir bin
echo Done.
echo.

REM Compile with Java 21 features enabled
echo [2/3] Compiling with Java 21...
"C:\Program Files\Java\jdk-21\bin\javac.exe" ^
    --release 21 ^
    --enable-preview ^
    -d bin ^
    -Xlint:preview ^
    *.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    echo.
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Create JAR
echo [3/3] Creating JAR file...
"C:\Program Files\Java\jdk-21\bin\jar.exe" cvfe HeronixuMonitor.jar HeronixuMonitor -C bin .

if exist HeronixuMonitor.jar (
    echo.
    echo ================================================================================
    echo    SUCCESS! Java 21 build complete!
    echo ================================================================================
    echo.
    echo File: HeronixuMonitor.jar
    dir HeronixuMonitor.jar | find "HeronixuMonitor.jar"
    echo.
    echo To run with Java 21:
    echo   java --enable-preview -jar HeronixuMonitor.jar
    echo.
    echo Or just double-click: HeronixuMonitor.jar
    echo.
) else (
    echo JAR creation failed!
)

pause
