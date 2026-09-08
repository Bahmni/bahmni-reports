# CP4, done -- PowerMock removed

Reference material for `../2.8.9-upgrade-plan.md`. Nothing under `src/main/java`
changed. The diff is `pom.xml` plus six test files.

## Result

Full suite on JDK 11.0.28 (Microsoft OpenJDK), MySQL 5.6 in Docker:
**341 tests, 0 failures, 0 errors, 4 skipped**, BUILD SUCCESS, 4:52 min. Identical to
the CP1/CP2 baseline on every number. Run twice: once on the first conversion, and
again after the review fixes below.

Exit criteria, each verified by running the command:

| Criterion | Result |
| --- | --- |
| `grep -rl "powermock\|PowerMock" src/test/java pom.xml` | prints nothing, exit 1 |
| `grep -rl "PrepareForTest\|Whitebox" src/test/java` | prints nothing, exit 1 |
| `grep -c '<argLine>' pom.xml` | 1 |
| Failures/errors on JDK 11 | 0 / 0 |
| Test count vs CP1 | 341, equal |
| Test method names vs `HEAD` | identical in all six changed files. The only added method is `tearDown`, an `@After` in `TSIntegrationDiagnosisLineReportTest` |

## The byte-buddy ceiling on the Mockito version

The plan said "bump mockito-core to 5.x" and left the version open. It is not open:
**jackson-databind 2.17.0 declares `net.bytebuddy:byte-buddy` 1.14.9 at compile
scope**, and that node wins Maven's nearest-wins resolution over whatever byte-buddy
mockito-core asks for. `byte-buddy-agent`, which only Mockito pulls in, is *not*
capped the same way. So a recent Mockito produces a split pair:

```
mockito-core 5.18.0 -> byte-buddy 1.17.5 (omitted for conflict with 1.14.9)
                    -> byte-buddy-agent 1.17.5   <- actually used
```

Mockito's inline mock maker running against a byte-buddy three minor versions older
than the agent it was built against is not a configuration anyone tests. Mapped the
Mockito 5 line to its byte-buddy dependency by reading the published poms:

| mockito-core | byte-buddy |
| --- | --- |
| 5.5.0 | 1.14.6 |
| 5.6.0 | 1.14.8 |
| **5.7.0** | **1.14.9** |
| 5.8.0 | 1.14.10 |
| 5.11.0 | 1.14.12 |
| 5.14.2 | 1.15.4 |
| 5.18.0 | 1.17.5 |

Settled on **5.7.0**, the newest Mockito 5 whose byte-buddy is exactly the 1.14.9
already on the classpath. The resolved tree then reads `omitted for duplicate` rather
than `omitted for conflict`, and `byte-buddy-agent` lands on 1.14.9 too, so the pair
is consistent. The alternative, declaring byte-buddy 1.17.5 directly, was rejected:
byte-buddy is a compile-scope dependency packaged into the WAR, so that would change
the shipped artifact during a test-harness checkpoint and hand CP5 a second variable.

Left a comment on the `mockitoVersion` property recording the cap, because the next
person to bump Mockito will hit it again and the failure mode is obscure. Note for
CP9: byte-buddy 1.14.9 was released after Java 21 GA and supports it, so this cap is
not a Java 21 blocker. If a later Mockito ever is needed, the way through is bumping
jackson-databind (which raises byte-buddy with it), not pinning byte-buddy by hand.

## Mockito 4 removed an API a sixth file was using

The plan named five PowerMock files. Bumping mockito-core past 4.0 also breaks
`org.mockito.Matchers`, deleted in Mockito 4. That class was imported by
`BaseIntegrationTest`, which uses no PowerMock at all and so did not appear in the
plan's grep. Fixed by switching the two static imports to `org.mockito.ArgumentMatchers`.

`MockitoAnnotations.initMocks` is deprecated but still present in 5.7.0, so
`BahmniReportsConfigurationTest` and `ConceptUtilTest` needed no change.

## Strict stubbing found 22 dead stubs, all provably dead

PowerMockRunner does no stubbing-usage analysis. `MockitoJUnitRunner` (which is
`MockitoJUnitRunner.Strict`) reports unused stubbings as a class-level
`UnnecessaryStubbingException`. The first conversion run failed on exactly that, in
both TS tests, with 7 and 20 entries. Every one was dead for a reason the code proves:

