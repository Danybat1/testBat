@echo off
echo Quick JasperReports Test
echo =======================
echo.

cd /d "%~dp0..\backend"

echo [1/2] Compiling backend...
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed!
    echo Running with verbose output...
    call mvnw.cmd clean compile
    goto end
)
echo ✓ Compilation successful

echo.
echo [2/2] Running simple JasperReports test...
call mvnw.cmd test -Dtest=SimpleJasperTest
if %errorlevel% neq 0 (
    echo ERROR: Simple test failed!
    goto end
)

echo.
echo ✓ Simple JasperReports test passed!
echo Ready to run full test suite.

:end
pause
