# CP1, in progress

Reference material for `../2.8.9-upgrade-plan.md`. CP1 is **not complete**. This records what was
done on 2026-09-07, what it changed, and what is still failing, so the next session does not
repeat any of it.

## Environment set up, all verified by running things

| Step | Result |
| --- | --- |
| `brew install mysql-client` | 26.7.0 installed. Keg-only, so it needs `export PATH="/opt/homebrew/opt/mysql-client/bin:$PATH"`. |
| MySQL root credentials | Set to password `root` with plugin `mysql_native_password`. |
| `scripts/create_configuration.sh` | Run. `~/.bahmni-reports/` holds both properties files. |
| Database engine | Switched from brew `mysql@8.0` to Docker `mysql:5.6.51`, matching CI. `brew services stop mysql@8.0` was run, so port 3306 is the container's. |
| `src/test/resources/create_db.sh` | Run against 5.6. `reports_integration_tests` 184 tables, `bahmni_reports_it` 12 tables. |

The Docker container is `bahmni-reports-mysql`. It survives a laptop restart only if Docker is set
to start it, so expect to run `docker start bahmni-reports-mysql` tomorrow.

```bash
docker run -d --name bahmni-reports-mysql --platform linux/amd64 \
  -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:5.6
```

`mysql:5.6` is published for amd64 only, so on this aarch64 machine it runs under emulation.
That is fine in practice: it accepted connections about 15 seconds after start.

Two details that matter and cost time to find:

- The `mysql_native_password` plugin is not cosmetic. With MySQL 8's default
  `caching_sha2_password`, Connector/J 8.0.28 fails against a plaintext JDBC URL with
  "Public Key Retrieval is not allowed", because `create_configuration.sh` does not set
  `allowPublicKeyRetrieval=true`. MySQL 5.6 only offers native password, so on the container
  this is moot, but it matters if anyone goes back to the brew 8.0 server.
- `create_db.sh` exits 0 even when its `DROP DATABASE` calls fail, because it has no `set -e` and
  those calls lack `IF EXISTS`. Always verify with table counts, never with its exit code.

## Progress against the suite

All runs on JDK 11.0.28, which matches CI.

| Run | Result | Dominant cause |
| --- | --- | --- |
| CP0 baseline, no config, brew MySQL 8 | 341 tests, 2 failures, 327 errors | 296 `FileNotFoundException` plus `NullPointerException`, missing config file |
| Config generated, databases provisioned, MySQL 8 | 341 tests, 2 failures, 327 errors | 296 `DatabaseUnitRuntimeException`, `AmbiguousTableNameException: REPLICATION_ASYNCHRONOUS_CONNECTION_FAILOVER` |
| Same, on Docker MySQL 5.6 | 341 tests, 2 failures, 327 errors | 296 `DatabaseUnitRuntimeException`, `AmbiguousTableNameException: USERS` |

The error total has not moved, but the **cause** has changed twice. Cause A, the missing config
file, is genuinely fixed and will not come back. This is exactly the situation the plan warns
about: comparing counts would have shown no progress, comparing causes shows two layers peeled off.

## Where it got to, 2026-09-07 late

Applied and committed:

- **Cause B**, `xerces`/`xml-apis` excluded from both `openmrs-api` entries in `pom.xml`. `dependency:tree` now shows neither.
- **Cause C**, trailing `\n` removed from the two literals in `PatientAttributesHelperTest`.
- **Cause D, partially.** Created a MySQL user `bahmnitest` with grants only on the two test databases, so `performance_schema` is invisible to it, and pointed the test properties at it.

Measured effect:

| | CP0 | end of 2026-09-07 |
| --- | --- | --- |
| Tests run | 341 | 341 |
| Failures | 2 | **0** |
| Errors | 327 | 327 |
| Dominant error | missing config file | `NoSuchTableException: diagnosis_concept_view` |

Failures are gone, which confirms cause C. `AmbiguousTableNameException` is gone entirely, which
confirms the restricted-user hypothesis. The error count has not moved because each fix reveals the
next layer in the same OpenMRS method.

A full run takes about 10 minutes, because `mysql:5.6` runs under amd64 emulation on this machine.

## The third layer, and what it probably means

```
org.dbunit.DatabaseUnitRuntimeException: org.dbunit.dataset.NoSuchTableException: diagnosis_concept_view
    at org.openmrs.test.BaseContextSensitiveTest.deleteAllData(BaseContextSensitiveTest.java:880)
```

`reports_integration_tests` contains three **views**: `concept_reference_term_map_view`,
`concept_view` and `diagnosis_concept_view`. dbunit 2.4.7 enumerates them through JDBC metadata,
which lists views alongside tables, and then fails to treat them as tables.

