@echo off
echo ========================================
echo   FREIGHTOPS - CORRECTION ERREURS 404
echo ========================================
echo.

echo [1/4] Verification du backend...
curl -s -o nul -w "Backend Status: %%{http_code}\n" http://localhost:8080/actuator/health

echo.
echo [2/4] Test des endpoints critiques...
echo.

echo Testing LTA endpoints:
curl -s -o nul -w "GET /api/lta/stats: %%{http_code}\n" http://localhost:8080/api/lta/stats
curl -s -o nul -w "GET /api/lta/recent: %%{http_code}\n" http://localhost:8080/api/lta/recent?limit=5

echo.
echo Testing Manifest endpoints:
curl -s -o nul -w "GET /api/fret/manifests: %%{http_code}\n" http://localhost:8080/api/fret/manifests?page=0^&size=10

echo.
echo Testing Treasury endpoints:
curl -s -o nul -w "GET /api/cash-boxes/active: %%{http_code}\n" http://localhost:8080/api/cash-boxes/active
curl -s -o nul -w "GET /api/bank-accounts/active: %%{http_code}\n" http://localhost:8080/api/bank-accounts/active
curl -s -o nul -w "GET /api/treasury-transactions: %%{http_code}\n" http://localhost:8080/api/treasury-transactions

echo.
echo [3/4] Verification des URLs frontend...
echo Checking frontend service URLs...
findstr /R "apiUrl.*=" frontend\src\app\**\*.service.ts > temp_urls.txt
echo URLs found in services:
type temp_urls.txt
del temp_urls.txt

echo.
echo [4/4] Recommandations:
echo - Verifiez que le backend est demarre sur le port 8080
echo - Tous les endpoints doivent commencer par /api
echo - Les services Angular doivent utiliser environment.apiUrl + '/api/...'
echo.

echo ========================================
echo   DIAGNOSTIC TERMINE
echo ========================================
pause
