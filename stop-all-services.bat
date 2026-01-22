@echo off
REM ============================================================================
REM Heronix Platform - Stop All Services
REM ============================================================================
REM Gracefully stops all running Heronix services
REM ============================================================================

echo.
echo ============================================================================
echo   HERONIX PLATFORM - STOPPING ALL SERVICES
echo ============================================================================
echo.

REM Kill Java processes on specific ports
echo Stopping services by port...

REM Stop Heronix-SIS (port 9580)
echo   Stopping Heronix-SIS (port 9580)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":9580" ^| findstr "LISTENING"') do taskkill /F /PID %%a 2>nul

REM Stop Heronix-Talk (port 9680)
echo   Stopping Heronix-Talk (port 9680)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":9680" ^| findstr "LISTENING"') do taskkill /F /PID %%a 2>nul

REM Stop Heronix-SchedulerV2 (port 8090)
echo   Stopping Heronix-SchedulerV2 (port 8090)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8090" ^| findstr "LISTENING"') do taskkill /F /PID %%a 2>nul

REM Stop Heronix-EdGames (port 8081)
echo   Stopping Heronix-EdGames (port 8081)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8081" ^| findstr "LISTENING"') do taskkill /F /PID %%a 2>nul

REM Stop Heronix-Teacher (port 58280) if running
echo   Stopping Heronix-Teacher (port 58280)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":58280" ^| findstr "LISTENING"') do taskkill /F /PID %%a 2>nul

REM Stop Heronix-Student (port 58180) if running
echo   Stopping Heronix-Student (port 58180)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":58180" ^| findstr "LISTENING"') do taskkill /F /PID %%a 2>nul

echo.
echo ============================================================================
echo   ALL SERVICES STOPPED
echo ============================================================================
echo.
pause
