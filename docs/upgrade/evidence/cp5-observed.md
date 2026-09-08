# CP5, done -- OpenMRS 2.5.7 to 2.8.9

Reference material for `../2.8.9-upgrade-plan.md`.

## Result

341 tests, 0 failures, 0 errors, 4 skipped. Matches the CP4 baseline exactly. Cause diff
against `/tmp/cp4-test.log` is empty. `dependency:tree` shows
`openmrs-api:jar:2.8.9:test`, `openmrs-api:test-jar:tests:2.8.9:test`,
`org.testcontainers:mysql:jar:1.21.3:test`, `liquibase-core:jar:4.32.0:compile`. All
three diagnostic greps (`GlobalConfiguration`, `MySQLContainer`, `MockitoException`)
print 0.

The pom edit itself was exactly as sized: bump `openMRSVersion`, bump `liquibase-core`
to 4.32.0 per CP3, add `org.testcontainers:mysql` at test scope (pinned to **1.21.3**,
read directly off openmrs-api 2.8.9's own parent pom's `dependencyManagement`, rather
than guessed). None of that was where the risk was. The suite went from green to **296
errors** on the first run, and getting back to green took four distinct, unrelated
fixes, none of which the plan anticipated. Each is a real behavior change forced by the
OpenMRS version jump, not a mistake in the pom edit.

## Finding 1: a live Docker daemon is now a hard test-time dependency

From openmrs-api 2.6.0, `BaseContextSensitiveTest`'s constructor calls
`Containers.ensureDatabaseRunning()` unconditionally whenever `useInMemoryDatabase()`
returns false -- true for every subclass of this repo's `BaseIntegrationTest`. That
method spins up its own throwaway `mysql:5.7.39` Testcontainers container via the raw
Docker API, regardless of whether `getRuntimeProperties()` is overridden downstream to
point somewhere else (it is, here). There is no system property or override point to
skip it; the only static field gating it (`Containers.mysql`) is private.

Locally this failed outright: `IllegalStateException: Could not find a valid Docker
environment`, with `BadRequestException (Status 400: ...)` from both
`UnixSocketClientProviderStrategy` and `DockerDesktopClientProviderStrategy`. This is a
known, documented incompatibility between Testcontainers < 2.x's bundled docker-java
client and Docker Desktop >= ~27 (client API negotiation against Docker Engine 29.x).
Confirmed via web search, not specific to this repo. Fixed with a new test-scope
resource, `src/test/resources/docker-java.properties`, containing `api.version=1.44`,
which pins the client to an API version both sides speak. GitHub Actions'
`ubuntu-latest` runner ships a Docker version old enough that this incompatibility
likely does not reproduce there, but the file is harmless either way and now guards
against every developer machine running a recent Docker Desktop.

The throwaway container this forces is never actually used for anything -- OpenMRS's
own `createSchema()` runs its Liquibase createdb changelog against it, but this
project's `getRuntimeProperties()` override replaces the connection details before any
Spring bean reads them. It is pure overhead (union of an image pull plus container
start, once per JVM fork, ~15-20s here), not a correctness risk.

## Finding 2: commons-compress 1.28.0 needs commons-lang3 3.18.0, and lost the fight

Once Docker was reachable, `Containers.ensureMySQLRunning()` failed differently:
`NoClassDefFoundError: org/apache/commons/lang3/ArrayFill` inside
`GenericContainer.tryStart()` -> `TarArchiveOutputStream` (part of copying files into
the container). `commons-compress:1.28.0` (already pinned in this pom, unrelated to
this checkpoint) declares `commons-lang3:3.18.0` in its own pom, but this project's
`commons-text:1.11.0` pulls `commons-lang3:3.13.0` transitively, and Maven's
same-depth, first-declaration-wins mediation picked the older one. `ArrayFill` was
added to commons-lang3 in 3.18.0. This dependency collision existed latently before
this checkpoint; it only surfaced because Testcontainers is the first thing on this
classpath to actually exercise that `commons-compress` code path. Fixed with an
explicit direct dependency, `org.apache.commons:commons-lang3:3.18.0`, in `pom.xml`.