- `when(mockTsProperties.getProperty("ts.defaultPageSize"))`, 14 occurrences across
  the two classes. The production read is
  `tsProperties.getProperty("terminologyServer.defaultPageSize")` at
  `TSIntegrationDiagnosisService.java:85`. Wrong key, so the stub never matched and
  `getProperty` was already returning `null` before this checkpoint.
- `when(mockJasperReport.setDataSource(anyString(), any()))`, 6 occurrences in
  `TSIntegrationDiagnosisLineReportTest`. The line template calls the
  `setDataSource(ResultSet)` overload, which the one test that needs it stubs
  separately and which strict stubbing did not flag.
- `when(mockJasperReport.subtotalsAtSummary(any()))`, 7 occurrences in the same class.
  The line template never calls it.

Deleted all 22 rather than dropping to `MockitoJUnitRunner.Silent`. Removing a stub
that Mockito itself reports as never matched cannot change behaviour, and keeping
strict stubbing is worth more than a smaller diff: it is the thing that will notice if
the 2.8.9 bump at CP5 silently stops calling something.

## How each test was converted

| Test | Was | Now |
| --- | --- | --- |
| `TSIntegrationDiagnosisCountReportTest` | `@RunWith(PowerMockRunner)`, `@PowerMockIgnore`, `initMocks` in `@Before` | `@RunWith(MockitoJUnitRunner)`, `initMocks` dropped (the runner does it) |
| `ReportAuthorizationTest` | `mockStatic(Reports.class)` in three tests | `Mockito.mockStatic` in try-with-resources around the `hasPrivilege` call only. The constructor does not touch `Reports`, so the scope stays narrow |
| `TSIntegrationDiagnosisLineReportTest` | `PowerMockito.mockStatic(SqlUtil.class)` in `@Before`, `verifyStatic` | `MockedStatic<SqlUtil>` field opened in `@Before`, closed in a new `@After`. `verifyStatic(SqlUtil.class, times(1)); SqlUtil.foo(...)` became `sqlUtilMock.verify(() -> SqlUtil.foo(...), times(1))` |
| `CleanReportsJobTest` | `whenNew(File.class)`, `whenNew(Date.class)` | real files in a `TemporaryFolder` rule, and an `ArgumentCaptor<Date>` |
| `ReportsSchedulerTest` | `mockStatic(JobBuilder)`, `mockStatic(TriggerBuilder)`, `whenNew(File.class)` | two nested `MockedStatic` scopes in one try-with-resources, plus a `TemporaryFolder` file |

### Why no `mockConstruction`, and why no production seam

The plan offered `mockConstruction` or a clock/filesystem seam for the two
`whenNew` cases. Both were rejected in favour of real files and a captured argument.

`mockConstruction(File.class)` and `mockConstruction(Date.class)` are thread-local and
intercept **every** construction on the test thread while open, including the ones
log4j2, Mockito and the JDK make. Mocking `new File(...)` and `new Date()` process-wide
for the duration of a test is a much larger blast radius than the assertion needs, and
a failure from it would surface as something unrelated later in the fork.

Real files are also a stronger assertion. `verify(mockFile, times(1)).delete()` proves
`delete()` was called on a mock; `assertFalse(reportFile.exists())` proves the file is
gone. That holds for the two delete-the-file tests.

The two **null-filename** tests are a different story, and the first attempt got them
wrong. Both originally asserted `verify(mockFile, never()).delete()` against a `File`
mock the production path never touches, which is vacuous. The first conversion replaced
that with a sentinel file in the directory plus `assertTrue(sentinel.exists())`, which
is *also* vacuous: with a null filename the job never reads `getReportsSaveDirectory()`,
so no code path could have reached the directory at all. The review caught it. Both
sentinels are gone; what carries these two tests is
`verify(bahmniReportsProperties, never()).getReportsSaveDirectory()`, since that call is
the only route to a `File`. That verify is genuinely new in `CleanReportsJobTest`, which
previously stubbed the directory instead of asserting it was never asked for. In
`ReportsSchedulerTest` it was already there, so the net change to that test is the
removal of a vacuous line and nothing else.

A seam on `CleanReportsJob` would have worked, but CP4 is a test-harness checkpoint and
keeping `src/main/java` untouched means any CP5 result is attributable to the OpenMRS
jar and nothing else. The plan's own advice on `OpenMRSAuthenticator` ("do not add a
seam until you have seen the failure persist without PowerMock") applies here for the
same reason.

