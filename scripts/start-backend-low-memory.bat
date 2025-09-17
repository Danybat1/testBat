@echo off
echo Starting FreightOps Backend with optimized memory settings...

cd /d "%~dp0\..\backend"

echo.
echo Current directory: %CD%
echo Java version:
java -version

echo.
echo Starting backend with reduced memory allocation...
echo JVM Options: -Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:G1HeapRegionSize=16m"

echo.
echo Backend startup completed.
pause
