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

Manual dispatch supports optional inputs:

```text
version_name
version_code
require_release_signing
```

The version inputs override the checked-in Gradle version properties for that
workflow run only. `require_release_signing=true` fails early unless all release
signing secrets are configured.

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

Release signing is optional for validation artifacts and required for
release-ready artifacts. The app's Gradle configuration signs the release build
only when all release signing properties are present. Manual workflow runs can
set `require_release_signing=true` to fail early when secrets are missing. The
workflow never commits or prints private signing material.

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

If any signing secret is missing and `require_release_signing` is false, the
workflow logs that signing was skipped and continues with unsigned validation
artifacts. If signed output is required, missing secrets fail the workflow before
Gradle builds artifacts.

## Versioning

Default version values live in `androidApp/gradle.properties`:

```text
POCKET_LEDGER_VERSION_CODE=1
POCKET_LEDGER_VERSION_NAME=1.0.0
```

`versionCode` must increase monotonically for every Play Store upload candidate.
`versionName` should use semantic form such as `1.0.0` or `1.0.0-rc.1`. The
repository-root `gradle.properties` mirrors these values for root Gradle
invocation. See `docs/release/signing-versioning.md` for the full policy.

## Play Store Readiness

The separate internal distribution workflow can send debug APKs to Firebase App
Distribution for early tester feedback. This release candidate workflow remains
the source for release APK/AAB artifacts and Play Store internal-testing
handoff.

This workflow supports `US-E18-01 - Prepare production-ready Play Store release`
by producing repeatable release candidate artifacts, validating the release/R8
path, preserving mapping files, and giving internal testers an AAB candidate for
Play Console upload. It does not replace the remaining Play Store readiness
work: privacy policy, app content declarations, store listing assets,
screenshots, device smoke tests, and the final release checklist in `docs/release/release-checklist.md` still needs
to be completed before production submission.
