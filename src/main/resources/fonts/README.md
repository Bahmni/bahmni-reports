# Bundled PDF font

`GoNotoCurrent-Regular.ttf` is the unmodified "Go Noto Current" v7.0 font (Noto Sans and ~80 other
Noto scripts merged into one file), used only as the embedded font for PDF export (see
`../dynamicreports-defaults.xml`).

It is not committed to the repo: the `download-maven-plugin` execution in `pom.xml` fetches it
from the upstream GitHub release into `target/classes/fonts/` during `generate-resources`,
verifying it against a pinned sha256 so the build fails loudly if the asset ever changes instead
of silently shipping a different font. Run `mvn generate-resources` (or any later phase) once to
populate it locally; IDEs that don't run Maven's lifecycle may need that triggered manually before
the resource is visible on the classpath.

- Source: https://github.com/satbyy/go-noto-universal/releases/tag/v7.0
- Licence: see `LICENSE-GoNotoCurrent.txt` (SIL Open Font License 1.1 — the licence every
  constituent Noto font ships under; the merge tooling itself is public domain / Unlicense, but
  the generated font is OFL per upstream Noto's terms)
- Scripts covered: Latin, Greek, Cyrillic, Armenian, Georgian, Hebrew, Arabic, Devanagari and the
  rest of South/Southeast Asia, CJK (via the Unihan IICore subset), and most other
  currently-in-use scripts, plus common symbols and math notation. **Does not cover Hangul
  (Korean) or emoji** — deployments that export PDFs with Korean content will see missing
  glyphs; see `GoNotoKurrent-Regular.ttf` from the same release if Korean support is needed
  later, it is a superset but 15.5 MB instead of 14.7 MB.
- JasperReports/OpenPDF subsets and embeds only the glyphs a given PDF actually uses, so the
  14.7 MB source file does not inflate every generated PDF — a report exercising 8 scripts in the
  BAH-5213 verification probe produced a ~103 KB PDF.
