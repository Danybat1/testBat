@echo off
echo FreightOps Production Deployment Script
echo =======================================
echo.

echo [1/6] Building Backend...
cd /d "%~dp0..\backend"
call mvn clean package -Pprod -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Backend build failed!
    pause
    exit /b 1
)

echo.
echo [2/6] Building Frontend...
cd /d "%~dp0..\frontend"
call npm run build:prod
if %errorlevel% neq 0 (
    echo ERROR: Frontend build failed!
    pause
    exit /b 1
)

echo.
echo [3/6] Building Docker Images...
cd /d "%~dp0.."
docker build -t freightops-backend:latest ./backend
docker build -t freightops-frontend:latest ./frontend

echo.
echo [4/6] Stopping existing containers...
docker-compose -f docker-compose.prod.yml down

echo.
echo [5/6] Starting production environment...
docker-compose -f docker-compose.prod.yml up -d

echo.
echo [6/6] Waiting for services to start...
timeout /t 30 /nobreak > nul

echo.
echo ================================
echo Production Deployment Complete!
echo ================================
echo Application: https://your-domain.com
echo API Health:  https://your-domain.com/api/actuator/health
echo.
echo Please verify:
echo 1. SSL certificates are properly configured
echo 2. Environment variables are set securely
echo 3. Database migrations have been applied
echo 4. Monitoring is active
echo.
pause
