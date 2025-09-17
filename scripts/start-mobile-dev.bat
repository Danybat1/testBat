@echo off
echo ========================================
echo  FreightOps - Mode Mobile/Reseau Local
echo ========================================

echo.
echo Recherche de l'adresse IP locale...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    for /f "tokens=1" %%b in ("%%a") do set LOCAL_IP=%%b
)

echo Adresse IP detectee: %LOCAL_IP%
echo.

echo [1/3] Demarrage du Backend Spring Boot...
start "Backend" cmd /k "cd /d %~dp0..\backend && mvnw.cmd spring-boot:run"

echo.
echo [2/3] Demarrage Frontend Principal avec acces reseau...
start "Frontend-Mobile" cmd /k "cd /d %~dp0..\frontend && ng serve --host 0.0.0.0 --port 4200 --proxy-config proxy.conf.js"

echo.
echo [3/3] Demarrage Frontend2 Public avec acces reseau...
start "Frontend2-Mobile" cmd /k "cd /d %~dp0..\frontend2 && ng serve --host 0.0.0.0 --port 4201"

echo.
echo ========================================
echo   ACCES DEPUIS VOS APPAREILS MOBILES
echo ========================================
echo.
echo Backend:    http://%LOCAL_IP%:8080
echo Frontend:   http://%LOCAL_IP%:4200
echo Frontend2:  http://%LOCAL_IP%:4201
echo.
echo Assurez-vous que vos appareils sont sur le meme reseau WiFi
echo ========================================
echo.
echo Appuyez sur une touche pour fermer cette fenetre...
pause >nul
