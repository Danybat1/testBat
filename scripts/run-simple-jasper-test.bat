@echo off
echo Executing Simple JasperReports Test
echo ==================================
echo.

cd /d "%~dp0..\backend"

echo [1/2] Compiling project...
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed!
    goto end
)
echo ✓ Compilation successful

echo.
echo [2/2] Running simple JasperReports test...
call mvnw.cmd test -Dtest=SimpleJasperTest -q
if %errorlevel% neq 0 (
    echo ERROR: Simple JasperReports test failed!
    echo.
    echo Running with verbose output for debugging...
    call mvnw.cmd test -Dtest=SimpleJasperTest
    goto end
)

echo ✓ Simple JasperReports test passed!
echo.
echo JasperReports is working correctly.
echo Ready to run full test suite.

:end
pause
