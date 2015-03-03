@echo off 

REM !!!!IMPORTANT!!!!
REM Before using this script to deploy do the following
REM Add putty to your path
REM Use puttygen to generate win_insecure_private_key.ppk from your %USERPROFILE%\.vagrant.d\insecure_private_key that comes along with vagrant.
REM !!!End of IMPORTANT!!!

REM All config is here

set MACHINE_IP=192.168.33.10
set TEMP_REPORTS_WEBAPP_WAR=/tmp/deploy_bahmnireports_webapp
set SCRIPTS_DIR=scripts
set KEY_FILE=%USERPROFILE%\.vagrant.d\win_insecure_private_key.ppk
set REPORTS_WEBAPP_WAR=.\target\bahmnireports.war

if exist %KEY_FILE% (
    REM setup
    REM putty -ssh vagrant@%MACHINE_IP% -i %KEY_FILE% -m %SCRIPTS_DIR%/setup_environment.sh
    REM Copy war to Vagrant tmp
    pscp  -i %KEY_FILE% %REPORTS_WEBAPP_WAR% vagrant@%MACHINE_IP%:%TEMP_REPORTS_WEBAPP_WAR%
    REM Copy Bahmni Reports Webapp war to Tomcat from tmp
    putty -ssh vagrant@%MACHINE_IP% -i %KEY_FILE% -m %SCRIPTS_DIR%/deploy_bahmnireports_webapp_war.sh
) else (
    echo Use puttygen to generate win_insecure_private_key.ppk from your %USERPROFILE%\.vagrant.d\insecure_private_key that comes along with vagrant.
)

