# CP1, done

Reference material for `../2.8.9-upgrade-plan.md`. Shipped together with CP2 as
[PR #106](https://github.com/Bahmni/bahmni-reports/pull/106) (branch
`BAH-5073-fix-integration-tests`, off `master`, under ticket **BAH-5073**), not as
commits on `BAH-5058`. See the note in the plan's Branching section for why, and what
that means for resuming this plan.

## Result

341 tests, **0 failures, 0 errors**, 4 skipped. Confirmed twice in CI on `mysql:5.6`
(native, GitHub-hosted runner): once at 341/57/0/4 (two classes not yet fixed), once
fully green after the remaining two were fixed. CI wall time ~7.5-12 minutes; a local
run on this dev machine took 40+ minutes because Docker was emulating `mysql:5.6`
under qemu (arm64 host, amd64-only image) while the machine was also swapping under
memory pressure from other apps. Neither condition applies to CI.

## The four layers under cause D

Cause D in the plan ("dbunit `AmbiguousTableNameException: USERS`") turned out to be
the first of four independent, stacked problems, each hidden behind the one before it
because CI has never run this suite (both workflows passed `-DskipTests` since Travis
was introduced in 2017 -- confirmed by reading the full `.travis.yml` and
`validate_pr.yml` history, not assumed).

