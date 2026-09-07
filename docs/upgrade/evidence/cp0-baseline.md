# CP0. Baseline, as measured

Reference material for `../2.8.9-upgrade-plan.md`. CP0 is complete; this is what it found.

### CP0. Baseline

Complete. Recorded here so later failures are attributable.

```bash
git -C /Users/vishalkarmalkar/IdeaProjects/bahmni/bahmni-reports fetch --quiet
git -C /Users/vishalkarmalkar/IdeaProjects/bahmni/bahmni-reports rev-list --left-right --count '@{u}...HEAD'
git -C /Users/vishalkarmalkar/IdeaProjects/bahmni/bahmni-reports rev-parse HEAD
export JAVA_HOME=/Users/vishalkarmalkar/Library/Java/JavaVirtualMachines/ms-11.0.28/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/vishalkarmalkar/IdeaProjects/bahmni/bahmni-reports && ./mvnw clean test 2>&1 | tee /tmp/baseline-bahmni-reports.log | grep -E "Tests run:|BUILD"
```

Exit criteria: build result, test counts and JDK recorded in the state block, commit SHA recorded. Met.

Observed: `behind=0 ahead=0`, HEAD `5b7f95bdafaaffd3bf9758304586a9619d5721eb`. On JDK 11.0.28: `Tests run: 341, Failures: 2, Errors: 327, Skipped: 4`, BUILD FAILURE, 15.8s.

Error breakdown, summing to 327: 296 config-file errors (each a paired `FileNotFoundException` and `NullPointerException`), 26 `NoClassDefFoundError`, 4 `IllegalAccessError`, 1 `java.lang.Exception`.

The 2 failures are both in `PatientAttributesHelperTest`, and both are `org.junit.ComparisonFailure` on a trailing newline: `expected:<...GROUP BY person_id[\n]> but was:<...GROUP BY person_id[]>`. The expected literals at `PatientAttributesHelperTest.java:15,22` end with `\n`; `PatientAttributesHelper.getSql()` no longer emits it. Pre-existing, unrelated to OpenMRS, and long-lived because CI never ran the tests.

The local default JDK is 26.0.2.1, far ahead of CI. Every command in this plan sets `JAVA_HOME` explicitly. Do not rely on the shell default.

Rollback: none required.

---
