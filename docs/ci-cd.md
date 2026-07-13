# CI/CD Strategy

Pocket Ledger uses GitHub Actions to keep normal pull-request validation fast
and to keep screenshot and benchmark work controlled. The Android Gradle wrapper
lives in `androidApp/`, and workflow commands run from that directory.


## Workflow Badges And Required Checks

README badges are published for all repository workflows:

```markdown
[![PR Validation](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/pr-validation.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/pr-validation.yml)
[![Screenshot And Benchmark Validation](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/screenshot-benchmark.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/screenshot-benchmark.yml)
[![Release Candidate](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/release-candidate.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/release-candidate.yml)
[![Internal Distribution](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/internal-distribution.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/internal-distribution.yml)
```

Configure GitHub branch protection for `dev` and `main` so pull requests cannot
merge unless the `PR Validation / Android validation` check is successful and
up to date with the target branch. Keep stale review dismissal and linear-history
requirements aligned with the repository policy. The release candidate,
screenshot/benchmark, and internal distribution workflows are intentionally
manual, scheduled, or tag/branch triggered; require them only for PRs whose risk
profile needs that extra gate.

This repository setting is the enforcement layer for the #16 acceptance criterion
"CI failures block unsafe merges". The workflow file defines the check; GitHub
branch protection must require it.

## Pull Request Validation

`.github/workflows/pr-validation.yml` runs for pull requests targeting `dev` and
`main`, the active integration and default branches in this repository. It also
supports manual `workflow_dispatch` runs.

The workflow uses read-only repository permissions, Gradle wrapper validation,
JDK 21, Gradle dependency caching through `gradle/actions/setup-gradle`, and
concurrency cancellation so newer pushes cancel older runs for the same PR.

Required PR checks:

```bash
./gradlew projects
./gradlew lintDebug
./gradlew testDebugUnitTest :shared:allTests
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew :app:assembleBenchmark :macrobenchmark:assemble
```

These checks cover module discovery, Android lint, local JVM tests, shared KMP
tests, debug assembly, release/R8 assembly, and benchmark artifact compilation.
They do not require signing secrets, Play Store credentials, an emulator, or a
physical device.

From the repository root on Windows, use the same tasks through the checked-in
Android wrapper:

```powershell
.\androidApp\gradlew.bat projects
.\androidApp\gradlew.bat lintDebug
.\androidApp\gradlew.bat testDebugUnitTest :shared:allTests
.\androidApp\gradlew.bat :app:assembleDebug
.\androidApp\gradlew.bat :app:assembleRelease
.\androidApp\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble
```

PR validation intentionally does not publish builds, upload to Play Store,
generate signed release artifacts, run connected Android tests, run
Macrobenchmark measurements, or generate Baseline Profiles. Those tasks either
need device infrastructure, release credentials, or manual review. For merge
safety, `PR Validation / Android validation` is the check that branch protection
should require on `dev` and `main`.

## Screenshot Workflow

`.github/workflows/screenshot-benchmark.yml` runs adaptive screenshot validation
with Paparazzi. It is controlled by:

- `workflow_dispatch` with `run_screenshots=true`.
- A weekly Monday scheduled run.

The screenshot job runs:

```bash
./gradlew verifyAdaptiveScreenshots
```

Paparazzi reports and failure diffs are uploaded as workflow artifacts from:

```text
androidApp/app/build/reports/paparazzi/
androidApp/app/build/paparazzi/failures/
```

Screenshot verification is not part of every PR because the suite is broader
and more expensive than normal unit-test feedback. UI PRs should run it locally
or trigger the workflow manually when layout, theme, accessibility font-scale,
or snapshot baselines change. Committed screenshot baselines remain under
`androidApp/app/src/test/snapshots/images`.

## Benchmark Workflow

The same controlled workflow assembles benchmark artifacts with:

```bash
./gradlew :app:assembleBenchmark :macrobenchmark:assemble
```

It runs by manual dispatch with `assemble_benchmarks=true` and on the weekly
schedule. The workflow uploads benchmark APKs and reports from:

```text
androidApp/app/build/outputs/apk/benchmark/
androidApp/macrobenchmark/build/outputs/apk/
androidApp/macrobenchmark/build/reports/
androidApp/macrobenchmark/build/outputs/connected_android_test_additional_output/benchmark/
```

Connected Macrobenchmark execution remains manual because it requires a stable
attached device or emulator and produces results that should be compared only
across the same named device, OS image, app commit, and build variant.

Local connected benchmark commands:

```bash
./gradlew :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest
./gradlew :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mojtaba.pocketledger.macrobenchmark.LargeDatasetBenchmark
./gradlew :app:generateReleaseBaselineProfile
```

Do not add connected benchmarks to default PR validation without dedicated
device-lab capacity and flake management.

## Internal Distribution Workflow

`.github/workflows/internal-distribution.yml` provides a manual and beta-tagged
Firebase App Distribution path for internal testers. It runs on
`workflow_dispatch` and `beta-*` tags only; it does not run for pull requests and
does not publish to Play Store.

The workflow validates and builds the existing debug APK with:

```bash
./gradlew projects
./gradlew lintDebug
./gradlew testDebugUnitTest :shared:allTests
./gradlew :app:assembleDebug
```

The debug APK is uploaded as a GitHub Actions artifact and distributed through
Firebase CLI only after `FIREBASE_APP_ID`, `FIREBASE_SERVICE_ACCOUNT_JSON`, and
`FIREBASE_TESTER_GROUPS` are configured as GitHub Actions secrets. Missing
secrets fail the workflow before Firebase distribution. See
`docs/internal-distribution.md` for triggering, release notes, tester groups,
artifact paths, and current limits.