### Replacing the mocked clock

`shouldReturnProperCleupDate` previously did `whenNew(Date.class).withNoArguments()
.thenReturn(currentDate)` and asserted an exact date. It now captures the argument to
`findByRequestDateTime` and bounds it by the same `Calendar.add(DATE, -10)` arithmetic
applied to `new Date()` immediately before and immediately after the call. That pins
the ten days exactly, is not tolerance-based, and is DST-safe because the expected
value is computed the same way production computes it.

Also dropped the test's stub of `findByRequestDateTime` there: it never had one, and it
does not need one, because Mockito's default answer returns an empty `List` rather than
`null` for a collection return type. Adding one would now fail strict stubbing.

### `OpenMRSAuthenticator` mocked fine

The plan flagged a possible `MockitoException: Cannot mock this class` on
`OpenMRSAuthenticator` and said to confirm it was PowerMock's classloader before adding
a seam. It was. `ReportAuthorizationTest` mocks it with a bare `@Mock` under
`MockitoJUnitRunner` and all five tests pass. No production change needed.

### One assertion added

`ReportsSchedulerTest.shouldCreateCorrectTriggerForScheduling` had no assertion at all:
it stubbed the trigger chain and ran, so it only checked that `schedule` did not throw.
Added `verify(scheduler, times(1)).scheduleJob(jobDetail, trigger)`, which is what the
test name claims. Not required by the checkpoint, but the test now fails if the trigger
stops reaching the scheduler.

## The adversarial review, and what it changed

Requested at high effort on the pom plus the six test files. It ran **15.5 minutes** and
returned seven findings. Two were defects in this checkpoint's own diff, one hardened
the pom, one corrected a claim made in an earlier draft of this file, and three were
real but out of scope. On the process question: high effort was the wrong tier to ask
for by the stated rule (the diff is confined to `src/test/java` with the test count and
method names both pinned), but it was not wasted, because it found a defect class the
count and name checks cannot see. Both facts are worth keeping.

Applied to this checkpoint:

| Finding | Fix |
| --- | --- |
| `TSIntegrationDiagnosisLineReportTest` closed the static mock unguarded, so a throwing `mockStatic` would make all 8 tests report an NPE from `tearDown` and hide the real cause. The reviewer observed exactly that on a JDK 26 run | `if (sqlUtilMock != null)` guard |
| The two sentinel assertions in the null-filename tests were vacuous (see above) | Both removed; the `never()` verify carries those tests |
| The Mockito-to-byte-buddy coupling was documented only in a comment | `dependencyManagement` pins `byte-buddy` and `byte-buddy-agent` to a new `byteBuddyVersion` property, 1.14.9. Resolution is unchanged (`version managed from 1.14.9`), so the WAR is byte-identical, but a future jackson bump can no longer move byte-buddy out from under the agent silently |
| `shouldProcessTerminologyDescendantsWithPagination` ran at the hardcoded fallback of 20 and asserted nothing about page size | Stubs the **correct** key and asserts the arithmetic (see below) |

One correction back to the reviewer: it also claimed the unguarded `close()` could leak
the static mock into the next test class when `@Before` throws after the assignment.
JUnit 4 runs `@After` even when `@Before` throws, so that path closes normally. Only the
null case was real. It also described a `dependencyManagement` pin as making the
coupling "fail loudly at resolution time"; a pin silently forces a version. What it
actually buys is determinism. Failing loudly would need `maven-enforcer` with
`requireUpperBoundDeps`, which was not added here.

### The page-size test, and proving it is not vacuous

`descendantCodes.json` has `total: 2` and two codes, and the mocked `HttpClient` returns
it for every page, so the loop in `TSIntegrationDiagnosisService.loadTempTable` runs
`ceil(total / pageSize)` times. Page size 1 gives two passes; any value at or above 2
gives one pass and is therefore indistinguishable from the fallback of 20. So the test
stubs `terminologyServer.defaultPageSize` to `"1"` and asserts 4 `setString`, 4
`addBatch`, 2 `executeBatch`, plus a direct
`verify(mockTsProperties).getProperty("terminologyServer.defaultPageSize")`.

Given that this checkpoint had already shipped one vacuous assertion, the new one was
mutation-tested rather than trusted: production was temporarily changed back to read the
wrong key `ts.defaultPageSize`, and the test failed with `Argument(s) are different!`.
Production was restored and `git diff src/main/java` confirmed empty. So the assertion
would have caught the original bug.

