# Analysis behind the 2.8.9 upgrade plan

Reference material for `../2.8.9-upgrade-plan.md`. This is the evidence the verdict rests on.

You do **not** need to read this to execute the plan. Read it when you want to know why a
checkpoint exists, when a checkpoint's result contradicts an expectation, or when someone
challenges the direct-jump decision.

Written 2026-09-07 against commit `5b7f95b`. Facts about released OpenMRS artifacts are
frozen, because published Maven artifacts are immutable. Facts about this repo were true at
that commit and may have moved since.

### What the probe does not prove

A probe run before this plan compared the failing test set at 2.8.9 against the 2.5.7 control and found them identical. That comparison matched test **names**, not failure **causes**, and it was run against the red baseline, where 296 of 341 tests abort at construction on a missing config file, upstream of any OpenMRS code.

Re-examined by cause, the runs are **not** identical. `ReportAuthorizationTest.shouldInvokeCallOpenMRSWithGivenSessionId` fails as `NoClassDefFoundError: Could not initialize class OpenMRSAuthenticator$MockitoMock$446490964` at 2.5.7, and as `RuntimeException: Invoking the beforeTestMethod method on PowerMock test listener ... AnnotationEnabler failed`, caused by `MockitoException`, at 2.8.9. `MockitoException` occurrences: 0 at 2.5.7, 1 to 2 at 2.8.9.

Two consequences the rest of this plan is built around. First, 2.8.9 does change harness behaviour, which is why PowerMock removal is CP4 and precedes the version bump at CP5. Second, no behavioural claim about the ~296 masked tests at 2.8.9 exists, because they have never executed at 2.8.9. Only a green CP1 can produce one.

### What an incremental path would buy

An incremental path buys no reduction in dependency floors, because the Liquibase floor lands at 2.7.0 either way. It does buy **attribution**: if the suite goes red after the bump, a 2.6 then 2.7 then 2.8 path localises the cause to one release, whereas a direct jump leaves three releases of change undifferentiated. That is worth little against a green baseline and a great deal against a red one, which is exactly why the recommendation is conditional.

## Decision basis

