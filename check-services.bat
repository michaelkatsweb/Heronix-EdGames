@echo off
REM ============================================================================
REM Heronix Platform - Service Status Check
REM ============================================================================
REM Checks the status of all Heronix services
REM ============================================================================

echo.
echo ============================================================================
echo   HERONIX PLATFORM - SERVICE STATUS
echo ============================================================================
echo.

echo Checking services...
echo.

REM Check Heronix-SIS (port 9580)
echo [Heronix-SIS API] Port 9580:
netstat -an | findstr ":9580.*LISTENING" > nul
if %errorlevel%==0 (
    echo   Status: RUNNING
    curl -s http://localhost:9580/actuator/health 2>nul | findstr "UP" > nul
    if %errorlevel%==0 (
        echo   Health: HEALTHY
    ) else (
        echo   Health: UNHEALTHY or STARTING
    )
) else (
    echo   Status: STOPPED
)
echo.

REM Check Heronix-Talk (port 9680)
echo [Heronix-Talk] Port 9680:
netstat -an | findstr ":9680.*LISTENING" > nul
if %errorlevel%==0 (
    echo   Status: RUNNING
) else (
    echo   Status: STOPPED
)
echo.

REM Check Heronix-SchedulerV2 (port 8090)
echo [Heronix-SchedulerV2] Port 8090:
netstat -an | findstr ":8090.*LISTENING" > nul
if %errorlevel%==0 (
    echo   Status: RUNNING
) else (
    echo   Status: STOPPED
)
echo.

REM Check Heronix-EdGames (port 8081)
echo [Heronix-EdGames] Port 8081:
netstat -an | findstr ":8081.*LISTENING" > nul
if %errorlevel%==0 (
    echo   Status: RUNNING
) else (
    echo   Status: STOPPED
)
echo.

REM Check Heronix-Teacher (port 58280)
echo [Heronix-Teacher] Port 58280:
netstat -an | findstr ":58280.*LISTENING" > nul
if %errorlevel%==0 (
    echo   Status: RUNNING
) else (
    echo   Status: STOPPED (Desktop app - start manually)
)
echo.

REM Check Heronix-Student (port 58180)
echo [Heronix-Student] Port 58180:
netstat -an | findstr ":58180.*LISTENING" > nul
if %errorlevel%==0 (
    echo   Status: RUNNING
) else (
    echo   Status: STOPPED (Desktop app - start manually)
)
echo.

echo ============================================================================
echo   PORT SUMMARY
echo ============================================================================
echo   9580  - Heronix-SIS API Server (Central Hub)
echo   9680  - Heronix-Talk (Messaging)
echo   8090  - Heronix-SchedulerV2 (AI Scheduler)
echo   8081  - Heronix-EdGames (Educational Games)
echo   58280 - Heronix-Teacher (Desktop Portal)
echo   58180 - Heronix-Student (Desktop Portal)
echo ============================================================================
echo.
pause
