# Release Candidate Workflow

Pocket Ledger uses `.github/workflows/release-candidate.yml` to prepare and
validate Android release candidates before Play Store internal testing.

## Triggers

Run the workflow from GitHub Actions with `workflow_dispatch`, or push a release
candidate branch or tag:

```text
release/**
rc/**
v*
rc-*
```

Use `release/**` or `rc/**` branches for candidate stabilization work. Use
`v*` or `rc-*` tags when a specific commit is ready to archive as a candidate.

## Validation

The workflow follows the same environment conventions as the PR and screenshot
workflows:

- Ubuntu GitHub-hosted runner.
- Temurin JDK 21.
- Gradle wrapper validation with `gradle/actions/wrapper-validation@v4`.
- Gradle setup and caching with `gradle/actions/setup-gradle@v4`.
- Gradle commands run from `androidApp/`.
- `GRADLE_OPTS=-Dorg.gradle.workers.max=2`.
- Read-only repository permissions.

Release candidate validation runs:

```bash
./gradlew projects
./gradlew lintRelease
./gradlew testDebugUnitTest :shared:allTests
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
./gradlew :app:assembleBenchmark :macrobenchmark:assemble
```

These checks validate module discovery, release lint, local JVM and shared KMP
tests, release/R8 APK generation, Play Store app bundle generation, and
benchmark artifact compilation. Connected instrumentation tests, Macrobenchmark
measurements, Baseline Profile generation, and Play Store upload are still
manual because they require device infrastructure or publishing credentials.

## Artifacts

Each run uploads artifacts named with the app version, version code, sanitized
ref name, and GitHub run number:

- `pocket-ledger-rc-apk-*`: release APK from
  `androidApp/app/build/outputs/apk/release/`.
- `pocket-ledger-rc-aab-*`: release app bundle from
  `androidApp/app/build/outputs/bundle/release/`.
- `pocket-ledger-rc-mapping-*`: R8 mapping outputs from
  `androidApp/app/build/outputs/mapping/release/`.
- `pocket-ledger-rc-reports-*`: release lint and JVM test reports.

The AAB is the candidate artifact for Play Store internal testing. The APK is
kept for local smoke testing and review. Mapping files are uploaded separately
so they can be retained with the candidate build record without mixing them
into installer artifacts.

## Signing

Release signing is optional in CI. The app's Gradle configuration signs the
release build only when all release signing properties are present. The workflow
therefore produces unsigned CI-safe artifacts by default and never commits or
prints private signing material.

To enable signed release candidate artifacts, configure all of these GitHub
Actions secrets:

```text
POCKET_LEDGER_RELEASE_STORE_BASE64
POCKET_LEDGER_RELEASE_STORE_PASSWORD
POCKET_LEDGER_RELEASE_KEY_ALIAS
POCKET_LEDGER_RELEASE_KEY_PASSWORD
```

`POCKET_LEDGER_RELEASE_STORE_BASE64` must contain the base64-encoded keystore
file. The workflow decodes it into the runner's temporary directory and exposes
the existing Gradle properties through `ORG_GRADLE_PROJECT_*` environment
variables:

```text
POCKET_LEDGER_RELEASE_STORE_FILE
POCKET_LEDGER_RELEASE_STORE_PASSWORD
POCKET_LEDGER_RELEASE_KEY_ALIAS
POCKET_LEDGER_RELEASE_KEY_PASSWORD
```

If any signing secret is missing, the workflow logs that signing was skipped and
continues with unsigned release candidate artifacts.

## Play Store Readiness

This workflow supports `US-E18-01 - Prepare production-ready Play Store release`
by producing repeatable release candidate artifacts, validating the release/R8
path, preserving mapping files, and giving internal testers an AAB candidate for
Play Console upload. It does not replace the remaining Play Store readiness
work: privacy policy, app content declarations, store listing assets,
screenshots, device smoke tests, and the final release checklist still need to
be completed before production submission.
