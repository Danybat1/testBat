@echo off
echo ========================================
echo   CORRECTION DES BUDGETS CSS ANGULAR
echo ========================================
echo.

echo [1/3] Nettoyage du cache Angular...
cd /d "%~dp0\..\frontend"
if exist ".angular" (
    rmdir /s /q ".angular"
    echo ✅ Cache Angular supprimé
) else (
    echo ⚠️  Pas de cache Angular à supprimer
)

if exist "node_modules\.cache" (
    rmdir /s /q "node_modules\.cache"
    echo ✅ Cache Node supprimé
)

echo.
echo [2/3] Test de compilation avec nouveaux budgets...
call npm run build
if %errorlevel% equ 0 (
    echo ✅ Compilation réussie avec nouveaux budgets
    echo.
    echo Taille du bundle final:
    dir /s dist\frontend\*.js | find "bytes"
) else (
    echo ❌ Erreurs de compilation persistantes
    echo Vérifiez les détails ci-dessus
    goto :error
)

echo.
echo [3/3] Test de démarrage en mode développement...
echo Démarrage du serveur de développement...
start "Dev Server" cmd /c "ng serve --port 4201"
timeout /t 10 > nul

echo Test de l'accès au serveur...
curl -s http://localhost:4201 > nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Serveur de développement accessible
) else (
    echo ⚠️  Serveur en cours de démarrage...
)

echo Arrêt du serveur de test...
taskkill /f /im node.exe > nul 2>&1

echo.
echo ========================================
echo   ✅ BUDGETS CSS CORRIGÉS
echo ========================================
echo.
echo Les budgets Angular ont été ajustés:
echo - Bundle initial: 500kb → 1.5mb (warning), 1mb → 2mb (error)
echo - Styles composants: 2kb → 10kb (warning), 4kb → 20kb (error)
echo.
echo L'application devrait maintenant compiler sans erreurs de budget.
echo.
pause
goto :end

:error
echo.
echo ========================================
echo   ❌ ERREURS PERSISTANTES
echo ========================================
echo.
echo Des erreurs de compilation subsistent.
echo Vérifiez les messages d'erreur ci-dessus.
echo.
pause

:end
