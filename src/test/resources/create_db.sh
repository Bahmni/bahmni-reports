#!/bin/sh

HOST=$(grep openmrs.url $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d '/' -f 3 | cut -d ':' -f 1)
PORT=$(grep openmrs.url $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d ':' -f 4 | cut -d '/' -f 1)
TARGET_DB=$(grep openmrs.url $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d '/' -f 4 | cut -d '?' -f 1)
TARGET_DB2=bahmni_reports_it
DUMP_FILE=src/test/resources/sql/openmrs_schema.sql
DUMP_FILE2=src/main/resources/sql/quartz/tables_mysql_innodb.sql
DUMP_FILE3=src/test/resources/sql/bahmniReports.sql
DUMP_FILE4=src/test/resources/sql/function_obsParentConcept.sql
USER_NAME=$(grep openmrs.username $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d '=' -f 2)
PASSWORD=$(grep openmrs.password $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d '=' -f 2)


echo "Dropping databases for integration tests ..."
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT -e "DROP DATABASE $TARGET_DB"
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT -e "DROP DATABASE $TARGET_DB2"

echo "Creating databases for integration tests ..."
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT -e "CREATE DATABASE $TARGET_DB"
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT -e "CREATE DATABASE $TARGET_DB2"

echo "Applying database dumps ..."
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT $TARGET_DB < $DUMP_FILE
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT $TARGET_DB2 < $DUMP_FILE2
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT $TARGET_DB2 < $DUMP_FILE3
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT $TARGET_DB < $DUMP_FILE4
