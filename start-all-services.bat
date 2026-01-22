@echo off
REM ============================================================================
REM Heronix Platform - Master Startup Script
REM ============================================================================
REM Starts all core Heronix services in the correct order
REM
REM Services (in startup order):
REM   1. Heronix-SIS API Server (port 9580) - Central hub, starts first
REM   2. Heronix-Talk (port 9680) - Messaging server
REM   3. Heronix-SchedulerV2 (port 8090) - AI Scheduler
REM   4. Heronix-EdGames (port 8081) - Educational Games Server
REM
REM Desktop apps (start manually as needed):
REM   - Heronix-Teacher (port 58280) - Teacher Portal
REM   - Heronix-Student (port 58180) - Student Portal
REM
REM ============================================================================

echo.
echo ============================================================================
echo   HERONIX PLATFORM - STARTING ALL SERVICES
echo ============================================================================
echo.
echo Starting services in order:
echo   1. Heronix-SIS API Server (port 9580)
echo   2. Heronix-Talk (port 9680)
echo   3. Heronix-SchedulerV2 (port 8090)
echo   4. Heronix-EdGames Server (port 8081)
echo.

REM ============================================================================
REM 1. Start Heronix-SIS API Server (must start first - central hub)
REM ============================================================================
echo [1/4] Starting Heronix-SIS API Server on port 9580...
cd /d h:\Heronix\Heronix-SIS
start "Heronix-SIS" cmd /c "mvn spring-boot:run -Dspring-boot.run.mainClass=com.heronix.HeronixSchedulerApiApplication 2>&1 | tee logs\sis-api-server.log"

REM Wait for SIS to initialize before starting dependent services
echo      Waiting 30 seconds for SIS to initialize...
timeout /t 30 /nobreak > nul

REM ============================================================================
REM 2. Start Heronix-Talk (depends on SIS for teacher sync)
REM ============================================================================
echo [2/4] Starting Heronix-Talk on port 9680...
cd /d h:\Heronix\Heronix-Talk
start "Heronix-Talk" cmd /c "mvn spring-boot:run 2>&1 | tee logs\talk-server.log"

REM Wait for Talk to initialize
echo      Waiting 15 seconds for Talk to initialize...
timeout /t 15 /nobreak > nul

REM ============================================================================
REM 3. Start Heronix-SchedulerV2 (depends on SIS for data)
REM ============================================================================
echo [3/4] Starting Heronix-SchedulerV2 on port 8090...
cd /d h:\Heronix\Heronix-SchedulerV2
start "Heronix-SchedulerV2" cmd /c "mvn spring-boot:run 2>&1 | tee logs\scheduler-v2.log"

REM Wait for SchedulerV2 to initialize
echo      Waiting 15 seconds for SchedulerV2 to initialize...
timeout /t 15 /nobreak > nul

REM ============================================================================
REM 4. Start Heronix-EdGames Server
REM ============================================================================
echo [4/4] Starting Heronix-EdGames Server on port 8081...
cd /d h:\Heronix\Heronix-EdGames\heronix-edu-games\heronix-server
start "Heronix-EdGames" cmd /c "mvn spring-boot:run 2>&1 | tee logs\edgames-server.log"

REM ============================================================================
REM Summary
REM ============================================================================
echo.
echo ============================================================================
echo   ALL SERVICES STARTED
echo ============================================================================
echo.
echo Service Endpoints:
echo   Heronix-SIS:        http://localhost:9580
echo     - Health:         http://localhost:9580/actuator/health
echo     - H2 Console:     http://localhost:9580/h2-console
echo     - API Docs:       http://localhost:9580/swagger-ui.html
echo.
echo   Heronix-Talk:       http://localhost:9680
echo     - WebSocket:      ws://localhost:9680/ws/chat
echo.
echo   Heronix-SchedulerV2: http://localhost:8090
echo     - API Docs:       http://localhost:8090/swagger-ui.html
echo.
echo   Heronix-EdGames:    http://localhost:8081
echo     - API Docs:       http://localhost:8081/swagger-ui.html
echo.
echo Desktop Apps (start separately):
echo   - Heronix-Teacher: Run from Heronix-Teacher folder
echo   - Heronix-Student: Run from Heronix-Student folder
echo.
echo Press any key to exit (services will continue running)...
pause > nul