This is not a permissions problem. `bahmnitest` selects from `diagnosis_concept_view` successfully,
returning 0 rows with no error. The views' `DEFINER` is `root@localhost` with `security_type
DEFINER`, and that turned out to be a red herring.

**The pattern matters more than this particular error.** Three independent environment-level
blockers, all inside `BaseContextSensitiveTest.deleteAllData()`: a missing config file, cross-schema
table-name ambiguity, and now views. That is the signature of a suite that has never run in this
configuration, not one that regressed.

Two pieces of evidence support that reading:

1. CI has never run these tests. Both workflows pass `-DskipTests`.
2. **PR #100 added a `unit-test` Maven profile that excludes exactly the 13 integration test
   classes**, including every one failing here. The previous attempt hit this same wall and routed
   around it rather than fixing it.

## Recommended next step, and it is a decision not a task

Before more debugging, someone should decide what "green baseline" means for this repo. The two
options are materially different in cost:

- **Split the suite.** Define the baseline as the unit tests, which are roughly 31 of the 341, and
  quarantine the integration tests behind a profile with a tracked reason and a ticket. This is what
  PR #100 did. It unblocks CP2 through CP9 within a day. The cost is that the 38 SQL reports lose
  their only automated coverage, which makes CP7 manual verification the sole safety net rather than
  a second one.
- **Fix the integration harness.** Make `deleteAllData()` work, probably by getting dbunit to
  exclude views, which needs a hook into OpenMRS's own test base class. Unknown effort, possibly
  upstream. Keeps real coverage over the reports.

This is a genuine trade-off about how much verification the upgrade deserves, so it belongs with a
human rather than being settled by whoever is executing. Raise it before continuing.

## What was still failing before tonight's fixes, and what it is not

296 errors, all the same shape:

```
org.dbunit.DatabaseUnitRuntimeException: org.dbunit.database.AmbiguousTableNameException: USERS
    at org.openmrs.test.BaseContextSensitiveTest.deleteAllData(BaseContextSensitiveTest.java:880)
    at org.bahmni.reports.report.integrationtests.BaseIntegrationTest.setUpTestData(BaseIntegrationTest.java:125)
    at org.bahmni.reports.report.integrationtests.BaseIntegrationTest.beforeBaseIntegrationTest(BaseIntegrationTest.java:113)
```

dbunit 2.4.7 enumerates tables through JDBC metadata without a schema qualifier, so it sees `users`
in more than one schema and refuses to proceed. Confirmed by query: `users` exists in both
`performance_schema` and `reports_integration_tests`.

The failing call is inside OpenMRS's own `BaseContextSensitiveTest`, which builds its own dbunit
connection, so it cannot be fixed from this repo's test code alone.

### Hypotheses already tested and rejected

Do not spend time on these again.

- **MySQL 8 is the problem.** Rejected. Moving to MySQL 5.6 changed which table name was reported,
  from `REPLICATION_ASYNCHRONOUS_CONNECTION_FAILOVER` to `USERS`, but the failure and its count are
  unchanged. The MySQL 8 table was a symptom, not the cause. Keeping 5.6 is still right because it
  matches CI, but it does not fix this.
- **`nullCatalogMeansCurrent=true` on the JDBC URL.** Rejected. This is the documented Connector/J 8
  behaviour change that makes `getTables(null, ...)` span all databases, so it looked like an exact
  match. Added to `openmrs.url` in the test properties and the suite still reported 341 tests,
  2 failures, 327 errors with 296 dbunit errors. The local properties edit has been reverted.

### Hypotheses not yet tried, roughly in order of promise

1. Grant the JDBC user access only to the two test databases, so `performance_schema` is invisible
   and the duplicate name disappears. Blocked by `create_configuration.sh` hardcoding `root`, so it
   needs either a script change or a properties override.
2. Check how OpenMRS 2.5.7's `BaseContextSensitiveTest.deleteAllData` constructs its
   `DatabaseConnection`, at line 880, and whether any system property or OpenMRS runtime property
   sets a dbunit schema. Read the class from the test-jar rather than guessing.
3. dbunit's `FEATURE_QUALIFIED_TABLE_NAMES`, if OpenMRS exposes any hook to set dbunit features.
4. Check whether `BaseIntegrationTest.useInMemoryDatabase()` returning `false` is the intended
   configuration here. Everything else in this suite assumes a real database, but it is worth
   confirming this is how the suite was ever meant to run, given CI has never run it.

Worth keeping in mind: this suite has never run green in CI, so there is no evidence it ever passed
in this configuration. Establishing whether it *ever* worked, and in what environment, may be faster
than assuming it did and hunting a regression.

## Cause C, settled but not yet applied

The 2 failures are `PatientAttributesHelperTest` asserting on strings that end in `\n` while
`getSql()` emits none. The resource it renders, `src/main/resources/sql/helper/patientAttributes.sql`,
has never ended with a newline: its only commit, `f4e44a7`, carries a
`\ No newline at end of file` marker. So the expected literals have been wrong since they were
written. Fix is to drop the trailing `\n` from the two literals at `PatientAttributesHelperTest.java:15`
and `:22`. Not yet done.

## Cause B, not yet applied

The `xerces`/`xml-apis` exclusions on the `openmrs-api` dependency have not been added. That work
accounts for the 26 `NoClassDefFoundError` plus 4 `IllegalAccessError` still in the run.

## No repository files were changed

Everything above is environment setup plus diagnosis. `git status` shows no modifications to
tracked files beyond the plan documents themselves.
