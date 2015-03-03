#!/bin/sh -x -e

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $PATH_OF_CURRENT_SCRIPT/vagrant_functions.sh

#All config is here
BUILD_DIR=/packages/build

SCRIPTS_DIR=scripts/vagrant
SHELL_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_BASE=$SHELL_DIR/../..
REPORTS_WEBAPP_WAR=$PROJECT_BASE/webapp/target/bahmnireports.war

if [[ ! -e $REPORTS_WEBAPP_WAR ]]; then
    echo "----------------------------------------"
    echo "REPORTS_WEBAPP_WAR does not exist!"
    echo "----------------------------------------"
    exit -1
fi

# Setup environment
#run_in_vagrant -f "$SCRIPTS_DIR/setup_environment.sh"

# Copy Bahmni Reports WebApp War file to Vagrant tmp
scp_to_vagrant $REPORTS_WEBAPP_WAR $BUILD_DIR

#Deploy them from Vagrant /tmp to appropriate location
run_in_vagrant -f "$SCRIPTS_DIR/deploy_bahmnireports_webapp_war.sh"

