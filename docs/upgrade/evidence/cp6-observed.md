# CP6, done -- repair the liquibase startup path

Reference material for `../2.8.9-upgrade-plan.md`.

## Result

`package/resources/run-liquibase.sh` no longer hardcodes jar filenames. `run-liquibase.sh`
was run to completion, exit 0, against two scratch MySQL databases (an OpenMRS-shaped one
seeded with a minimal admin user for `liquibase.xml`, and an empty one for
`liquibase_bahmni_reports.xml`), both from a plain shell invocation and from inside the
actual built Docker image (`docker run ... sh /etc/bahmni-reports/run-liquibase.sh ...`
against the host's MySQL). `liquibasechangelog` ended up with 6 rows and 2 rows
respectively. `docker build` succeeds.

## The jar names, confirmed by unzipping the WAR

`liquibase-core-4.32.0.jar` (per CP3's decision) and `mysql-connector-java-8.0.28.jar`
(unchanged). Both are still guarded by a fail-loud lookup; grep for a hardcoded
`N.N.N.jar` pattern in the script now returns nothing.

## Finding: liquibase-core 4.32.0 no longer shades its runtime dependencies

Replacing the hardcoded two-jar classpath with a two-jar *glob-derived* classpath
compiled fine but failed at runtime: first `NoClassDefFoundError:
org/apache/commons/io/output/WriterOutputStream`, then, after adding `commons-io`,
`NoClassDefFoundError: org/apache/commons/lang3/StringUtils`. `liquibase-core-4.8.0.jar`
was a fat jar (roughly 3873 classes); `4.32.0.jar` is not (roughly 1344), and no longer
bundles `opencsv` either. The old script's two-jar classpath only ever worked because the
old jar shaded everything else in.

**Fix:** rather than accreting a fourth, fifth, sixth named jar to the classpath by
trial and error, the script now adds the rest of `WEB-INF/lib` via Java's own trailing
`/*` classpath wildcard (`${WAR_DIRECTORY}/WEB-INF/lib/*`), alongside the two explicitly
guarded, by-name jars (`liquibase-core-*.jar`, `mysql-connector-java-*.jar`). This is a
directory wildcard the JVM itself expands at launch, not a shell glob -- it must reach
`java -cp` as a literal `/*` rather than being shell-expanded first, which it does here
because POSIX `sh` does not glob-expand the right-hand side of a variable assignment,
and the later unquoted use of `$CLASSPATH` (one colon-joined word, no shell wildcard
characters split across separate words) is left untouched by pathname expansion since
nothing on the real filesystem matches the whole joined string as one pattern. Confirmed
by running: both changelogs succeed, and a jar deliberately renamed to be undiscoverable
in a bare test directory still fails loudly via the two explicit guarded lookups.

Fed back to the skill (`references/openmrs-facts.md`, Bahmni consumer notes section):
the general lesson that fixing a hardcoded-jar-name bug in a Liquibase-bump packaging
script is not just "swap the exact filename for a glob" -- the newer jar's loss of
shading is itself a forced change worth expecting.

## Finding: `Reports-202304011846` (the bootstrap reports-user changeset) needs a
## pre-existing OpenMRS admin user

Unrelated to the jar-classpath fix: `liquibase.xml`'s changeset that creates OpenMRS's
`reports-user` inserts into `person`/`users` with `creator = 1`, which fails with an FK
violation (`user_who_created_person`) against a schema-only scratch database that has no
seed data. This is not new behavior from the Liquibase bump -- it is a property of the
changeset's own SQL, on any Liquibase version -- but it means a truly empty scratch
database is not a valid target for this specific changelog. Verification here seeded a
minimal `person_id=1`/`user_id=1` self-consistent pair (via a temporary
`FOREIGN_KEY_CHECKS=0` window) before running the changelog, matching what a real
OpenMRS install already has via its own bootstrap.

## Unrelated, pre-existing Dockerfile breakage found and fixed

`docker build` failed independently of anything in this checkpoint's scope, at `yum
install -y nc elfutils-libelf-0.176-2.amzn2.0.1 libnghttp2-1.41.0-1.amzn2.0.1`: `No
match for argument`. Confirmed pre-existing by stashing this checkpoint's changes and
rebuilding against the unmodified branch -- identical failure. The Amazon Linux 2023
yum mirror no longer serves those exact, several-years-old RPM builds. Fixed by dropping
the exact-version pins (`elfutils-libelf`, `libnghttp2`, unversioned) since there was no
documented reason for pinning those two exact builds and no compensating pin (digest,
repo snapshot) available to reproduce them instead. Flagged to the user before making
this change since it is outside CP6's stated scope; user chose to fix it inline rather
than defer it.

## Caught by review, and what didn't reproduce

A high-effort review (run per the plan's process, before committing) reported five
findings. Two were acted on:

- **Real, and the one worth generalizing:** hardcoding a short allowlist of jar names
  (the two originals plus `commons-io`/`commons-lang3` added to fix the
  `NoClassDefFoundError`s above) is exactly the same fragility class as the bug this
  checkpoint exists to fix, just moved one level up -- the next Liquibase or dependency
  bump could need a jar nobody thought to add. Fixed by switching to the directory
  wildcard described above, which also let the two ad hoc jar additions be removed
  entirely.
- **Real, minor:** `ls pattern | head -1` silently picks one match instead of failing
  loudly if a glob for one of the two named jars ever matches more than one file (e.g.
  two `liquibase-core-*.jar` versions present during a transitional dependency bump).
  Fixed with a small `find_one_jar()` helper shared by both lookups that fails loudly on
  zero **or more than one** match; verified by deliberately placing two
  `liquibase-core-*.jar` files in a scratch directory and confirming exit 1.

One finding did not reproduce: the review reported a runtime `NoClassDefFoundError` for
`org.yaml.snakeyaml.constructor.BaseConstructor`, then a further `ClassNotFoundException`
for `com.opencsv.exceptions.CsvMalformedLineException`, running the pre-wildcard,
four-named-jar version of the script. Re-running that exact script against a fresh
scratch database, twice, from a freshly built WAR, did not reproduce either failure --
both changelogs completed successfully both times, with the same four jars the review
said were insufficient. Re-verified again after switching to the wildcard-based fix,
including from inside the actual built Docker image against a real MySQL server, with
the same result: no snakeyaml or opencsv error at any point. The wildcard-based fix
would mask this specific pair of errors even if the underlying claim were correct for
some environment or code path this session's testing didn't hit (both jars are present
in `WEB-INF/lib` and now included via the wildcard either way), so it does not need
resolving further to close this checkpoint, but the discrepancy itself is worth a
future session's attention if it resurfaces.

The other two findings (a helper-function suggestion for the jar lookups, made moot by
the wildcard fix removing three of the four lookups; and "document why the Dockerfile's
exact-version yum pins were dropped") are addressed by this file and the Dockerfile's
own history rather than further code changes.
