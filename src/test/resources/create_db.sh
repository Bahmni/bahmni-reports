#!/bin/sh

HOST=$(grep openmrs.url $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d '/' -f 3 | cut -d ':' -f 1)
PORT=$(grep openmrs.url $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d ':' -f 4 | cut -d '/' -f 1)
TARGET_DB=$(grep openmrs.url $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d '/' -f 4 | cut -d '?' -f 1)
DUMP_FILE=src/test/resources/sql/openmrs_schema.sql
USER_NAME=$(grep openmrs.username $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d '=' -f 2)
PASSWORD=$(grep openmrs.password $HOME/.bahmni-reports/bahmni-reports-test.properties | cut -d '=' -f 2)


echo "Dropping database for integration tests ..."
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT -e "DROP DATABASE $TARGET_DB"

echo "Creating database for integration tests ..."
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT -e "CREATE DATABASE $TARGET_DB"

echo "Applying database dump ..."
mysql --protocol tcp -u$USER_NAME -p$PASSWORD -h$HOST -P$PORT $TARGET_DB < $DUMP_FILE