One residual fragility, not fixed: `getDefaultPageSize` reads the environment variable
`REPORTS_TS_PAGE_SIZE` before the property, so setting it in a shell or CI runner would
bypass the stub and break this test. It is unset locally and in CI today.

## Out of scope, raised elsewhere

None of these belong to CP4, and none were fixed here.

- **`MainReportController.java:62`, pre-existing on `master`.** `response.sendError(SC_BAD_REQUEST, ...)` on a privilege failure is not followed by `return`, so execution falls through to `applyHttpHeaders` and `reportGenerator.invoke()`. Confirmed by reading the code. Whether report bytes reach an unprivileged caller is container-dependent, because `sendError` commits the response and a later `getOutputStream()` write may throw `IllegalStateException`, which `catchBlock` would swallow. Raised as its own bug; it needs a test to settle the actual outcome.
- **`ReportAuthorization.java:39`, from CP1 (PR #106).** The new `cookies != null` guard does not prevent the NPE it appears aimed at: `OpenMRSAuthenticator.callOpenMRS` returns `new ResponseEntity<>(exception.getStatusCode())` with a **null body** on `HttpClientErrorException`, so `privileges.forEach` at line 33 still throws. Confirmed by reading both files. Also a behaviour flip: if OpenMRS answers 200 for an empty session, `hasPrivilege` returns `true` for any report with a null `requiredPrivilege`, where the NPE previously failed closed. Currently unreachable over HTTP because `AuthenticationFilter.preHandle` redirects first. Raised on PR #106, which is still open.
- **`.github/workflows/validate_pr.yml:42`, from CP2 (PR #106).** `sh create_db.sh || echo "..."` discards the exit code and the follow-up guard only requires `SHOW TABLES` to return more than zero rows, so a partial schema import satisfies both and the build proceeds half-provisioned. Raised on PR #106.
- **`BaseIntegrationTest` connection proxy, from CP1.** The proxy declares only `java.sql.Connection`, routes `equals` to `realConnection.equals(proxy)` (always false), and mints a fresh proxy per `getConnection()` call. Reasoned, not observed; the suite passes. Recorded as a known unknown in the plan, because CP7 is the checkpoint that will exercise that proxy hardest.

## Suite runtime, measured

Not a CP4 finding, but recorded here because it came up. Of the 5:36 wall clock, 326s
is inside test classes, and seven database-backed classes account for ~300s of that
across 290 tests, so roughly 1.0s per test.

| Class | Tests | Seconds |
| --- | --- | --- |
| `GenericObservationReportTest` | 71 | 74.5 |
| `GenericObservationFormReportTest` | 49 | 61.0 |
| `GenericLabOrderReportTest` | 47 | 48.3 |
| `ObservationFormReportTest` | 45 | 46.8 |
| `GenericProgramReportTest` | 33 | 32.4 |
| `GenericVisitReportTest` | 30 | 22.7 |
| `AggregationReportTest` | 15 | 14.9 |

The cost is per test **method**, not per class:
`BaseIntegrationTest.beforeBaseIntegrationTest` is an `@Before`, and it calls
`setUpTestData()`, which runs `deleteAllData()` then `executeDataSet` twice then
`getConnection().commit()`. `useInMemoryDatabase()` returns `false`, so that is a real
MySQL round trip, and each test then renders a real Jasper report through `MockMvc`.

Nothing about it was changed here. Options, for CP8 where the surefire bump lives:

- Surefire 2.18.1 has no usable parallelism. Surefire 3.x supports `forkCount=2C` with
  `reuseForks=true`, but not as-is: every class shares one schema and `deleteAllData()`
  truncates it, so concurrent forks would race. It needs a per-fork database, e.g.
  `${surefire.forkNumber}` in the JDBC URL.
- Moving dataset load from `@Before` to `@BeforeClass`, or leaning on the existing
  `@Transactional` rollback instead of delete-and-reload, is the larger win and the
  larger risk, because it introduces order dependence between tests in a class.
- Switching `useInMemoryDatabase()` to `true` is not an option. CP7 depends on these
  tests meeting a real MySQL schema.

While iterating, `-Dtest=` is the practical lever: the five CP4 classes run in about
9s against 5:36 for the suite.
