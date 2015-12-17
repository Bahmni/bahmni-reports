#!/bin/sh -x -e

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $PATH_OF_CURRENT_SCRIPT/vagrant/vagrant_functions.sh
USER=bahmni

run_in_vagrant -c "sudo rm -rf /opt/bahmni-reports/bahmni-reports"
run_in_vagrant -c "sudo ln -s /bahmni/bahmni-reports/target/bahmnireports /opt/bahmni-reports/bahmni-reports"
run_in_vagrant -c "sudo chown -h ${USER}:${USER} /opt/bahmni-reports/bahmni-reports"