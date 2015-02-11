#!/bin/sh -x -e

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $PATH_OF_CURRENT_SCRIPT/vagrant_functions.sh

#All config is here
TEMP_DHIS_WEBAPP_WAR=/tmp/deploy_dhis_webapp
SCRIPTS_DIR=scripts/vagrant
SHELL_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_BASE=$SHELL_DIR/../..
DHIS_WEBAPP_WAR=$PROJECT_BASE/webapp/target/dhis-webapp.war

if [[ ! -e $DHIS_WEBAPP_WAR ]]; then
    echo "----------------------------------------"
    echo "DHIS_WEBAPP_WAR does not exist!"
    echo "----------------------------------------"
    exit -1
fi

# Setup environment
run_in_vagrant -f "$SCRIPTS_DIR/setup_environment.sh"

# Copy DHIS WebApp War file to Vagrant tmp
scp_to_vagrant $DHIS_WEBAPP_WAR $TEMP_DHIS_WEBAPP_WAR


#Deploy them from Vagrant /tmp to appropriate location
run_in_vagrant -f "$SCRIPTS_DIR/deploy_dhis_webapp_war.sh"

