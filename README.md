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

The tests run against **MySQL 8.0**, matching what Bahmni deploys. The schema fixture in
`src/test/resources/sql/openmrs_schema.sql` is a dump from OpenMRS 2.8.9 and loads on 8.0 with
the same object counts it produces on 5.6.

Two server settings matter, and both are handled rather than assumed:

- `sql_mode` must not include `ONLY_FULL_GROUP_BY`, which MySQL 8 enables by default and the
  report SQL does not satisfy. Set it to the same value `bahmni-docker` uses
  (`OPENMRS_DB_SQL_MODES`); the CI workflow does this explicitly and fails loudly if the mode is
  still present.
- `optimizer_search_depth` is set to `0` on the OpenMRS connection, via `sessionVariables` in the
  JDBC URL. Left at the MySQL 8 default of 62, `observationFormReport.sql` (a 22-table join)
  spends 370+ seconds in join-order planning rather than execution, which reads as a hang rather
  than a failure. `0` lets MySQL pick the depth and brings the same query back to about a second.
  This lives in the JDBC URL so it does not depend on each deployment configuring its server.

Creating the `obsParent` function needs either `SUPER` or `log_bin_trust_function_creators=1`
when binary logging is on, which it is by default on MySQL 8. `create_db.sh` connects as `root`,
which holds `SUPER`, so local and CI runs are unaffected. Deployments that run this service's
Liquibase against a MySQL 8 OpenMRS database as a non-`SUPER` user need that flag set
server-side.

### SNOMED Integration Support

bahmni-reports also integrates with SNOMED for descendant-based reporting by querying the terminology server. More details can be found in [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/90472551/Reports) Reports documentation and also [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/3132686337/SNOMED+FHIR+Terminology+Server+Integration+with+Bahmni) Wiki link

### Integration of Third-Party Extensions

Discover how bahmni-reports effortlessly integrates third-party extensions. Check [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/3230760978/SNOMED+to+ICD10+Mapping) documentation for details.