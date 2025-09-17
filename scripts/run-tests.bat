@echo off
echo FreightOps Test Suite Runner
echo ============================
echo.

set /p choice="Run which tests? (1=Backend, 2=Frontend Unit, 3=Frontend E2E, 4=All): "

if "%choice%"=="1" goto backend_tests
if "%choice%"=="2" goto frontend_unit
if "%choice%"=="3" goto frontend_e2e
if "%choice%"=="4" goto all_tests
goto invalid_choice

:backend_tests
echo.
echo [Running Backend Tests (JUnit + Spring Boot Test)]
cd /d "%~dp0..\backend"
call mvn test
goto end

:frontend_unit
echo.
echo [Running Frontend Unit Tests (Jest)]
cd /d "%~dp0..\frontend"
call npm test -- --coverage --watchAll=false
goto end

:frontend_e2e
echo.
echo [Running Frontend E2E Tests (Cypress)]
echo Starting development servers first...
cd /d "%~dp0..\backend"
start "Backend Test" cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=test"
timeout /t 15 /nobreak > nul

cd /d "%~dp0..\frontend"
start "Frontend Test" cmd /c "npm start"
timeout /t 10 /nobreak > nul

echo Running Cypress tests...
call npm run e2e:run
goto end

:all_tests
echo.
echo [Running All Tests]
echo.
echo [1/3] Backend Tests...
cd /d "%~dp0..\backend"
call mvn test
if %errorlevel% neq 0 (
    echo Backend tests failed!
    goto end
)

echo.
echo [2/3] Frontend Unit Tests...
cd /d "%~dp0..\frontend"
call npm test -- --coverage --watchAll=false
if %errorlevel% neq 0 (
    echo Frontend unit tests failed!
    goto end
)

echo.
echo [3/3] Frontend E2E Tests...
echo Starting servers...
cd /d "%~dp0..\backend"
start "Backend Test" cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=test"
timeout /t 15 /nobreak > nul

cd /d "%~dp0..\frontend"
start "Frontend Test" cmd /c "npm start"
timeout /t 10 /nobreak > nul

call npm run e2e:run
goto end

:invalid_choice
echo Invalid choice. Please run the script again.
goto end

:end
echo.
echo Test execution completed.
pause
