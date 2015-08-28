#!/bin/bash -x -e

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

WEB_CONTAINER=$1
SHELL_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_BASE=$SHELL_DIR/../..
REPORTS_WEBAPP_WAR=$PROJECT_BASE/target/bahmnireports.war
TOMCAT_LOCATION=/var/lib/tomcat7/webapps

if [[ ! -e $REPORTS_WEBAPP_WAR ]]; then
    echo "----------------------------------------"
    echo "REPORTS_WEBAPP_WAR does not exist!"
    echo "----------------------------------------"
    exit -1
fi

docker exec -t $WEB_CONTAINER rm -rf $TOMCAT_LOCATION/bahmnireports
docker cp $REPORTS_WEBAPP_WAR $WEB_CONTAINER:$TOMCAT_LOCATION/bahmnireports.war
