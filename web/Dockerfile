FROM openjdk:8-jre
COPY target/dependency/webapp-runner.jar /webapp-runner.jar
COPY target/*.war /app.war
# specify default command
ENTRYPOINT java ${JAVA_OPTS} -jar /webapp-runner.jar ${WEBAPPRUNNER_OPTS} /app.war
