@echo off
echo Building and running FreightOps Backend as JAR...

cd /d "%~dp0\..\backend"

echo.
echo [1/2] Building JAR file...
mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Running JAR with minimal memory...
java -Xms128m -Xmx256m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -jar target\freightops-backend-*.jar

echo.
echo Backend stopped.
pause
