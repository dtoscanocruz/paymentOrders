#!/bin/sh
# Wait until the app actuator/health reports UP using the docker healthcheck service
cd "$(dirname "$0")/.." || exit 1
printf "Running docker compose healthcheck (this will block until app is UP)...\n"
docker compose run --rm healthcheck
rc=$?
if [ $rc -eq 0 ]; then
  printf "App reported UP\n"
else
  printf "Healthcheck failed with exit code $rc\n"
fi
exit $rc
