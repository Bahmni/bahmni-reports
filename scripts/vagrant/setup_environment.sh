#!/bin/sh -x

TEMP_LOCATION=/tmp/deploy_bahmnireports_webapp

if [[ ! -d $TEMP_LOCATION ]]; then
   mkdir $TEMP_LOCATION
fi

rm -rf $TEMP_LOCATION/*
