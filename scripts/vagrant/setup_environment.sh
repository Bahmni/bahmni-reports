#!/bin/sh -x

TEMP_LOCATION=/tmp/deploy_dhis_webapp

if [[ ! -d $TEMP_LOCATION ]]; then
   mkdir $TEMP_LOCATION
fi

rm -rf $TEMP_LOCATION/*
