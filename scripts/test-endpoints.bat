@echo off
echo Testing JasperReports Endpoints
echo ===============================
echo.

set BACKEND_URL=http://localhost:8080
set OUTPUT_DIR=%~dp0..\test-output

echo Creating output directory...
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo.
echo [1/4] Testing backend health...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" %BACKEND_URL%/actuator/health
if %errorlevel% neq 0 (
    echo ERROR: Backend not responding. Please start the backend first.
    echo Run: scripts\start-backend-test.bat
    goto end
)

echo.
echo [2/4] Creating test manifest...
curl -X POST %BACKEND_URL%/api/fret/manifests ^
  -H "Content-Type: application/json" ^
  -d "{\"manifestNumber\":\"TEST-2024-001\",\"proformaNumber\":\"PRO-TEST-001\",\"transportMode\":\"ROAD\",\"vehicleReference\":\"TRK-TEST\",\"driverName\":\"Jean Test\",\"shipperName\":\"Expéditeur Test\",\"shipperAddress\":\"123 Rue Test\",\"shipperCity\":\"Kinshasa\",\"consigneeName\":\"Destinataire Test\",\"consigneeAddress\":\"456 Ave Test\",\"consigneeCity\":\"Lubumbashi\",\"totalPackages\":2,\"totalWeight\":50.5,\"totalVolume\":1.2,\"totalValue\":1000.0,\"status\":\"DRAFT\",\"items\":[{\"lineNumber\":1,\"trackingNumber\":\"TRK-001\",\"description\":\"Test Item 1\",\"packagingType\":\"Carton\",\"packageCount\":1,\"grossWeight\":25.0,\"volume\":0.6,\"declaredValue\":500.0},{\"lineNumber\":2,\"trackingNumber\":\"TRK-002\",\"description\":\"Test Item 2\",\"packagingType\":\"Caisse\",\"packageCount\":1,\"grossWeight\":25.5,\"volume\":0.6,\"declaredValue\":500.0}]}" ^
  -o "%OUTPUT_DIR%\manifest-response.json"

if %errorlevel% neq 0 (
    echo ERROR: Failed to create test manifest
    goto end
)

echo ✓ Test manifest created

echo.
echo [3/4] Testing PDF generation...
curl -X GET %BACKEND_URL%/api/fret/manifests/1/pdf ^
  -H "Accept: application/pdf" ^
  -o "%OUTPUT_DIR%\test-manifest.pdf"

if %errorlevel% neq 0 (
    echo ERROR: PDF generation failed
    goto end
)

echo ✓ PDF generated: %OUTPUT_DIR%\test-manifest.pdf

echo.
echo [4/4] Testing Word generation...
curl -X GET %BACKEND_URL%/api/fret/manifests/1/word ^
  -H "Accept: application/rtf" ^
  -o "%OUTPUT_DIR%\test-manifest.rtf"

if %errorlevel% neq 0 (
    echo ERROR: Word generation failed
    goto end
)

echo ✓ Word document generated: %OUTPUT_DIR%\test-manifest.rtf

echo.
echo ========================================
echo ✓ All endpoint tests passed!
echo ========================================
echo.
echo Generated files:
echo - PDF: %OUTPUT_DIR%\test-manifest.pdf
echo - Word: %OUTPUT_DIR%\test-manifest.rtf
echo - Response: %OUTPUT_DIR%\manifest-response.json
echo.
echo Open the files to verify the JasperReports output.

:end
pause
