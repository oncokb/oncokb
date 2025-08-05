#!/bin/sh

# Due to DataSource.xml having <property name="systemPropertiesModeName" value="SYSTEM_PROPERTIES_MODE_OVERRIDE"/>
# The system properties passed in here will override database.properties values

mvn -ntp test \
  -Djdbc.driverClassName=com.mysql.jdbc.Driver \
  -Djdbc.url="jdbc:mysql://mysql:3306/oncokb_core_test?useUnicode=yes&characterEncoding=UTF-8" \
  -Djdbc.username=root \
  -Djdbc.password=${MYSQL_ROOT_PASSWORD}