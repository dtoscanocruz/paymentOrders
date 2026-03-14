@echo off
REM Wait until the app actuator/health reports UP using the docker healthcheck service
cd /d %~dp0\..
echo Running docker compose healthcheck (this will block until app is UP)...
docker compose run --rm healthcheck
if %ERRORLEVEL% equ 0 (
  echo App reported UP
) else (
  echo Healthcheck failed with exit code %ERRORLEVEL%
)
exit /b %ERRORLEVEL%
