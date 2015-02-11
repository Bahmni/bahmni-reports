@echo off 

REM !!!!IMPORTANT!!!!
REM Before using this script to deploy do the following
REM Add putty to your path
REM Use puttygen to generate win_insecure_private_key.ppk from your %USERPROFILE%\.vagrant.d\insecure_private_key that comes along with vagrant.
REM !!!End of IMPORTANT!!!

REM All config is here

set MACHINE_IP=192.168.33.10
set TEMP_DHIS_WEBAPP_WAR=/tmp/deploy_dhis_webapp
set SCRIPTS_DIR=scripts
set KEY_FILE=%USERPROFILE%\.vagrant.d\win_insecure_private_key.ppk
set DHIS_WEBAPP_WAR=.\target\dhis-webapp.war

if exist %KEY_FILE% (
    REM setup
    putty -ssh vagrant@%MACHINE_IP% -i %KEY_FILE% -m %SCRIPTS_DIR%/setup_environment.sh
    REM Copy war to Vagrant tmp
    pscp  -i %KEY_FILE% %DHIS_WEBAPP_WAR% vagrant@%MACHINE_IP%:%TEMP_DHIS_WEBAPP_WAR%
    REM Copy DHIS Webapp war to Tomcat from tmp
    putty -ssh vagrant@%MACHINE_IP% -i %KEY_FILE% -m %SCRIPTS_DIR%/deploy_dhis_webapp_war.sh
) else (
    echo Use puttygen to generate win_insecure_private_key.ppk from your %USERPROFILE%\.vagrant.d\insecure_private_key that comes along with vagrant.
)

