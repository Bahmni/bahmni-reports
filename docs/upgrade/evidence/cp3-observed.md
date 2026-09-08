# CP3, done -- Option 1 chosen

Reference material for `../2.8.9-upgrade-plan.md`. No pom or code changed by this
checkpoint; the evidence below is what the decision was based on.

## Environment note: BAH-5058 caught up without waiting on PR #106's merge

PR #106 (CP1/CP2) was still open, blocked on tech-lead review with no fixed date.
Rather than stall, `origin/BAH-5073-fix-integration-tests` was merged directly into
`BAH-5058` locally (merge commit `bdf0a99`), not via master. That branch's own CI had
already proven the green baseline (341/0/0/4), so this gets `BAH-5058` the same
verified state without needing the master merge first. Re-ran the full suite locally
after the merge to confirm: **341 tests, 0 failures, 0 errors, 4 skipped**, ~4:42 min.
Matches CP1/CP2 exactly. Nothing was pushed; `origin/master` and `origin/BAH-5058` are
untouched. When #106 eventually merges to master, `BAH-5058`'s history already
contains those identical commits, so there is nothing to reconcile.

## The scratch-database test, full scale

First pass used a bare schema with no seed data and only got through 2 of the 8 real
changesets before a foreign-key error (`person.creator` needing a `users` row that
didn't exist). Rather than accept that partial result, seeded a scratch database with
OpenMRS's own bundled fixtures -- `org/openmrs/include/initialInMemoryTestDataSet.xml`
and `standardTestDataset.xml`, both already present locally inside
`openmrs-api-2.5.7-tests.jar` in the Maven cache, no internet needed. Converted the
flat DBUnit XML to plain SQL `INSERT IGNORE` statements (a ~15-line Python script) and
loaded them with the `mysql` client directly, sidestepping an unrelated
DBUnit/mysql-connector-java-8.0.x/MySQL-5.6 metadata-scanning bug
(`AmbiguousTableNameException`) that had nothing to do with Liquibase. 410 of 420 rows
loaded; the 10 that failed referenced two columns (`concept_datatype.allow_decimal`,
`orders.status`) that don't exist in this project's 2.5.7-era schema fixture, and were
skipped. Result: 12 persons, 4 users, 36 concepts, 6 patients, 4 encounters, enough
for every real changeset in both changelogs to run to completion.

Used liquibase-core 4.8.0 and 4.32.0, both already cached locally. Built 4.32.0's full
transitive classpath via a throwaway scratch pom (`dependency:build-classpath`), since
4.32.0's jar alone throws `NoClassDefFoundError` on `commons-io` -- it no longer
bundles `opencsv`, `commons-collections4`, `commons-text`, or `commons-io`, confirming
the plan's note about the jar shrinking (3873 -> 1344 classes).

1. Applied both changelogs with **4.8.0** against the seeded database: all 6
   changesets in `liquibase.xml` and both in `liquibase_bahmni_reports.xml` ran clean
   (8 total), checksums stored as `8:<md5>`.
2. Applied both with **4.32.0**: no new changesets pending, but it rewrote all 8
   existing rows' checksums from `8:<md5>` to `9:<newer-algorithm>`. Exit code 0.
3. Ran **4.8.0 again** against the `9:`-rewritten rows: 3 of the 6 changesets in
   `liquibase.xml` actually re-executed (`Running Changeset:` in the log), not merely
   validated. Those 3 are exactly the ones marked `runOnChange="true"` in the XML
   (`Reports-022420151643`, `Reports-030320150913`, `Reports-030320150914`) -- all
   three are `CREATE OR REPLACE VIEW ... replaceIfExists="true"`, so re-execution is a
   no-op recreation of the same view, not a data mutation. The other 5 (not marked
   `runOnChange`) were treated as already-applied with no error, despite the checksum
   mismatch. Exit code 0 both times.
4. Ran **4.32.0 a second time** afterward: it rewrote all 8 checksums back to `9:`
   format again, with the same "Upgrading checksum" messages as the first run. Fully
   repeatable, not a one-time transition.

**Conclusion:** the checksum-format bump is not a one-way ratchet for this project's
actual changelog content. Each version's `update` call rewrites the stored checksum
to match its own algorithm for whatever it touches, and both versions ran successfully
against the other's rewritten state, repeatedly, with zero errors. The only real
effect of switching between versions is that Liquibase re-executes changesets marked
`runOnChange="true"`, and every changeset in this changelog that carries that flag is
an idempotent view definition. Nothing here is destructive, and nothing here is
irreversible in the way "point of no return" implies for a pure version-bump alone.

**Caveat, stated plainly:** this is still a scratch test on synthetic OpenMRS demo
data, not a production database dump (none was available; see the "how to get one"
discussion in-session -- OpenMRS's own bundled fixtures were used instead of an
internet download). It covers every changeset that exists in both changelog files
today, which is the full current scope, but it cannot rule out an unknown quirk in a
much larger, real production `liquibasechangelog` history if one exists beyond what
these two files define. `--validate` and `rollback-count-sql` were not exercised
further: `--validate` failed on an unrelated path-resolution quirk, and
`rollback-count-sql` failed on a CLI syntax difference between versions. Neither was
needed to answer the checkpoint's actual question.

## Decision

**Option 1: bump both the test-classpath and shipped Liquibase to 4.32.0.** Chosen by
the user after reviewing this evidence, given the tested behavior above is
successful and repeatable in both directions for every changeset this project
currently ships. No separate test-scope split (Option 2) or dual-version workaround
(Option 3) is needed.

See the plan's "Point of no return" section for how this changes CP6's status.

## Fed back to the skill

Reviewed `openmrs-upgrade-feasibility` (the skill that produced this plan) for what CP1
through CP3 found that it did not yet anticipate, per the plan's step 6. Added: the
compile-scope Liquibase coupling as its own decision pattern, the tested
checksum-rewrite finding above, the scratch-seeding-via-bundled-fixtures technique,
and the second `AmbiguousTableNameException` variant (DBUnit's `RefreshOperation`
against `mysql:5.6` plus `mysql-connector-java` 8.0.x, distinct from CP1's
`deleteAllData()` one, which the skill already lacked). CP1's other findings (schema
fixture drift, the `xerces`/`xml-apis` exclusion, `commons-compress` pinning) are
either too repo-specific to generalize or already covered in
`references/openmrs-facts.md`. CP2's findings are this repo's own CI plumbing, not
upgrade-feasibility analysis, so nothing from it was fed back.

