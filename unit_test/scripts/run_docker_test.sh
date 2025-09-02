#!/bin/bash

export MYSQL_ROOT_PASSWORD=rootroot

mvn -ntp package -P enterprise -DskipTests=true
docker compose -f unit_test/scripts/docker-compose.yml up --build --exit-code-from maven 