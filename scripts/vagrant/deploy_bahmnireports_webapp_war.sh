#!/bin/sh -x -e

BUILD_DIR=/packages/build
WAR_LOCATION=/home/jss/apache-tomcat-8.0.12/webapps

sudo rm -rf $WAR_LOCATION/bahmnireports
sudo su - jss -c "cp -f $BUILD_DIR/bahmnireports.war $WAR_LOCATION"
sudo su - jss -c "unzip -o -q $BUILD_DIR/bahmnireports.war -d $WAR_LOCATION/bahmnireports"
