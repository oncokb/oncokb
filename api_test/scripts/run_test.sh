RESOURCES_DIR=$PWD/core/src/main/resources/properties
if [ -d $RESOURCES_DIR ]; then
    echo "\033[1;33mWarning: $RESOURCES_DIR exsists and will override JAVA_OPTS.\033[0m"
fi

mvn -ntp package -P enterprise -DskipTests=true
docker compose -f $PWD/test_api/scripts/docker-compose.yaml up