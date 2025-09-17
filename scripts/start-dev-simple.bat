@echo off
echo Starting FreightOps Development (Simple Mode)...
echo.

echo [1/2] Starting Backend (Spring Boot)...
cd /d "%~dp0..\backend"
start "FreightOps Backend" cmd /k "mvn spring-boot:run"

echo.
echo [2/2] Starting Frontend (Angular - No SSR)...
cd /d "%~dp0..\frontend"
start "FreightOps Frontend" cmd /k "ng serve --port 4200 --host 0.0.0.0 --configuration development --disable-host-check"

echo.
echo ================================
echo FreightOps Development Started!
echo ================================
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:4200
echo.
echo Demo Accounts:
echo - Admin:   admin@freightops.com / admin123
echo - Agent:   agent@freightops.com / agent123  
echo - Finance: finance@freightops.com / finance123
echo.
pause
