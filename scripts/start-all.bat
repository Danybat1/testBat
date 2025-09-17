@echo off
echo Starting FreightOps Complete System...
echo.

echo [1/3] Starting Backend...
start "FreightOps Backend" cmd /k "cd /d %~dp0\..\backend && mvn spring-boot:run"
timeout /t 5

echo [2/3] Starting Admin Frontend...
start "FreightOps Admin" cmd /k "cd /d %~dp0\..\frontend && npm start"
timeout /t 3

echo [3/3] Starting Public Frontend...
start "FreightOps Public" cmd /k "cd /d %~dp0\..\frontend2 && npm start"

echo.
echo All services are starting...
echo Backend: http://localhost:8080
echo Admin Frontend: http://localhost:4200
echo Public Frontend: http://localhost:4201
echo.
pause
