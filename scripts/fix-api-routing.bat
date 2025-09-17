@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    CORRECTION ROUTAGE API LTA
echo ========================================
echo.

cd /d "%~dp0..\frontend"

echo [1] Vérification de la configuration proxy...
if not exist "proxy.conf.js" (
    echo ❌ proxy.conf.js manquant
    goto :end
)

echo ✅ proxy.conf.js trouvé
type proxy.conf.js
echo.

echo [2] Vérification de la configuration Angular...
if not exist "angular.json" (
    echo ❌ angular.json manquant
    goto :end
)

findstr "proxyConfig" angular.json
if %errorlevel% neq 0 (
    echo ❌ Configuration proxy manquante dans angular.json
    goto :end
)
echo ✅ Configuration proxy trouvée dans angular.json

echo.
echo [3] Arrêt des processus Angular existants...
taskkill /f /im node.exe 2>nul
timeout /t 2 >nul

echo.
echo [4] Nettoyage du cache...
if exist ".angular" rmdir /s /q ".angular"
if exist "node_modules\.cache" rmdir /s /q "node_modules\.cache"

echo.
echo [5] Vérification de la configuration environment...
findstr "apiUrl" src\environments\environment.ts
echo.

echo [6] Démarrage du serveur avec proxy...
echo.
echo ⚠️  IMPORTANT: Le serveur va démarrer avec la configuration proxy.
echo    Vérifiez dans les logs que le proxy est bien configuré.
echo    Vous devriez voir des messages comme:
echo    "[HPM] Proxy created: /api/*  -> http://localhost:8080"
echo.

echo Commande utilisée: npm start
echo (équivaut à: ng serve --proxy-config proxy.conf.js --configuration development)
echo.

pause

echo Démarrage en cours...
npm start

:end
echo.
echo ========================================
echo    SCRIPT TERMINÉ
echo ========================================
pause
