# Bahmni Reports

Hosts the reports web application for the [Bahmni project](http://www.bahmni.org/)

[![Build Status](https://travis-ci.org/Bahmni/bahmni-reports.svg?branch=master)](https://travis-ci.org/Bahmni/bahmni-reports)

# Installing the application

1. Clone or download this repository.

2. Run `mvn clean install -DskipTests` to build it

3. Deploy the WAR file in `target/bahmnireports.war`

# For developers : How to run integration tests

1. Run `scripts/create_configuration.sh` to create the properties file required to run integration tests. The file created at `$HOME/.bahmni-reports/bahmni-reports-test.properties` comes with default values.

3. Install MySQL client and server in your machine. If you already have a MySQL server available make sure that the user has the privileges to dump the database.

4. Change `openmrs.url` in the properties file to set the host and port of the MySQL server.

5. Change `openmrs.username` to set the database user.

6. Change `openmrs.password` to set the database user password

7. Run `mvn clean install -DskipDump=false` to build your changes and run integration tests. If you are using an IDE you can directly run a specific integration test.
