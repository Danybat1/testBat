@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    DIAGNOSTIC APPELS API LTA
echo ========================================
echo.

:: Variables
set BACKEND_URL=http://localhost:8080
set FRONTEND_URL=http://localhost:4200

echo [1] Test de connectivité backend...
curl -s -o nul -w "Status: %%{http_code}\n" %BACKEND_URL%
if %errorlevel% neq 0 (
    echo ❌ Backend non accessible sur %BACKEND_URL%
    goto :end
)
echo ✅ Backend accessible

echo.
echo [2] Test des endpoints backend directs...
echo.

echo Testing: %BACKEND_URL%/api/lta/stats
curl -s -w "Status: %%{http_code}\n" %BACKEND_URL%/api/lta/stats
echo.

echo Testing: %BACKEND_URL%/api/lta/recent?limit=5
curl -s -w "Status: %%{http_code}\n" %BACKEND_URL%/api/lta/recent?limit=5
echo.

echo [3] Test des endpoints SANS préfixe /api (pour confirmer 404)...
echo.

echo Testing: %BACKEND_URL%/lta/stats
curl -s -w "Status: %%{http_code}\n" %BACKEND_URL%/lta/stats
echo.

echo Testing: %BACKEND_URL%/lta/recent?limit=5
curl -s -w "Status: %%{http_code}\n" %BACKEND_URL%/lta/recent?limit=5
echo.

echo [4] Vérification de la configuration Angular...
echo.

if exist "..\frontend\src\environments\environment.ts" (
    echo Configuration environment.ts:
    findstr "apiUrl" ..\frontend\src\environments\environment.ts
) else (
    echo ❌ Fichier environment.ts non trouvé
)

echo.
if exist "..\frontend\proxy.conf.js" (
    echo Configuration proxy.conf.js:
    type ..\frontend\proxy.conf.js
) else (
    echo ❌ Fichier proxy.conf.js non trouvé
)

echo.
echo [5] Vérification du processus Angular...
netstat -ano | findstr :4200
if %errorlevel% equ 0 (
    echo ✅ Serveur Angular détecté sur port 4200
) else (
    echo ❌ Aucun serveur Angular détecté sur port 4200
)

echo.
echo [6] Test avec proxy Angular (si serveur actif)...
if exist "..\frontend\proxy.conf.js" (
    echo Testing via proxy: %FRONTEND_URL%/api/lta/stats
    curl -s -w "Status: %%{http_code}\n" %FRONTEND_URL%/api/lta/stats
    echo.
    
    echo Testing via proxy: %FRONTEND_URL%/api/lta/recent?limit=5
    curl -s -w "Status: %%{http_code}\n" %FRONTEND_URL%/api/lta/recent?limit=5
    echo.
)

echo [7] Analyse des logs réseau (instructions)...
echo.
echo Pour diagnostiquer plus précisément:
echo 1. Ouvrez les DevTools (F12) dans votre navigateur
echo 2. Allez dans l'onglet Network
echo 3. Rechargez la page du dashboard LTA
echo 4. Observez les appels HTTP pour voir l'URL exacte utilisée
echo.
echo Si vous voyez des appels vers /lta/* au lieu de /api/lta/*,
echo le problème vient de la configuration du proxy ou de l'environnement.

:end
echo.
echo ========================================
echo    DIAGNOSTIC TERMINÉ
echo ========================================
pause