## Release Candidate Workflow

`.github/workflows/release-candidate.yml` prepares release candidate artifacts
for Play Store internal testing. It runs manually through `workflow_dispatch`
and on pushes to `release/**`, `rc/**`, `v*`, and `rc-*` refs.

The workflow uses the same JDK, Gradle wrapper validation, Gradle setup, working
directory, repository permissions, and worker cap as the other Android
workflows. It runs release lint, JVM/shared tests, release APK assembly, release
app bundle generation, and benchmark artifact assembly.

Uploaded artifacts include the release APK, release AAB, release mapping files,
and validation reports. Artifact names include app version, version code, ref
name, and GitHub run number where practical. See
`docs/release/release-candidate.md` for triggers, signing behavior, artifact
paths, and `docs/release/release-checklist.md` for the release checklist.

## Signing And Versioning

Release signing and versioning are documented in
`docs/release/signing-versioning.md`. The app module is `androidApp/app`, the
production application ID is `com.mojtaba.pocketledger`, and version defaults
come from `POCKET_LEDGER_VERSION_CODE` / `POCKET_LEDGER_VERSION_NAME` in Gradle
properties.

Normal PR validation assembles release/R8 artifacts without signing secrets.
True release-ready signed builds must provide all `POCKET_LEDGER_RELEASE_*`
values and set `POCKET_LEDGER_REQUIRE_RELEASE_SIGNING=true`; `:app:validateReleaseSigning`
then fails clearly if signing is incomplete. Keystores, passwords, aliases,
service-account JSON, Play Store credentials, Firebase credentials, private
tester lists, and private release data must never be committed or uploaded as
workflow artifacts.

## Debug Health Screen

Debug builds expose a `Debug` top-level navigation item that opens the Debug
Health screen. Release and benchmark variants do not register this destination,
and the detailed implementation lives in the debug source set with a release
stub only for compilation.

The screen summarizes safe development and release-readiness state:

- Build: app label, version, application ID, build type, flavor, environment,
  debuggable state, and internal diagnostics state.
- CI/CD and testing: PR validation, lint/unit test coverage, benchmark artifact
  assembly, screenshot workflow, release candidate workflow, and merge-safety
  expectations.
- Firebase/App Distribution: Firebase resource detection, Analytics dependency
  status, Crashlytics availability, and the fact that App Distribution is
  handled by CI rather than app runtime code.
- Observability, database, feature flags, and release safety: sanitized logging
  mode, startup-health placeholder, product event taxonomy/provider state, Room
  schema/migration status, safe feature flag summary, and debug-only route/privacy
  checks.

The screen intentionally does not display service-account JSON, keystore data,
Firebase app IDs or tokens, tester emails or groups, private CI metadata,
credentials, stack traces, transaction amounts, notes, search text, or encrypted
payloads. Unavailable runtime data is shown as `Unavailable in local build`,
`Configured via CI`, `Not configured`, `Debug only`, or `Release hidden` rather
than fake live status.

Use this screen during local debug builds and internal debug distribution to
review CI/CD, observability, accessibility-label, and release-safety impact
before opening or merging release-related changes.

## Release Safety

PR and screenshot/benchmark workflows do not require secrets. Release signing
properties, Play Store upload credentials, crash-reporting tokens, API keys, and
personal financial data must not be printed, uploaded, or embedded in workflow
artifacts.

Release readiness is supported by:

- Release/R8 assembly in PR validation.
- Release APK/AAB generation and mapping upload in the release candidate
  workflow.
- Benchmark build assembly in PR validation and the controlled workflow.
- Firebase App Distribution for manual or beta-tagged internal debug APKs.
- Paparazzi coverage for adaptive UI, theme, and 200% font-scale states.
- Existing accessibility guidance in `androidApp/docs/accessibility.md`.
- Existing privacy-safe logging guidance in `androidApp/docs/logging-policy.md`.

## Before Opening A PR

Run the focused checks that match the change:

```powershell
.\androidApp\gradlew.bat lintDebug
.\androidApp\gradlew.bat testDebugUnitTest :shared:allTests
.\androidApp\gradlew.bat :app:assembleDebug :app:assembleRelease
.\androidApp\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble
```

For UI, accessibility, theme, or font-scale changes:

```powershell
.\androidApp\gradlew.bat verifyAdaptiveScreenshots
```

For performance-sensitive, Baseline Profile, or release-build changes:

```powershell
.\androidApp\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble
.\androidApp\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest
.\androidApp\gradlew.bat :app:generateReleaseBaselineProfile
```

The connected benchmark and Baseline Profile commands require an attached
emulator or device. They should be run on stable, comparable hardware when a
change affects startup, scrolling, release/R8 behavior, or profile coverage.

## Issue Closure Readiness

- #23 is covered by `.github/workflows/pr-validation.yml`, Gradle setup/cache,
  automated lint/test/build verification, release/R8 assembly, benchmark artifact
  assembly, README badges, and this workflow documentation.
- #123 is covered by the PR validation, release candidate, screenshot/benchmark,
  and internal distribution workflows plus the README badges and local commands.
- #16 is implemented from the repository side once GitHub branch protection
  requires `PR Validation / Android validation` before merging to `dev` and
  `main`. Keep #16 open only if that repository setting has not been enabled.
- #17 remains open until Play Store assets, published privacy policy URL, Play
  Console app-content declarations, and final production submission evidence are
  complete.
