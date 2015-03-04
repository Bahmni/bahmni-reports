#!/bin/sh -x
PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $PATH_OF_CURRENT_SCRIPT/vagrant/vagrant_functions.sh

set -e
$PATH_OF_CURRENT_SCRIPT/vagrant-deploy.sh

#invoke migration of openmrs core
run_in_vagrant -c "sudo su - bahmni -c 'cd /bahmni_temp/ && ./run-bahmni-reports-liquibase.sh'"