@echo off
echo Starting Backend for JasperReports Testing
echo ==========================================
echo.

cd /d "%~dp0..\backend"

echo [1/3] Compiling backend...
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed!
    goto end
)
echo ✓ Backend compiled successfully

echo.
echo [2/3] Starting Spring Boot application...
echo Backend will be available at: http://localhost:8080
echo.
echo Available endpoints for testing:
echo - POST /api/fret/manifests (create manifest)
echo - GET /api/fret/manifests/{id}/pdf (generate PDF)
echo - GET /api/fret/manifests/{id}/word (generate Word)
echo.
echo Press Ctrl+C to stop the backend when testing is complete.
echo.

start "FreightOps Backend" cmd /k "mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev"

echo.
echo [3/3] Backend starting in separate window...
echo Wait 30 seconds for full startup, then test endpoints.
echo.

timeout /t 5 /nobreak > nul

echo Ready for testing!
echo.
echo Test commands (use in another terminal):
echo curl -X GET http://localhost:8080/api/fret/manifests/1/pdf -o manifest.pdf
echo curl -X GET http://localhost:8080/api/fret/manifests/1/word -o manifest.rtf
echo.

:end
pause