| Finding | Source | Verified by |
| --- | --- | --- |
| `openmrs-api` enters the build at `test` scope only, on both the jar and the test-jar | `pom.xml:248-261` | ran `./mvnw dependency:tree`, output `org.openmrs.api:openmrs-api:jar:2.5.7:test` and `test-jar:tests:2.5.7:test`, 152 tree rows rendered |
| Main source contains zero OpenMRS Java API references. The only two hits are SQL string literals | `src/main/java/org/bahmni/reports/util/GenericReportsHelper.java:74`, `src/main/java/org/bahmni/reports/util/GenericObservationFormReportTemplateHelper.java:174` | ran grep for `org\.openmrs` over `src/main/java` with a positive control on `org\.bahmni` that returned 5 files |
| The shipped artifact is a standalone WAR with its own embedded Tomcat and Docker image, not an OMOD loaded by an OpenMRS host | `pom.xml:9`, `package/docker/bahmni-reports/Dockerfile:1` | read both files |
| `test-compile` against 2.8.9 succeeds on JDK 11 with no source changes | probe branch `spike/probe-2.8.9`, since torn down | ran `./mvnw clean test-compile`, exit 0, BUILD SUCCESS |
| At 2.8.9 the failing test **names** match the 2.5.7 control, but the failure **causes** do not. `MockitoException` appears only at 2.8.9 | probe branch `spike/probe-2.8.9-liquibase`, since torn down | ran the same `-Dtest` list on both. Name diff empty. Cause comparison shows `ReportAuthorizationTest` failing for different reasons, and `grep -c MockitoException` returns 0 at 2.5.7 and 1 at 2.8.9 |
| Liquibase 4.8.0 is too old for 2.8.9. `NoClassDefFoundError: liquibase/GlobalConfiguration$DuplicateFileMode` is raised from `org.openmrs.util.OpenmrsConstants` static init on the **test** classpath | probe log | ran the probe. Separately confirmed the symbol is absent in liquibase-core 4.8.0 and present from 4.20.0 onward by listing the jars |
| The Liquibase floor is introduced at OpenMRS 2.7.0, not at 2.8 | openmrs root poms: 2.5.7 and 2.6.17 declare 4.4.3, 2.7.0 declares 4.28.0, 2.7.9 declares 4.31.1, 2.8.0 and 2.8.9 declare 4.32.0 | fetched each `openmrs-$v.pom` from the OpenMRS Nexus and grepped `liquibase-core` |
| Every `openmrs-api` jar from 2.5.7 to 2.8.9 is Java 8 bytecode, so the artifact imposes no JDK floor above 8 | jars from `https://mavenrepo.openmrs.org/nexus/content/repositories/public` | read the class-file major version of every class in each jar. All are major 52. Positive control on a repo class returned major 55 (Java 11) |
| Java 21 becomes the minimum required version only at OpenMRS Platform 3.0.0. 2.7 "added support for Java 17" and 2.8 "added Java 21 support" | [Java 21 migration thread](https://talk.openmrs.org/t/planning-java-21-migration-for-openmrs-modules/45713), [2.7.0 announcement](https://talk.openmrs.org/t/announcing-openmrs-platform-2-7-0-release/43919), [openmrs-core releases](https://github.com/openmrs/openmrs-core/releases) | fetched all three |
| Spring stays at 5.3.30 and Hibernate at 5.6.15.Final through 2.8.9, so no `javax` to `jakarta` migration is forced | openmrs root poms 2.5.7, 2.7.9, 2.8.9 | grepped `springVersion` and `hibernateVersion` in each |
| The JUnit 4 `BaseContextSensitiveTest` and `SkipBaseSetup` that the harness extends still exist at 2.8.9 | `openmrs-api-2.8.9-tests.jar` | listed the test-jar contents for 2.5.7, 2.7.9 and 2.8.9. Present in all three |
| `xerces:xercesImpl` and `xml-apis` arrive only beneath the `openmrs-api` subtree, so excluding them there is sufficient | `dependency:tree` rows 100 to 101 | ran `dependency:tree` and read the subtree |
| Neither CI workflow runs the test suite. Both pass `-DskipTests` while naming the step "Test and build package" and "Test and Package" | `.github/workflows/validate_pr.yml:25`, `.github/workflows/build_publish.yml:39` | read both files |

Institutional context is `PARTIAL: no institutional search available`. The Unblocked MCP rejected the query because the account is on a legacy plan that does not support MCP, so Jira and the GitHub CLI were used instead. There is an older unmerged branch attempting a similar upgrade, but it carries no review discussion and is not treated as evidence here.

## API surface in use

One row per API. `Status per version` carries an entry for every intermediate release, because a change introduced mid-path does not appear in the target's release notes.

| API in use | Site (`file:line`) | Scope | Status per version | Compiler catches it | Action |
| --- | --- | --- | --- | --- | --- |
| `org.openmrs.test.BaseContextSensitiveTest` | `src/test/java/org/bahmni/reports/report/integrationtests/BaseIntegrationTest.java:34,66` | test | 2.6 present, 2.7 present, 2.8 present | yes | none |
| `org.openmrs.test.SkipBaseSetup` | `src/test/java/org/bahmni/reports/report/integrationtests/BaseIntegrationTest.java:35,65` | test | 2.6 present, 2.7 present, 2.8 present | yes | none |
| `org.openmrs.api.context.Context` | `src/test/java/org/bahmni/reports/report/integrationtests/BaseIntegrationTest.java` (authenticate, openSession, clearSession) | test | 2.6 ok, 2.7 ok, 2.8 ok | yes | none |
| `org.openmrs.util.OpenmrsConstants`, reached indirectly through OpenMRS logging init | raised in every test that touches OpenMRS logging | test | 2.6 ok, 2.7 ok, **2.8 requires Liquibase 4.20.0 or newer** | no | CP3 and CP5 |
| `import org.openmrs.*` wildcard, unresolved by scanning | `.../integrationtests/OrderFulfillmentReportTest.java:11`, `.../MedicationLogReportTest.java:10`, `.../DrugOrderReportTest.java:7` | test | resolved by a clean `test-compile` at 2.8.9 | yes | none. Expanded by compiling, not by the type-count scan |
| Domain builders over `Concept`, `Order`, `OrderType`, `Person`, `PersonName`, `Encounter`, `EncounterProvider`, `Visit`, `Obs`, `Patient`, `ConceptReferenceTerm`, `ConceptReferenceMap`, `ConceptReferenceSource` | 14 files under `src/test/java/org/bahmni/reports/builder/` | test | 2.6 ok, 2.7 ok, 2.8 ok | yes | none |
| Main-source OpenMRS Java API | none | n/a | not applicable at any version | n/a | none. This is the single most load-bearing fact in the plan |

## Class names used as data

Runtime-only coupling to upstream class names, in strings, SQL, fixtures, config or database column values. These fail silently, so each needs a named verification step.

| Location (`file:line`) | Class name | Where the value lives at runtime | How it gets verified |
| --- | --- | --- | --- |
| `src/main/resources/sql/genericObservationFormReport.sql:24,25`, `genericProgramReport.sql:15,16`, `genericObservationReportInOneRow.sql:22,23`, `visit.sql:32`, `genericObservationReport.sql:23,24`, `genericVisitReport.sql:14,15`, `observationFormReport.sql:24,25`, `genericLabOrderReport.sql:19,20` | `org.openmrs.Concept` | `person_attribute_type.format` in the deployed OpenMRS database | CP7. Class confirmed present in the 2.8.9 jar. The column value is owned by the deployed OpenMRS instance |
| `src/main/java/org/bahmni/reports/util/GenericReportsHelper.java:74` | `org.openmrs.Concept` | `person_attribute_type.format`, compared inside generated SQL | CP7 |
| `src/main/java/org/bahmni/reports/util/GenericObservationFormReportTemplateHelper.java:174` | `org.openmrs.customdatatype.datatype.DateDatatype` | `program_attribute_type.datatype`, compared inside generated SQL | CP7 |
| `src/test/resources/datasets/testDataSet.xml:76,77` and `genericLabOrderReportDataSet.xml:103,104` | `org.openmrs.Order`, `org.openmrs.DrugOrder` | `order_type.java_class_name` | CP5 test run |
| `src/test/resources/datasets/testDataSet.xml:105,106,107`, `genericProgramReportDataSet.xml:32,33,35`, `genericObservationFormReportDataSet.xml:28,29`, `observationFormReportDataSet.xml:33,34` | `org.openmrs.customdatatype.datatype.DateDatatype`, `FreeTextDatatype`, `BooleanDatatype`, `RegexValidatedTextDatatype` | `visit_attribute_type.datatype`, `program_attribute_type.datatype` | CP5 test run |
| `src/test/resources/datasets/DrugOrderReportTest-DrugOrders.xml:91,94,97,100,103` | `org.openmrs.SimpleDosingInstructions` | `drug_order.dosing_type` | CP5 test run |
| `src/test/resources/datasets/testDataSet.xml:114,115,116` | `org.openmrs.patient.impl.LuhnIdentifierValidator` | `patient_identifier_type.validator` | CP5 test run |

All nine distinct class names above were confirmed present in `openmrs-api` 2.5.7, 2.6.17, 2.7.9 and 2.8.9 by listing the jar contents, with a negative control on a fabricated class name that correctly reported missing in all four. None was renamed on the path, so the residual risk is the database schema rather than the class names.

## Forced dependency changes

| Dependency | Pinned today | Floor at 2.8.9 | Namespace change | Notes |
| --- | --- | --- | --- | --- |
| `org.testcontainers:mysql` | **absent** | required at `test` scope from OpenMRS **2.6.0** | no | Forced, and the easiest to miss. `org.openmrs.test.BaseContextSensitiveTest` references `org.openmrs.test.Containers`, which needs `org.testcontainers.containers.MySQLContainer`. `BaseIntegrationTest` extends that base class, so this repo is exposed. Verified by reading the constant pool of `BaseContextSensitiveTest.class` in the 2.5.7, 2.6.0, 2.6.17, 2.7.0, 2.7.9 and 2.8.9 test-jars: absent at 2.5.7, present from 2.6.0 onward. Symptom is `NoClassDefFoundError: org/testcontainers/containers/MySQLContainer` at test runtime, not at compile time |
| `org.liquibase:liquibase-core` | 4.8.0, `compile` (`pom.xml:413-417`) | 4.20.0 or newer on the **test classpath**. OpenMRS 2.8.x itself declares 4.32.0 | no | Forced only where OpenMRS code runs. CP3 decides whether the shipped jar moves with it |
| `org.springframework:*` | 5.3.39 | 5.3.30 | no | Repo is already ahead of the floor. No action |
| `org.hibernate:hibernate-entitymanager` | 5.6.0.Final, `runtime` (`pom.xml:381-396`) | 5.6.15.Final | no | Same 5.6.x line, `javax.persistence` throughout. Optional alignment, not forced |
| `org.hibernate:hibernate-search-orm` | not pinned, arrives transitively | 6.2.4.Final at 2.8.9 | no | Not used by this repo. Arrives at `test` scope only |
| `com.fasterxml.jackson.core:*` | 2.17.0 | 2.19.1 | no | Not forced. The repo's pin wins and 2.17.0 is API-compatible for the use here |
| `xerces:xercesImpl` and `xml-apis` | not pinned, arrive transitively from `openmrs-api` at `test` scope | 2.12.2 at 2.8.9, was 2.12.1 | no | Not forced, but these cause the pre-existing red baseline. Excluded at CP1 |
| `javax.servlet:javax.servlet-api` | 4.0.1, `provided` | unchanged | no | No `jakarta` migration is forced by any OpenMRS release through 2.8.9 |

## Test harness viability

| Library / base class | Version | Survives Java 21 | Mitigation |
| --- | --- | --- | --- |
| `org.powermock:powermock-*` | 2.0.7 | no | Remove at CP4, **before** the OpenMRS bump. On JDK 17 and JDK 21 the suite raises 27 `InaccessibleObjectException` from `org.powermock.reflect.internal.WhiteboxImpl.doGetAllMethods`, because `module java.base does not "opens java.lang"`. Identical on 17 and 21, so the wall is at 17, not 21. PowerMock also changes behaviour at 2.8.9 on JDK 11, which is the ordering reason |
| `org.mockito:mockito-core` | 3.5.11 | no | Bump to 5.x with the PowerMock removal at CP4 |
| `maven-surefire-plugin` | 2.18.1 (`pom.xml:79-92`) | no | Bump to 3.x at CP8. On JDK 21 it also miscounts: per-class lines sum to 48 while the summary reports 24 run |
| `maven-compiler-plugin` | 3.1 (`pom.xml:60-70`) | compiles, but predates `release` | Bump to 3.14.0 at CP8 so `<release>` can replace `<source>`/`<target>` |
| `org.openmrs.test.BaseContextSensitiveTest` (JUnit 4) | from `openmrs-api` test-jar | yes at 2.8.9 | none. Confirmed present in the 2.8.9 test-jar. OpenMRS also ships an `org.openmrs.test.jupiter` variant, but migrating to it is not required |
| `junit:junit` | 4.13 | yes | Optional bump to 4.13.2 |
| `org.dbunit:dbunit` | 2.4.7, `compile` | untested above 11 | Evaluate at CP8. Reached only through `BaseIntegrationTest` |

## What the compiler will not catch

| Area | Location | Risk | How it gets verified |
| --- | --- | --- | --- |
| Liquibase jar filename hardcoded under `set -e -x` | `package/resources/run-liquibase.sh:7` | high. Pins `${WAR_DIRECTORY}/WEB-INF/lib/liquibase-core-4.8.0.jar` by exact filename. If the packaged version changes, the path stops resolving and container startup fails at the migration step. No compiler, test or CI job sees this, because CI skips tests and never starts the container | CP6 |
| Liquibase CLI dependencies no longer bundled | `package/resources/run-liquibase.sh:9,14` | high. The script builds a two-entry classpath and invokes `liquibase.integration.commandline.Main`. That class still exists at 4.32.0, but the jar shrank from 3873 to 1344 classes and no longer bundles `opencsv`, which 4.8.0 carried with 166 entries | CP6 |
| Liquibase writes to the OpenMRS database, not only the reports database | `package/docker/bahmni-reports/start.sh:11` | high. This service applies its own changelog against `$OPENMRS_DB_NAME`. A Liquibase major-version change rewrites `liquibasechangelog` checksum rows, and that write is not undone by reverting the code | CP3, CP6 |
| MySQL connector filename hardcoded | `package/resources/run-liquibase.sh:9` | medium. Pins `mysql-connector-java-8.0.28.jar`. Not touched by this upgrade, but it breaks the moment anyone bumps the connector | CP6 |
| Embedded Tomcat predates the target runtime | `package/docker/bahmni-reports/Dockerfile:27` | high, and this is the real JDK 21 blocker. The image fetches `bahmni-embedded-tomcat-8.0.42.jar` from `repo.mybahmni.org`. Inspected: `server.info=Apache Tomcat/8.0.42`, `server.built=Mar 8 2017`, and 3232 of its classes are Java 7 bytecode. Tomcat 8.0.x is end of life. Nothing in the pom governs it | CP9 |
| 38 raw SQL files query the OpenMRS schema directly | `src/main/resources/sql/*.sql` | high. These bypass the OpenMRS API entirely, so schema drift between the deployed 2.5.7 and 2.8.x databases is invisible to both the compiler and the jar bump | CP7 |
| Changelogs declare the Liquibase 2.0 XSD | `src/main/resources/liquibase.xml`, `src/main/resources/liquibase_bahmni_reports.xml` | medium. Both reference `dbchangelog-2.0.xsd`. Liquibase 4.32 still accepts old XSDs, but duplicate-file handling changed, which is why `GlobalConfiguration$DuplicateFileMode` exists | CP6 |
| Hibernate dialect pinned as a string | `src/main/java/org/bahmni/reports/persistence/PersistenceConfiguration.java:28` | low. `org.hibernate.dialect.MySQL5Dialect` is a string and is deprecated in later Hibernate. Hibernate stays on 5.6.x here, so it is out of scope, but it will bite a future Hibernate 6 move | CP4 test run |
| Local config file absent by default | `pom.xml:17` sets `skipConfig=true`, script at `scripts/create_configuration.sh` | high for verification, not for production. 296 of the 327 baseline errors are `FileNotFoundException: ~/.bahmni-reports/bahmni-reports-test.properties` followed by an NPE at `BahmniReportsProperties.getOpenmrsUrl` | CP1 |
| Test database provisioning is fragile and silent | `src/test/resources/create_db.sh` | high for verification. No `set -e`. Lines 16 and 17 issue `DROP DATABASE` with no `IF EXISTS`, so on a fresh server they fail and the script continues. Its exit status comes from line 27 alone, so partial provisioning reports success. It also requires a `mysql` client binary and reads credentials out of the config file, so `create_configuration.sh` must run first | CP1 |
| `useInMemoryDatabase()` returns false | `src/test/java/org/bahmni/reports/report/integrationtests/BaseIntegrationTest.java:136-138` | high for verification. Integration tests need a real MySQL. Surefire excludes only `**/BaseIntegrationTest.java` (`pom.xml:88-90`), so every subclass runs | CP1 |
| Reflection and dynamic mocking | PowerMock `WhiteboxImpl`, Mockito Objenesis | high on 17 and 21, and already changed at 2.8.9 on JDK 11 | CP4 |
