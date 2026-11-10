# Bahmni Reports

The reporting backend for the [Bahmni project](http://www.bahmni.org/).

[![Validate PR](https://github.com/Bahmni/bahmni-reports/actions/workflows/validate_pr.yml/badge.svg)](https://github.com/Bahmni/bahmni-reports/actions/workflows/validate_pr.yml)

# What this is

A standalone Java/Maven WAR with its own embedded Tomcat, shipped as its own Docker container. It
is **not** an OpenMRS module (OMOD) loaded by an OpenMRS host, so it picks its own Java version
and is deployed and restarted independently of OpenMRS.

It generates reports over clinical data (visits, observations, observation forms, programs, lab
orders, aggregations, implementation-supplied SQL, and SNOMED descendant-based diagnosis counts)
and renders each one as HTML, PDF, Excel including macro-driven templates, CSV or ODF. It also
schedules reports to run later and serves the generated files.

Two things about it are worth knowing before reading the code:

- **The report definitions do not live here.** A deployment's `reports.json`, in its own config
  module, is fetched over HTTP at request time and decides which reports exist, what SQL some of
  them run, and who is allowed to see them. This repository provides the report *types* that
  configuration selects from.
- **It never calls the OpenMRS Java API.** It reads the OpenMRS database directly through raw SQL
  over JDBC, and calls the OpenMRS REST API only to check the caller's privileges. The same code
  path also queries OpenELIS, Odoo and BahmniMart through one datasource abstraction. So the
  coupling that actually matters is the OpenMRS *database schema*, not a Java dependency version.

# Prerequisites

- **JDK 21.** Required to build and to run. Your shell's default JDK may be older, so set
  `JAVA_HOME` explicitly rather than relying on it.
- **MySQL 8.0**, for the integration tests. See Running Integration tests below.
- **A running Docker daemon**, also for the integration tests. The OpenMRS test harness this
  suite extends starts a throwaway MySQL container of its own on every run, whether or not you
  point the tests at your own database, and there is no switch to skip it. Without Docker the
  suite fails before any test body executes.

# Installing the application

1. Clone or download this repository.

2. Run `./mvnw clean install -DskipTests` to build it

3. Deploy the WAR file in `target/bahmnireports.war`, or build the container image under
   `package/docker/bahmni-reports/`, which is how Bahmni actually ships it

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
which holds `SUPER`, so local and CI runs are unaffected.

A deployment hits this only if its OpenMRS database has no `liquibasechangelog` row for that
changeset *and* the connecting user lacks `SUPER`. A distro install is not that case: the
shipped `openmrs-db` images restore a dump that already records the changeset as applied, so
Liquibase skips the `CREATE FUNCTION`. It bites a hand-built OpenMRS database on plain
`mysql:8.0`, or an existing MySQL 8 instance that has never run this service before, where the
flag needs setting on the database server.

### SNOMED Integration Support

bahmni-reports also integrates with SNOMED for descendant-based reporting by querying the terminology server. More details can be found in [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/90472551/Reports) Reports documentation and also [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/3132686337/SNOMED+FHIR+Terminology+Server+Integration+with+Bahmni) Wiki link

### Integration of Third-Party Extensions

Discover how bahmni-reports effortlessly integrates third-party extensions. Check [this](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/3230760978/SNOMED+to+ICD10+Mapping) documentation for details.