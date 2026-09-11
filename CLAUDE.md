# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

`bahmni-reports` is the reporting backend for [Bahmni](http://www.bahmni.org/): a standalone Java/Maven **WAR** with its own embedded Tomcat (`amazoncorretto:21` base image, `bahmni-embedded-tomcat-8.0.42.jar` fetched at Docker build time), deployed as its own container, not as an OpenMRS module (OMOD) loaded by an OpenMRS host. It generates reports (visits, observations, forms, programs, lab orders, aggregations, custom SQL, SNOMED-based diagnosis counts) and renders them as HTML, PDF, Excel (including macro-templated Excel), CSV, or ODF.

**It never calls the OpenMRS Java API.** All of `src/main/java` talks to OpenMRS purely via raw JDBC/SQL (`src/main/resources/sql/*.sql`) and the OpenMRS REST API (for auth/privilege checks). `org.openmrs.api:openmrs-api` is a **test-scope-only** dependency, used exclusively to run integration tests against a real, Hibernate-mapped OpenMRS-shaped schema. Never assume a production code path exercises OpenMRS Java classes.

## Build and test commands

```bash
# Build the WAR (target/bahmnireports.war), skipping tests
./mvnw clean install -DskipTests

# Compile only (also runs the config/schema-generation exec-plugin steps below)
./mvnw clean test-compile

# Full test suite -- requires MySQL 8.0 on localhost:3306 (root/root), a running Docker
# daemon (the OpenMRS harness starts its own throwaway container regardless), and the
# test config + schema fixtures already generated (see "Running integration tests" below)
./mvnw clean test

# Run one test class (or a comma-separated list) while iterating
./mvnw test -Dtest=GenericObservationReportTest

# Regenerate ~/.bahmni-reports/{bahmni-reports,bahmni-reports-test}.properties
sh scripts/create_configuration.sh

# Drop and recreate the two integration-test MySQL schemas from the fixtures
export PATH="/opt/homebrew/opt/mysql-client/bin:$PATH"   # mysql client is keg-only on macOS
sh src/test/resources/create_db.sh
```

**JDK: must be 21.** The shell's default JDK may not match what CI uses; always `export JAVA_HOME` to a JDK 21 install explicitly before running Maven, don't rely on the ambient default.

**Running integration tests locally (from the README):** the `skipConfig`/`skipDump` pom properties gate two `exec-maven-plugin` steps bound to `test-compile` (`scripts/create_configuration.sh` and `src/test/resources/create_db.sh`); both default to `true` (skipped). Either pass `-DskipConfig=false -DskipDump=false` to `./mvnw clean package`, or run both scripts manually once before `./mvnw clean test`. `create_configuration.sh` always writes `openmrs.username=root`/`openmrs.password=root` into the generated properties file, matching CI's `MYSQL_ROOT_PASSWORD=root` service container — if a stale hand-edited properties file is lying around with different credentials, regenerate it rather than trusting it. The schema dump's 3 views (`concept_reference_term_map_view`, `concept_view`, `diagnosis_concept_view`) carry no explicit `DEFINER` clause, so whichever user runs `create_db.sh` becomes `CURRENT_USER` at `CREATE VIEW` time; using root there is what keeps that consistent with CI rather than a `SUPER`-privilege requirement.

**MySQL 8.0**, matching what Bahmni deploys. Two server-side facts are load-bearing and both are handled explicitly rather than inherited from defaults:

- `sql_mode` must **not** contain `ONLY_FULL_GROUP_BY`. MySQL 8 enables it by default and the report SQL does not satisfy it. `validate_pr.yml` sets the mode to the same value `bahmni-docker` uses (`OPENMRS_DB_SQL_MODES`) and fails loudly if the flag survives. Locally, start MySQL with the same `--sql-mode`.
- `optimizer_search_depth=0` is set on the OpenMRS connection through `sessionVariables` in the JDBC URL (`create_configuration.sh` and `bahmni-reports.properties.template`). **Do not remove it.** At MySQL 8's default of 62, `observationFormReport.sql` renders a 22-table join that spends 370+ seconds in join-order *planning* — `state=statistics`, not execution — so it presents as a hang, not a failure. `ObservationFormReportTest` goes from unfinishable to 45 tests in 86s with it set. It is in the URL, not the server config, so it does not depend on each deployment getting its server right.

Creating the `obsParent` function requires `SUPER` or `log_bin_trust_function_creators=1` when binary logging is on, which is the MySQL 8 default. `create_db.sh` connects as `root`, which has `SUPER`, so local and CI runs are fine. It bites only a deployment whose OpenMRS database has no `liquibasechangelog` row for that changeset *and* whose connecting user is not `root`; the fix is `--log-bin-trust-function-creators` on the `openmrsdb` service and belongs in `bahmni-docker`. A distro install is not that case: `bahmni/openmrs-db:3.0.0-lite-mysql8.0` and `bahmni/openmrs-db:1.0.0-standard` both restore a dump that already records all six of this changelog's `Reports-*` changesets as applied, so Liquibase skips the `CREATE FUNCTION` entirely. The lite MySQL 8 image nonetheless ships **no** `obsParent` function, because `mysqldump` omits routines without `--routines`, and the changeset carries no `runOnChange`, so Liquibase will never recreate it; the standard image does ship the function. Nothing in `src/main/resources/sql/` or in the reference `clinic-config` calls it, so this is latent rather than breaking.

## Request-handling architecture

`Initializer` (a `WebApplicationInitializer`, no `web.xml`) registers a `DispatcherServlet` backed by the single `@Configuration` class `SecurityConfig` (`@ComponentScan("org.bahmni.reports")`, `@EnableWebMvc`), which also wires `AuthenticationFilter` as a global interceptor.

`GET /report?name=X&startDate=...&endDate=...&responseType=...` (`MainReportController`) flows as:

1. `ReportAuthorization` checks the caller has the report's required privilege by calling back to the **OpenMRS REST API** (`openmrs.service.rootUrl`) — there is no local user/session store.
2. `ReportGenerator.invoke()` calls `Reports.find(name, configFileUrl, httpClient)`, which **fetches `reports.json` over HTTP** via `httpClient` (not a local file read) from `bahmniReportsProperties.getConfigFileUrl()` (`reports.config.url`). In a real deployment this points at the implementation's config module, e.g. `.../openmrs/apps/reports/reports.json`.
3. `reports.json` entries deserialize into `Report` subclasses via Jackson polymorphic dispatch keyed on the JSON `"type"` field (`@JsonTypeInfo`/`@JsonSubTypes` in `model/Report.java` — the single place that maps a config `type` string, e.g. `"MRSGeneric"`, `"aggregation"`, `"visits"`, to a concrete `Report`/`BaseReportTemplate` pair). Adding a new report type means adding both classes and registering the type string there.
4. Each `Report` returns a `BaseReportTemplate`, which is annotated `@UsingDatasource("openmrs"|"openelis"|"openerp"|"bahmniReports"|"bahmniMart")`. `AllDatasources.getConnectionFromDatasource(template)` reads that annotation via reflection and hands back a `Connection` from the matching Spring-managed `DataSource` bean — this is how one service transparently queries OpenMRS (MySQL), OpenELIS (Postgres), Odoo/OpenERP (Postgres), its own scheduling DB, and BahmniMart (a separate analytics DB) through one code path.
5. `BahmniReportUtil.build()` turns the template + connection into DynamicReports/JasperReports builders; multiple builders can be concatenated into one multi-tab output (`ConcatenatedReportTemplate` family).
6. `JasperResponseConverter` renders to the requested `responseType` (HTML/PDF/XLS/CSV/ODF), including custom-Excel-with-macros support (an uploaded `.xls` template's `Sheet1` formulae operate on data placed in a generated `"Report"` sheet).

Report scheduling (`GET /schedule`, `GET /getReports`, `GET /download/{id}`) is a parallel path through `ReportsScheduler` (Quartz-based, `scheduler/` package), persisting `ScheduledReport` rows and generated files that `CleanReportsJob` later purges by age (`reports.cleanup.keepItForNDays`).

All runtime config is read from `~/.bahmni-reports/bahmni-reports.properties` via `BahmniReportsProperties` — see that class for the full property list (datasource URLs/credentials, OpenMRS REST endpoint, macro/cleanup/mart settings). This file is generated from a template (`package/docker/bahmni-reports/template/bahmni-reports.properties.template`) via `envsubst` at container start (`start.sh`), or by `scripts/create_configuration.sh` for local dev/tests.

### Startup migrations

`start.sh` runs **two separate Liquibase changelogs** before starting Tomcat: `liquibase.xml` against the **OpenMRS** database, and `liquibase_bahmni_reports.xml` against this service's **own** database — both via `package/resources/run-liquibase.sh`, a plain `#!/bin/sh` script that builds a `java -cp` classpath by hand from jars inside the exploded WAR. It locates `liquibase-core` and `mysql-connector-java` by glob with a fail-loud, exactly-one-match check, and adds the rest of `WEB-INF/lib` via Java's own trailing `/*` classpath wildcard. It previously hardcoded exact jar filenames, which broke silently on any version bump. Do not reintroduce a pinned filename: Liquibase 4.32.0 no longer shades its runtime dependencies, so naming them one by one is itself fragile.

## Test harness architecture (non-obvious, read before touching `BaseIntegrationTest`)

Because `openmrs-api` is test-scope only, the integration tests (`src/test/java/.../report/integrationtests/`) extend OpenMRS's own `BaseContextSensitiveTest` purely to get a real, Hibernate-validated OpenMRS schema to run this app's raw SQL against — not to exercise any OpenMRS Java API this app actually uses in production. `BaseIntegrationTest` carries several workarounds that exist specifically to keep that harness pointed at *this* repo's own MySQL fixture instead of doing what OpenMRS's test framework does by default; understand all of them before changing any one:

- Overrides `getRuntimeProperties()` to connect to a real MySQL fixture (`bahmni-reports-test.properties`) instead of OpenMRS's own defaults, and also sets `OpenmrsConstants.DATABASE_NAME` / `System.setProperty("databaseName", ...)` — required because OpenMRS's DBUnit harness hardcodes database-catalog lookups to that value rather than the connection's actual catalog.
- Wraps `getConnection()` in a JDK dynamic `Proxy` to narrow `DatabaseMetaData.getTables()` calls, keeping cross-database tables and non-updatable views (e.g. `diagnosis_concept_view`, which this app's reports query directly) out of DBUnit's `deleteAllData()`/`REFRESH` bookkeeping.
- Overrides `setupDatabaseConnection()` to register a MySQL-specific DBUnit data type factory.

Two MySQL schemas back the suite (both created by `create_db.sh` from files under `src/test/resources/sql/`):

- `reports_integration_tests` — an OpenMRS-shaped schema, from `openmrs_schema.sql`. **This is a `mysqldump --no-data` capture from a real `openmrs-core:2.8.9` instance** (taken after this repo's own `liquibase.xml` had been applied to it, which is where `reporting_age_group` and the three views come from), plus four tables carried forward by hand because nothing in a bare core provides them: `episode`, `episode_encounter`, `episode_patient_program` (episodes omod) and `reporting_concept_range`. **Regenerate it rather than hand-patching it**, as the header comment in the file itself says. Adding a column by hand is how the previous version became a 2017-era dump with later patches bolted on, passing tests against a schema shape no deployment actually had.
  `AllReportsSmokeTest` now executes the report SQL against this fixture on every run, driving 31 report types and reaching **30 of the 38** files under `src/main/resources/sql/`; counting the whole suite it is **31 of 38**, since `GenericObservationReportTest` covers `genericObservationReportInOneRow.sql`. A report type is not a SQL file: `MRSGeneric` runs implementation-supplied SQL via `config.sqlPath`, so it reaches none of the 38, and `diagnosisCount`, `obsCount` and `observations` each branch to a different file depending on config or on a concept datatype fetched over HTTP. **Seven files are executed by nothing:** `diagnosisSummary`, `diagnosisCountWithoutAgeGroup`, `codedObsCount` and `booleanConceptsCount` (each reachable by adding a config entry, the latter two also needing a stub for the `GET {root}/concept/{name}` datatype lookup), plus `testCount` (OpenELIS/Postgres, no fixture exists) and the two `tsIntegrationDiagnosis*` (need a SNOMED terminology-server stub). Their template *selection* is unit tested; only their SQL is unexercised. If you add a config entry that flips one of those branches, record a row floor for it.
- `bahmni_reports_it` — this app's own scheduling/reports schema (`bahmniReports.sql` + quartz DDL).

`mockito-core` is pinned to `5.7.0` because `jackson-databind`'s pinned `byte-buddy` version caps it — don't bump either independently; check `pom.xml`'s comments first.

Under JDK 21 the suite logs `WARNING: A Java agent has been loaded dynamically` and `Dynamic loading of agents will be disallowed by default in a future release`, from Mockito's `byte-buddy-agent`. It passes today. When a future JDK enforces that, the fix is `-XX:+EnableDynamicAgentLoading` on the surefire `argLine`, or a Mockito version that attaches the agent at startup. Not a current failure, so it is deliberately not fixed here; this note is the record of it.

## The platform versions, and why they moved as one piece of work

Test-scope `openmrs-api` is on **2.8.9** (from 2.5.7), Liquibase on **4.32.0** (from 4.8.0), the JDK on **21** (from 11), and the test and CI database on **MySQL 8.0** (from 5.6). Those are not four independent choices. Each forces the next: 2.6.0 makes Testcontainers a hard test-time dependency, 2.7.0 raises the Liquibase floor, PowerMock has no working version above JDK 17 so removing it is a precondition for the JDK move, and MySQL 8.0 is what Bahmni deploys.

Several things that look arbitrary are consequences of that, not preferences:

- **A running Docker daemon is required to run the tests.** `BaseIntegrationTest.useInMemoryDatabase()` returns `false`, so `BaseContextSensitiveTest` unconditionally starts a throwaway Testcontainers MySQL, regardless of the real database the tests are pointed at. There is no hook to skip it; the gating field is private. `src/test/resources/docker-java.properties` pins `api.version=1.44` because Testcontainers' bundled client otherwise fails API negotiation against recent Docker Engine.
- The `OpenmrsConstants.DATABASE_NAME` assignment in `BaseIntegrationTest` exists because OpenMRS's DBUnit harness scopes its catalog lookup to fixed configuration rather than the connection's actual catalog.
- The `sessionVariables` in the JDBC URL, and the deterministic `ORDER BY` now present in every report SQL file that groups, are MySQL 8 requirements rather than tidiness.

The checkpointed plan and its per-checkpoint evidence live in `docs/upgrade/`, which is **git-ignored and local to a working copy**, so a fresh clone will not have it. If you are changing the OpenMRS version, Liquibase, the JDK or the test harness and that directory is present, read it first. If it is absent, ask for it rather than re-deriving it.
