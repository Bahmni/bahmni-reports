#!/bin/sh -x -e

BUILD_DIR=/packages/build
WAR_LOCATION=/home/bahmni/apache-tomcat-8.0.12/webapps

sudo rm -rf $WAR_LOCATION/bahmnireports
sudo su - bahmni -c "cp -f $BUILD_DIR/bahmnireports.war $WAR_LOCATION"
sudo su - bahmni -c "unzip -o -q $BUILD_DIR/bahmnireports.war -d $WAR_LOCATION/bahmnireports"
