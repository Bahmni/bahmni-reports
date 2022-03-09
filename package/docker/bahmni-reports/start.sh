#!/bin/sh
set -e

sh /etc/wait-for ${REPORTS_DB_SERVER}:3306
sh /etc/wait-for ${OPENMRS_DB_HOST}:3306

echo "[INFO] Substituting Environment Variables"
envsubst < /etc/bahmni-reports/bahmni-reports.properties.template > ${HOME}/.bahmni-reports/bahmni-reports.properties
echo "[INFO] Running Liquibase migrations"
sh /etc/bahmni-reports/run-liquibase.sh liquibase.xml $OPENMRS_DB_HOST $OPENMRS_DB_NAME $OPENMRS_DB_USERNAME $OPENMRS_DB_PASSWORD
sh /etc/bahmni-reports/run-liquibase.sh liquibase_bahmni_reports.xml $REPORTS_DB_SERVER $REPORTS_DB_NAME $REPORTS_DB_USERNAME $REPORTS_DB_PASSWORD
echo "[INFO] Starting Application"
java -jar $SERVER_OPTS $DEBUG_OPTS /opt/bahmni-reports/lib/bahmni-embedded-tomcat.jar