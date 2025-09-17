@echo off
echo Starting FreightOps Development Environment...
echo.

echo [1/4] Starting Docker infrastructure...
cd /d "%~dp0..\backend"
docker-compose up -d postgres redis rabbitmq minio

echo.
echo [2/4] Waiting for services to be ready...
timeout /t 10 /nobreak > nul

echo.
echo [3/4] Starting Backend (Spring Boot)...
start "FreightOps Backend" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo.
echo [4/4] Starting Frontend (Angular)...
cd /d "%~dp0..\frontend"
start "FreightOps Frontend" cmd /k "npm start"

echo.
echo ================================
echo FreightOps Development Started!
echo ================================
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:4200
echo pgAdmin:  http://localhost:5050
echo.
echo Demo Accounts:
echo - Admin:   admin@freightops.com / admin123
echo - Agent:   agent@freightops.com / agent123  
echo - Finance: finance@freightops.com / finance123
echo.
pause