**Layer 1: `deleteAllData()`'s unscoped JDBC metadata scan.**
`BaseContextSensitiveTest.deleteAllData()` (openmrs-api's own test base class, not
this repo's code) calls `DatabaseMetaData.getTables(null, "PUBLIC", "%", null)` --
disassembled via `javap` against `openmrs-api-2.5.7-tests.jar` to confirm, rather than
guessed. A null catalog returns every database visible to the connecting user, and a
null table-type filter returns views alongside tables. Against `root` (what
`create_configuration.sh` actually connects as), that pulled in tables from the
sibling `bahmni_reports_it` database (e.g. `scheduled_report`) and views this schema's
own reports query directly (e.g. `diagnosis_concept_view`), which then failed
`DELETE_ALL` because non-updatable joined views can't be deleted from.

Fix: `deleteAllData()` isn't overridable, but it calls `this.getConnection()`
virtually, so `BaseIntegrationTest.getConnection()` now returns a dynamic proxy
(`java.lang.reflect.Proxy`) that narrows exactly that one unscoped `getTables` call
to the current catalog and `TABLE`-only, and leaves every other call untouched.

**Layer 2: a Lucene reindex with no relevance here.**
`deleteAllData()` always finishes with `updateSearchIndex()`, reindexing
`ConceptName`, `Drug`, `PersonName`, `PersonAttribute`, `PatientIdentifier`. `Drug`'s
mapping in openmrs-api 2.5.7 (`Drug.hbm.xml`, extracted from
`openmrs-api-2.5.7-sources.jar`) reads `drug.dose_limit_units`, a column the schema
fixture didn't have. None of this app's reports go through OpenMRS's search API --
they run raw SQL against the tables directly -- so `updateSearchIndex()` is now a
no-op override rather than a reason to patch the schema for a reindex nothing needs.

**Layer 3: the schema fixture is stale, not just missing one column.**
`src/test/resources/sql/openmrs_schema.sql` was captured once around 2016
("Upgrading openmrs to 2.1.0-SNAPSHOT") and never refreshed as the pinned
`openMRSVersion` moved to 2.5.7. Each missing column/table was found by running one
test, reading the exact `Unknown column`/`doesn't exist` error, and verifying the
correct name/type/nullability against the real `.hbm.xml` mapping in
`openmrs-api-2.5.7-sources.jar` -- never guessed. Found and added, in the order they
surfaced:

| Table | Column/addition | Source mapping |
| --- | --- | --- |
| `users` | `email` | `User.hbm.xml` |
| `drug` | `dose_limit_units` | `Drug.hbm.xml` |
| `person` | `cause_of_death_non_coded` | `Person.hbm.xml` |
| `provider` | `role_id`, `speciality_id` | `Provider.hbm.xml` |
| `orders` | `fulfiller_comment`, `fulfiller_status`, `form_namespace_and_path` | `Order.hbm.xml` |
| `test_order` | `location` | `Order.hbm.xml` (`TestOrder` joined-subclass) |
| `referral_order` | whole table, mirroring `test_order` plus `location` | `Order.hbm.xml` (`ReferralOrder` joined-subclass) |

These columns are genuine OpenMRS core columns confirmed against the real jar, not
invented to satisfy an assertion. `src/test/resources/sql/openmrs_schema.sql` has no
path to production: it is read only by `create_db.sh`, which stands up a disposable
local database purely for tests. Production builds its schema from OpenMRS's own
Liquibase changesets, entirely independent of this file.

**Layer 4: an actual production bug, exposed once setup stopped failing first.**
`ReportAuthorization.getSessionId()` iterated `request.getCookies()` without a null
check. `getCookies()` returns `null`, not an empty array, when a request carries no
cookies -- true for a real first-time request in production and for every
`MockMvc`-driven test here. This NPE was never caught because no test had gotten this
far into request handling before. Fixed with a null guard; no other behaviour change.

## Two more bugs found once the suite could run for real

**`genericLabOrderReport.sql`'s "Obs Id" is a `GROUP_CONCAT`.** It returns a
comma-joined string (e.g. `"2011,2012"`) whenever an order has more than one
non-excluded obs. `GenericLabOrderReportTemplateHelper` typed that Jasper column as
`Long`, so any such order threw a `JRException`, surfacing to callers as an HTTP 500.
Retyped to `String`. Confirmed `genericObservationReport.sql`'s own, differently-named
"Obs Id" column is a plain, unaggregated value and left that helper untouched.

**Stale `.0` in test literals, the same pattern as `PatientAttributesHelperTest`
(cause C).** Many assertions in `GenericLabOrderReportTest` and
`GenericObservationReportTest` asserted on `Timestamp.toString()`'s trailing `.0`.
Confirmed via `SHOW COLUMNS` that the backing columns (`orders.date_activated`,
`obs.obs_datetime`, `visit.date_started`, etc.) are plain `DATETIME` with no
fractional-seconds precision, so `.0` carries no information. Never checked before
because CI never ran. Fixed by stripping `.0` only when it immediately follows an
`HH:mm:ss` timestamp in the literal, so no unrelated decimal in either file was
touched -- confirmed by a scoped regex, not a blind find-and-replace.

## A cheap side effect worth keeping: quiet test logs

No test-scoped logging config existed, so `logback-classic` (added to the classpath in
an earlier, unrelated security-dependency bump) fell back to its default and every
test run emitted DEBUG-level output for `org.dbunit`, `org.hibernate`, `org.openmrs`.
A single class's run log ran past 100,000 lines; the full suite produced multiple
gigabytes and made CI logs unreadable. Added `src/test/resources/logback-test.xml`
at `WARN` -- Logback's own mechanism for a test-only override, with no effect on the
application's own `log4j2.properties` used at runtime.

## Also fixed along the way, not part of any layer above

- `xerces`/`xml-apis` excluded from both `openmrs-api` entries in `pom.xml` (cause B,
  already diagnosed before this session; applied here).
- `commons-compress` had been excluded from `poi-ooxml` entirely (`BAH-3884`, dodging
  a CVE) instead of pinned to a patched version, which silently broke every `.xlsx`
  read/write path -- in production as well as tests, since nothing had ever exercised
  it. Pinned `commons-compress:1.28.0` (well past the `1.26.0` fix for
  `CVE-2024-25710`) and bumped `commons-io` `2.7` -> `2.20.0` to match (`1.28.0` needs
  `IOIterator`, added in `commons-io` `2.12.0`).

## Hypotheses tested and rejected earlier, before this session's schema-drift work

Kept for anyone re-deriving this: do not spend time on these again.

- **MySQL 8 is the problem.** Rejected: moving to MySQL 5.6 only changed which table
  name dbunit reported (`REPLICATION_ASYNCHRONOUS_CONNECTION_FAILOVER` to `USERS`),
  not the failure or its count.
- **`nullCatalogMeansCurrent=true` on the JDBC URL.** Rejected: this is the textbook
  Connector/J 8 explanation and looked like an exact match, but adding it made no
  difference to the counts. The real fix needed code (the `getConnection()` proxy),
  not a connection property.
- **A restricted MySQL user (`bahmnitest`) that can't see `performance_schema`.**
  This was tried mid-investigation and genuinely fixed the *first* symptom
  (`AmbiguousTableNameException: USERS`), but was superseded once the `getConnection()`
  proxy fix landed, which scopes the catalog in code and works correctly with the
  plain `root` user that `create_configuration.sh` actually configures -- what CI and
  any fresh local setup use. No custom MySQL user is needed.

## Adversarial review, applied

A medium-effort review of the diff's production-facing files
(`ReportAuthorization.java`, `pom.xml`'s dependency changes,
`GenericLabOrderReportTemplateHelper.java`, `validate_pr.yml`) plus a lighter pass over
`BaseIntegrationTest.java`'s new proxy found two real issues, both fixed before merge:

- The proxy's `InvocationHandler`s called `method.invoke(...)` directly. JDK dynamic
  proxies wrap any checked exception the handler throws that isn't declared by the
  invoked interface method into `UndeclaredThrowableException` -- and
  `Method.invoke`'s own `InvocationTargetException` is never one of those declared
  types. Any real `SQLException` through the proxy (a constraint violation, a dropped
  connection) would have surfaced as the wrong exception type. Added an `invokeReal()`
  helper that catches `InvocationTargetException` and rethrows `getCause()`.
- A comment on `getConnection()` attributed the cross-database visibility problem to
  the `bahmnitest` user from the rejected-hypothesis investigation above, but the real
  config connects as `root`. Corrected.

It also found two CI-only issues, folded into CP2's evidence since that's where they
live.
