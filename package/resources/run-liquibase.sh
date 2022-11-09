#!/bin/sh
set -e -x



CHANGE_LOG_TABLE="-Dliquibase.databaseChangeLogTableName=liquibasechangelog -Dliquibase.databaseChangeLogLockTableName=liquibasechangeloglock -DschemaName=$3"
LIQUIBASE_JAR="${WAR_DIRECTORY}/WEB-INF/lib/liquibase-core-4.8.0.jar"
DRIVER="com.mysql.cj.jdbc.Driver"
CLASSPATH="${WAR_DIRECTORY}/WEB-INF/lib/mysql-connector-java-8.0.26.jar"
CHANGE_LOG_FILE="$1"

(cd ${WAR_DIRECTORY}/WEB-INF/classes/ && java $CHANGE_LOG_TABLE  -jar $LIQUIBASE_JAR --driver=$DRIVER --classpath=$CLASSPATH --changeLogFile=$CHANGE_LOG_FILE --url=jdbc:mysql://$2:3306/$3 --username=$4 --password=$5 update)
