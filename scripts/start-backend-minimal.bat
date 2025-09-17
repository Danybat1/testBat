@echo off
echo Starting FreightOps Backend with MINIMAL memory settings...

cd /d "%~dp0\..\backend"

echo.
echo Current directory: %CD%
echo.
echo ATTENTION: Utilisation de parametres JVM ultra-minimaux
echo pour contourner le probleme de memoire Windows
echo.

echo Starting backend with minimal memory allocation...
echo JVM Options: -Xms128m -Xmx256m -XX:MaxMetaspaceSize=64m

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -XX:CompressedClassSpaceSize=32m -XX:ReservedCodeCacheSize=32m"

echo.
echo Backend startup completed.
pause
