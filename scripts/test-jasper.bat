@echo off
echo Testing JasperReports Implementation
echo ===================================
echo.

cd /d "%~dp0..\backend"

echo [1/4] Compiling backend project...
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Backend compilation failed!
    goto end
)
echo ✓ Backend compiled successfully

echo.
echo [2/4] Running simple JasperReports validation...
call mvnw.cmd test -Dtest=SimpleJasperTest -q
if %errorlevel% neq 0 (
    echo ERROR: Simple JasperReports test failed!
    echo Running with verbose output for debugging...
    call mvnw.cmd test -Dtest=SimpleJasperTest
    goto end
)
echo ✓ Simple JasperReports validation passed

echo.
echo [3/4] Running JasperReports template tests...
call mvnw.cmd test -Dtest=ManifestTemplateTest -q
if %errorlevel% neq 0 (
    echo ERROR: Template tests failed!
    goto end
)
echo ✓ Template tests passed

echo.
echo [4/4] Running ManifestPdfService tests...
call mvnw.cmd test -Dtest=ManifestPdfServiceTest -q
if %errorlevel% neq 0 (
    echo ERROR: PDF service tests failed!
    goto end
)
echo ✓ PDF service tests passed

echo.
echo ========================================
echo ✓ All JasperReports tests passed!
echo ========================================
echo.
echo Next steps:
echo - Test REST endpoints /api/fret/manifests/{id}/pdf
echo - Test REST endpoints /api/fret/manifests/{id}/word
echo - Integration testing with real data
echo.

:end
pause
