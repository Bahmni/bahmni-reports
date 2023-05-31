# Bahmni Reports

Hosts the reports web application for the [Bahmni project](http://www.bahmni.org/)

[![Build Status](https://travis-ci.org/Bahmni/bahmni-reports.svg?branch=master)](https://travis-ci.org/Bahmni/bahmni-reports)

# Installing the application

1. Clone or download this repository.

2. Run `./mvnw clean install -DskipTests` to build it

3. Deploy the WAR file in `target/bahmnireports.war`

# Running Integration tests

   1. Install MySQL client and server in your machine. If you already have a MySQL server available make sure that the user has the privileges to dump the database.
   2. Run: `./mvnw -DskipDump=false -DskipConfig=false clean package` (note this would trigger `scripts/create_configuration.sh` as part of test-compile and create respective test properties under `$HOME/.bahmni-reports/bahmni-reports-test.properties`. You can also explicitly run `scripts/create_configuration.sh` to create the properties (incase if you are using IDE to run the test)
   3. This should trigger all the tests including integration (it assumes jdbc:mysql://localhost:3306/reports_integration_tests as the DB URL)

**Note:**

OpenMRS 2.1.6 and its corresponding schema dump is on MySql 5.6(**preferred**). There are breaking changes between 5.6 and 5.7

E.g. only_full_group_by is enabled by default 

We are mutating global and session sql_mode as workaround to make 5.7 almost similar to 5.6. For reference check github action workflow. 

### SNOMED Integration Support

bahmni-reports also integrates with SNOMED for descendant based reporting by looking up terminology server. More details can be found in [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/90472551/Reports) Reports documentation and also [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/3132686337/SNOMED+FHIR+Terminology+Server+Integration+with+Bahmni) Wiki link