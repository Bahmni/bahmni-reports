#!/bin/sh

HOST=localhost
TARGET_DB=reports_integration_tests
DUMP_FILE=src/test/resources/openmrs_schema.sql
USER_NAME=openmrs-user
PASSWORD=password


echo "Dropping database for integration tests ..."
mysql -u$USER_NAME -p$PASSWORD -h$HOST -e "DROP DATABASE $TARGET_DB"

echo "Creating database for integration tests ..."
mysql -u$USER_NAME -p$PASSWORD -h$HOST -e "CREATE DATABASE $TARGET_DB"

echo "Applying database dump ..."
mysql -u$USER_NAME -p$PASSWORD -h$HOST $TARGET_DB < $DUMP_FILE
