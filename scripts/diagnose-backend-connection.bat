@echo off
echo ========================================
echo   DIAGNOSTIC CONNEXION BACKEND LTA
echo ========================================
echo.

echo [1/5] Vérification du port 8080...
netstat -an | findstr ":8080" > nul
if %errorlevel% equ 0 (
    echo ✅ Port 8080 en écoute
    netstat -an | findstr ":8080"
) else (
    echo ❌ Port 8080 non accessible
    echo ⚠️  Le backend Spring Boot n'est pas démarré
    goto :backend_not_running
)

echo.
echo [2/5] Test de connectivité HTTP...
curl -s -o nul -w "%%{http_code}" http://localhost:8080 > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt
del temp_status.txt

if "%HTTP_STATUS%"=="200" (
    echo ✅ Backend accessible - Status: %HTTP_STATUS%
) else if "%HTTP_STATUS%"=="404" (
    echo ⚠️  Backend répond mais endpoint racine non trouvé - Status: %HTTP_STATUS%
    echo ℹ️  C'est normal, continuons les tests...
) else if "%HTTP_STATUS%"=="000" (
    echo ❌ Backend non accessible - Pas de réponse
    goto :backend_not_running
) else (
    echo ⚠️  Backend répond avec status: %HTTP_STATUS%
)

echo.
echo [3/5] Test des endpoints LTA spécifiques...
echo Test de /api/lta/stats...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/lta/stats > temp_status.txt
set /p STATS_STATUS=<temp_status.txt
del temp_status.txt

if "%STATS_STATUS%"=="200" (
    echo ✅ Endpoint /api/lta/stats accessible
) else (
    echo ❌ Endpoint /api/lta/stats inaccessible - Status: %STATS_STATUS%
)

echo Test de /api/lta/recent...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/lta/recent > temp_status.txt
set /p RECENT_STATUS=<temp_status.txt
del temp_status.txt

if "%RECENT_STATUS%"=="200" (
    echo ✅ Endpoint /api/lta/recent accessible
) else (
    echo ❌ Endpoint /api/lta/recent inaccessible - Status: %RECENT_STATUS%
)

echo.
echo [4/5] Vérification des processus Java...
tasklist | findstr "java.exe" > nul
if %errorlevel% equ 0 (
    echo ✅ Processus Java détectés:
    tasklist | findstr "java.exe"
) else (
    echo ❌ Aucun processus Java en cours
    goto :backend_not_running
)

echo.
echo [5/5] Test complet avec réponse...
echo Appel GET /api/lta/stats avec réponse:
curl -s http://localhost:8080/api/lta/stats
echo.
echo.

echo ========================================
echo   ✅ DIAGNOSTIC TERMINÉ
echo ========================================
echo.
if "%STATS_STATUS%"=="200" if "%RECENT_STATUS%"=="200" (
    echo ✅ Backend LTA fonctionnel - Endpoints accessibles
    echo.
    echo Le problème peut venir de:
    echo 1. Cache navigateur - Videz le cache ^(Ctrl+Shift+R^)
    echo 2. Proxy/CORS - Vérifiez la configuration proxy.conf.js
    echo 3. Frontend sur mauvais port - Vérifiez que Angular utilise le bon port
) else (
    echo ⚠️  Backend partiellement fonctionnel
    echo Certains endpoints LTA ne répondent pas correctement
)
echo.
pause
goto :end

:backend_not_running
echo.
echo ========================================
echo   ❌ BACKEND NON DÉMARRÉ
echo ========================================
echo.
echo Le backend Spring Boot n'est pas en cours d'exécution.
echo.
echo Solutions:
echo 1. Démarrer le backend:
echo    cd backend
echo    mvn spring-boot:run
echo.
echo 2. Ou utiliser le script de démarrage:
echo    scripts\dev-start.bat
echo.
echo 3. Vérifier les logs du backend pour les erreurs
echo.
pause

:end
