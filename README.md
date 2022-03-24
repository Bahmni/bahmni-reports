# Bahmni Reports

Hosts the reports web application for the [Bahmni project](http://www.bahmni.org/)

[![Build Status](https://travis-ci.org/Bahmni/bahmni-reports.svg?branch=master)](https://travis-ci.org/Bahmni/bahmni-reports)

# Installing the application

1. Clone or download this repository.

2. Run `./mvnw clean install -DskipTests` to build it

3. Deploy the WAR file in `target/bahmnireports.war`

# For developers : How to run integration tests

There are two ways to run integration tests:

**Option 1: (The recommended one)**

The configuration should be created as part of the test-compile phase and this will also test their local mysql connect.

    Run: ./mvnw -DskipDump=false -DskipConfig=false clean install 

**Option 2: (The properties can be forcefully created by running the shell)**

   1. Run `scripts/create_configuration.sh` to create the properties file required to run integration tests. The file created at `$HOME/.bahmni-reports/bahmni-reports-test.properties` comes with default values.

   2. Install MySQL client and server in your machine. If you already have a MySQL server available make sure that the user has the privileges to dump the database.

   3. Change `openmrs.url` in the properties file to set the host and port of the MySQL server.

   4. Change `openmrs.username` to set the database user.

   5. Change `openmrs.password` to set the database user password

   6. Run `./mvnw clean install -DskipDump=false` to build your changes and run integration tests. If you are using an IDE you can directly run a specific integration test.

**Note:**

OpenMRS 2.1.6 and its corresponding schema dump is on MySql 5.6(**preferred**). There are breaking changes between 5.6 and 5.7

E.g. only_full_group_by is enabled by default 

We are mutating global and session sql_mode as workaround to make 5.7 almost similar to 5.6. For reference check github action workflow. 

