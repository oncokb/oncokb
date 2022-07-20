FROM openjdk:8-jre
COPY target/dependency/webapp-runner.jar /webapp-runner.jar
COPY target/*.war /app.war
# specify default command
ENTRYPOINT java ${JAVA_OPTS} -jar /target/dependency/webapp-runner.jar ${WEBAPPRUNNER_OPTS} /oncokb.war


# ENTRYPOINT java "-Djdbc.driverClassName=com.mysql.jdbc.Driver -Djdbc.url=jdbc:mysql://db:3306/oncokb?useUnicode=yes&characterEncoding=UTF-8&useSSL=false -Djdbc.username=root -Djdbc.password=rootdocker" -jar ./dependency/webapp-runner.jar -cp ./oncokb.war
