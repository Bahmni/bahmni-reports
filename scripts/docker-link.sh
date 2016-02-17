#!/bin/bash

USER=bahmni

rm -rf /opt/bahmni-reports/bahmni-reports
ln -s /bahmni-code/bahmni-reports/target/bahmnireports /opt/bahmni-reports/bahmni-reports
chown -h ${USER}:${USER} /opt/bahmni-reports/bahmni-reports