## Finding 3: OpenMRS's DBUnit harness now hardcodes the database name to "openmrs"

The real failure, and the one worth generalizing. From some version between 2.5.7 and
2.8.9, two places in OpenMRS's own test harness stopped taking the database's catalog
from the connection and started taking it from fixed configuration instead:

- `BaseContextSensitiveTest.deleteAllData()` calls
  `connection.getMetaData().getTables(System.getProperty("databaseName"), "PUBLIC",
  "%", null)` -- a system property, not `null`, unlike at 2.5.7.
- `OpenmrsMetadataHandler` (new class, used as DBUnit's
  `DatabaseConfig.PROPERTY_METADATA_HANDLER` for every real-database test) calls
  `databaseMetaData.getTables(OpenmrsConstants.DATABASE_NAME, schemaName, ...)` for
  every `tableExists()`/`getTables()` check DBUnit makes during REFRESH/DELETE_ALL.

Both default to the literal string `"openmrs"` -- exactly the database name
`Containers.ensureMySQLRunning()` gives its own throwaway container (Finding 1), which
is presumably why nobody at OpenMRS noticed this coupling. This project's real
database is `reports_integration_tests` (see `bahmni-reports-test.properties`), so
every DBUnit table lookup silently scoped itself to a catalog that doesn't exist on
this server, surfacing as `NoSuchTableException: person` and, once that was fixed,
`NoSuchTableException: diagnosis_concept_view`.

Confirmed by enabling MySQL's general query log during a run and observing `SHOW FULL
TABLES FROM \`openmrs\`` arrive on the connection that had just authenticated "on
reports_integration_tests" -- the JDBC connection was correct throughout; only the
catalog argument DBUnit's metadata handler passed was wrong.

Fixed in `BaseIntegrationTest.java`:
- `getRuntimeProperties()` now also sets `OpenmrsConstants.DATABASE_NAME` (a mutable
  `public static String`, not `final` -- an intentional OpenMRS extension point) and
  `System.setProperty("databaseName", ...)` to the real database name, parsed out of
  the existing `connection.url`.
- The existing `wrapMetaDataExcludingViews()` proxy (added in CP1 to keep
  `diagnosis_concept_view` out of `deleteAllData()`'s DELETE_ALL) matched only when
  the catalog argument was `null`. Once the database name fix made the catalog
  argument non-null, that condition stopped matching and views came back into scope
  for deletion. Loosened the match to trigger on an unscoped **table-type** filter
  alone (`args[3] == null`), using the caller's catalog when given and falling back to
  the connection's own catalog only when it is `null` -- covering both the pre-2.6 and
  post-2.6 caller shapes.

This is the finding most worth carrying into the skill: bumping a test-scope-only
dependency changed the identity of the database an existing, working proxy-based
workaround was reasoning about, and the workaround's own matching condition (not its
intent) went stale silently. A green compile gave zero warning of this.

## Finding 4: a real, version-forced schema gap in the test fixture

Even after Finding 3's fix, two tests
(`OrderFulfillmentReportTest`, `DrugOrderReportTest`) still failed on
`org.hibernate.exception.SQLGrammarException` wrapping `Unknown column
'globalprop0_.edit_privilege' in 'field list'`. Checked OpenMRS's own bundled
`liquibase-schema-only-*.x.xml` snapshots (found inside the `openmrs-api` jar itself)
across every minor from 2.1.x to 2.7.x: `global_property.edit_privilege` (plus
`view_privilege` and `delete_privilege`, all three FK'd to `privilege.privilege`) is
**absent through 2.6.x and present from 2.7.x**. This is a genuine schema addition
inside the version range this checkpoint jumped, not a latent CP1-era gap -- CP1's
fixture patch was correct for 2.5.7's own Hibernate mappings, which never touched
these columns.

Fixed by adding the three columns plus their FKs to `global_property` in
`src/test/resources/sql/openmrs_schema.sql`, matching OpenMRS's own 2.7.x column
types and constraint shape exactly. Confirms the plan's framing of CP7 risk is right in
spirit (schema drift is real and compiler-invisible) but shows it starts arriving as
early as the version-bump checkpoint itself, for objects OpenMRS's own Hibernate
mappings touch, well before CP7's own concern (this project's *report* SQL against a
*production* schema).

