#!/usr/bin/env bash

TARGET_DIR=$HOME/.bahmni-reports

mkdir -p $TARGET_DIR/reports/

echo """openelis.url=jdbc:postgresql://localhost:5432/clinlims
openelis.username=clinlims
openelis.password=clinlims
openerp.url=jdbc:postgresql://localhost:5432/openerp
openerp.username=openerp
openerp.password=
openmrs.url=jdbc:mysql://localhost:3306/openmrs?allowMultiQueries=true
openmrs.username=openmrs-user
openmrs.password=password
config.file.path=/var/www/bahmni_config/openmrs/apps/reports/reports.json
openmrs.service.rootUrl=http://localhost:8050/openmrs/ws/rest/v1
openmrs.service.user=admin
openmrs.service.password=test
openmrs.connectionTimeoutInMilliseconds=30000
openmrs.replyTimeoutInMilliseconds=120000
macrotemplates.temp.directory=/tmp/
bahmnireports.db.url=jdbc:mysql://localhost:3306/bahmni_reports?allowMultiQueries=true
reports.save.directory=$TARGET_DIR/reports""" > $TARGET_DIR/bahmni-reports.properties

echo """openelis.url=jdbc:postgresql://localhost:5432/clinlims
openelis.username=clinlims
openelis.password=clinlims
openerp.url=jdbc:postgresql://localhost:5432/openerp
openerp.username=openerp
openerp.password=
openmrs.url=jdbc:mysql://localhost:3306/reports_integration_tests?allowMultiQueries=true
openmrs.username=root
openmrs.password=password
config.file.path=/var/www/bahmni_config/openmrs/apps/reports/reports.json
openmrs.service.rootUrl=http://localhost:8050/openmrs/ws/rest/v1
openmrs.service.user=admin
openmrs.service.password=test
openmrs.connectionTimeoutInMilliseconds=30000
openmrs.replyTimeoutInMilliseconds=120000
macrotemplates.temp.directory=/tmp/
bahmnireports.db.url=jdbc:mysql://localhost:3306/bahmni_reports_it?allowMultiQueries=true
reports.save.directory=$TARGET_DIR/reports""" > $TARGET_DIR/bahmni-reports-test.properties

echo "Copied configruation files to $TARGET_DIR"
