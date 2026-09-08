# CP2, done

Reference material for `../2.8.9-upgrade-plan.md`. Shipped together with CP1 in the
same PR: [PR #106](https://github.com/Bahmni/bahmni-reports/pull/106) (branch
`BAH-5073-fix-integration-tests`, ticket **BAH-5073**). CP1's local fix and CP2's CI
wiring were developed together in one session and it made no sense to split them
into separate PRs; see the note in the plan's Branching section.

## What changed in `.github/workflows/validate_pr.yml`

- Installs a `mysql` client (`sudo apt-get install -y mysql-client`) -- not present
  on the runner by default.
- Waits for the `mysql:5.6` service to accept connections (`mysqladmin ping` in a
  retry loop, capped at 30 tries / 60s), with an explicit failure if it never comes
  up, rather than falling through into a confusing downstream connection error.
- Runs `./scripts/create_configuration.sh` and asserts the properties file exists.
- Runs `create_db.sh` and independently asserts both databases have tables via
  `SHOW TABLES ... -N | wc -l`, since `create_db.sh`'s own exit status is unreliable
  (no `set -e`, `DROP DATABASE` without `IF EXISTS`). The `-N` flag matters: without
  it, `mysql`'s header row means `wc -l` is always >= 1 even for an empty result, so
  the very check meant to catch a silent provisioning failure could never fail --
  caught by adversarial review, fixed before merge.
- Removed `-DskipTests` from the build step.
- Added a `concurrency` group (`cancel-in-progress: true`) scoped to the PR number,
  so pushing a fix while iterating cancels whatever CI run was still going for the
  same PR instead of both running to completion. Note this only takes effect for runs
  that start after the concurrency block exists in the workflow file on that ref; a
  run already in flight when this was first added had to be cancelled manually once
  via `gh run cancel`.

## Result

First real run of this suite in CI, ever. 341 tests collected, matching the local
CP1 count exactly. First run: 0 errors, 57 failures (the two report-logic/test-literal
bugs described in CP1's evidence, not yet fixed at that point) in ~7.5 minutes. Second
run, after those two fixes: 341/0/0/4, in ~7.5-12 minutes depending on run. All 5
classes that use PowerMock passed cleanly on CI's `corretto` JDK 11 -- the
`NoClassDefFoundError`/`IllegalAccessError` seen when running the same classes locally
is specific to this dev machine's Microsoft OpenJDK distribution, not a real blocker.
CP4 (removing PowerMock) is therefore not gating anything here; it stays worth doing
for its own sake and for the JDK 17/21 move at CP9, tracked separately under BAH-5060.