## Fixed opportunistically: DBUnit MySQL data type factory warning

Not a failure, but the user asked about the noise during this session:
`BaseContextSensitiveTest.setupDatabaseConnection()` only registers a
`DatabaseConfig.PROPERTY_DATATYPE_FACTORY` for the in-memory H2 path; the real-database
path gets none, so DBUnit falls back to `DefaultDataTypeFactory` and warns on every
table it inspects that "MySQL" isn't a recognised product. `dbunit:2.4.7` (already a
dependency) bundles `org.dbunit.ext.mysql.MySqlDataTypeFactory`, unused until now.
`setupDatabaseConnection()` is `protected`, not `final`, so `BaseIntegrationTest` now
overrides it to call `super` and additionally register that factory. Cosmetic only, no
behavior change; kept because it was directly asked for and cost one method.

## Environment note: stale local properties file

Also hit and fixed, not a code change: this machine's
`~/.bahmni-reports/bahmni-reports-test.properties` had `openmrs.username=bahmnitest`
from an earlier manual setup, not the `root`/`root` that
`scripts/create_configuration.sh` actually generates (matching CI's
`MYSQL_ROOT_PASSWORD=root`). That user lacks `SUPER`, which the schema dump's
`DEFINER=`root`@`localhost`` views require to (re)create, so reloading the fixture
after the schema edit failed with `Access denied ... SUPER privilege`. Re-ran
`create_configuration.sh` to regenerate the file correctly, then `create_db.sh` applied
cleanly. Worth remembering for the next checkpoint that touches the schema fixture:
regenerate configuration before reloading it, don't assume a prior session's file is
still what the scripts would produce.

## Caught by review: a greedy regex in the Finding 3 fix

The medium-effort review requested before committing (per the plan's review-tier process)
found one real, confirmed bug: the regex originally used to pull the database name out of
`connection.url` (`.*/([^/?]+)(\?.*)?$`) is greedy and matches the *last* `/` anywhere in
the string, including inside the query string. A JDBC URL with a `/` in a query parameter
(e.g. `trustCertificateKeyStoreUrl=file:///certs/truststore.jks`, a realistic TLS-enabled
MySQL URL) would silently extract `truststore.jks` instead of the real database name --
exactly the `NoSuchTableException` failure mode this fix exists to prevent. Doesn't trip on
this repo's current URL (no `/` in its query string), so the test suite gave no signal
either way. Fixed by splitting off the query string first (`split("\\?", 2)[0]`) and taking
the substring after the last `/` of what remains, rather than one regex trying to do both.
Re-ran the full suite afterward: still 341/0/0/4.

## Fed back to the skill

Added to `openmrs-upgrade-feasibility`: the Testcontainers-Docker-Desktop API version
incompatibility and its `docker-java.properties` fix; the `Containers.mysql` /
`OpenmrsConstants.DATABASE_NAME` hardcoding as a named risk for any consumer repo with
its own `getRuntimeProperties()` override; the commons-compress/commons-lang3
`ArrayFill` version floor as a forced-dependency pattern (a transitive dependency's own
declared minimum losing to an unrelated direct dependency's transitive pin); and the
technique of reading a target version's bundled `liquibase-schema-only-*.x.xml`
snapshots out of the jar to find exactly which schema versions introduced a column, as
a faster and more precise alternative to standing up two live databases when the
question is scoped to a handful of columns.
