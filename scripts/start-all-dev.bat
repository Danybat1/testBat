@echo off
echo ========================================
echo    FreightOps - Lancement Complet Dev
echo ========================================

echo.
echo [1/3] Demarrage du Backend Spring Boot...
start "Backend" cmd /k "cd /d %~dp0..\backend && mvnw.cmd spring-boot:run"

echo.
echo [2/3] Demarrage Frontend Principal (port 4200)...
start "Frontend" cmd /k "cd /d %~dp0..\frontend && npm start"

echo.
echo [3/3] Demarrage Frontend2 Public (port 4201)...
start "Frontend2" cmd /k "cd /d %~dp0..\frontend2 && npm start"

echo.
echo ========================================
echo Applications en cours de demarrage...
echo.
echo Backend:    http://localhost:8080
echo Frontend:   http://localhost:4200
echo Frontend2:  http://localhost:4201
echo ========================================
echo.
echo Appuyez sur une touche pour fermer cette fenetre...
pause >nul
