#!/bin/sh -x -e

TEMP_LOCATION=/tmp/deploy_dhis_webapp
WAR_LOCATION=/home/jss/apache-tomcat-8.0.12/webapps

sudo rm -rf $WAR_LOCATION/dhis-webapp
sudo su - jss -c "cp -f $TEMP_LOCATION/* $WAR_LOCATION"
