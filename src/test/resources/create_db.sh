#!/bin/sh

echo "Creating database for integration tests ..."
mysql -uroot -ppassword -hlocalhost reports_integration_tests < src/test/resources/openmrs_schema.sql
