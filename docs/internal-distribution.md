# Internal Distribution

Folentra uses `.github/workflows/internal-distribution.yml` for internal
tester distribution through Firebase App Distribution. This workflow is separate
from PR validation and the release candidate workflow so internal testing cannot
publish to production or upload to Play Store by accident.

## Triggers

Run the workflow manually from GitHub Actions with `workflow_dispatch`, or push a
beta tag:

```text
beta-*
```

Do not trigger internal distribution from pull requests or untrusted forks. The
workflow uses read-only repository permissions and concurrency keyed by ref so
multiple distributions for the same ref do not overlap.

## Distributed Build

The workflow distributes the existing `debug` APK:

```bash
./gradlew :app:assembleDebug
```

This is intentional for the current project state. Folentra does not yet
have a dedicated `internal` or `beta` build type. Release signing is
secret-backed and reserved for release-candidate APK/AAB artifacts. The debug APK is installable through the normal debug signing
configuration and uses the `.debug` application ID suffix. It is appropriate for
internal tester feedback, not staged Play Store rollout or production testing.
Version metadata for artifact names comes from the non-secret
`FOLENTRA_VERSION_CODE` and `FOLENTRA_VERSION_NAME` Gradle properties.
The workflow validates the version name and version code before building so
misconfigured internal artifacts fail early instead of reaching testers.

A future task can add a dedicated `internal` build type when the project is
ready to define internal signing, logging, app ID, and diagnostics behavior
separately from `debug` and `release`.

## Validation Gates

Before distribution, the workflow runs the same lightweight checks used by the
project's CI style:

```bash
./gradlew projects
./gradlew lintDebug
./gradlew testDebugUnitTest :shared:allTests
./gradlew :app:assembleDebug
```

If any validation step fails, the APK is not distributed. Connected Android
tests, Paparazzi screenshots, Macrobenchmark measurements, Baseline Profile
generation, release/R8 assembly, and Play Store upload remain outside this
workflow. Use the PR validation, screenshot/benchmark, and release candidate
workflows for those paths.

## Required GitHub Secrets

Configure these repository or environment secrets before running distribution:

```text
FIREBASE_APP_ID
FIREBASE_SERVICE_ACCOUNT_JSON
FIREBASE_TESTER_GROUPS
```

`FIREBASE_APP_ID` is the Firebase Android app ID for the debug/internal app
registered in Firebase. `FIREBASE_SERVICE_ACCOUNT_JSON` is the full service
account JSON value with permission to upload Firebase App Distribution releases.
`FIREBASE_TESTER_GROUPS` is a comma-separated list of Firebase tester group
aliases, not raw tester email addresses.

The workflow writes the service account JSON only to the runner temporary
directory, locks the file to the current runner user, exposes it through
`GOOGLE_APPLICATION_CREDENTIALS`, and deletes it in an `always()` cleanup step.
The Firebase app ID and tester-group aliases are masked in logs. Missing secrets
cause the workflow to fail before the Firebase upload step.

## Artifacts

Every successful build uploads the generated debug APK as a GitHub Actions
artifact named:

```text
folentra-internal-apk-<version>-<code>-<ref>-<run>
```

The artifact comes from:

```text
androidApp/app/build/outputs/apk/debug/*.apk
```

No keystores, service-account files, Firebase credentials, Play Store
credentials, mapping files, crash-reporting tokens, or private tester data are
uploaded by this workflow. The workflow summary records only the ref, version,
APK artifact name, and job result; it intentionally omits tester groups, release
notes content, and secret values.

## Release Notes

Manual runs can provide release notes through the `release_notes` workflow
input. Tag-triggered runs and manual runs without notes use the latest commit
message. Release notes should describe user-visible changes and validation
context; do not include secrets, private tester information, personal financial
data, stack traces, or raw diagnostics.

## Staged Rollout Readiness

This workflow supports `US-E17-01 - Automate CI/CD and release workflows` by
providing a repeatable internal tester pipeline after lint, JVM/shared tests,
and APK assembly pass. It complements the E-18 release candidate workflow, which
continues to own release APK/AAB generation, optional release signing, mapping
artifacts, and Play Store internal-testing readiness.

Intentionally not automated here:

- Production Play Store upload.
- Staged rollout promotion.
- Release signing setup.
- Crashlytics, Analytics, or remote observability SDK integration.
- Connected device tests, Macrobenchmark measurements, or Baseline Profile
  generation.

Keep production publishing and internal tester distribution separate until a
future release workflow explicitly defines promotion rules, approval gates,
Play Console credentials, and rollback behavior.

## Closure Checklist For #120

`T-E17-02 - Configure App Distribution/internal tester flow` is complete when
all of these repository-side items remain true:

- `.github/workflows/internal-distribution.yml` can be run manually or from a
  `beta-*` tag.
- The workflow validates Gradle project discovery, debug lint, JVM/shared tests,
  and debug APK assembly before upload.
- The debug APK is retained as a GitHub Actions artifact with version, version
  code, ref, and run number in the artifact name.
- Firebase upload requires `FIREBASE_APP_ID`,
  `FIREBASE_SERVICE_ACCOUNT_JSON`, and `FIREBASE_TESTER_GROUPS` secrets and
  fails before upload when any are missing.
- Firebase credentials are written only to runner temp storage, are not uploaded
  as artifacts, are masked where practical, and are removed during cleanup.
- Release notes support both manual workflow input and tag-triggered commit
  messages, with no release-note content echoed into summaries.
- The workflow is documented here and summarized from `docs/ci-cd.md` so
  maintainers know it is for internal debug tester feedback only, not Play Store
  publication or production release promotion.

After those checks are present in `dev`, #120 can be closed. Its parent #119 can
then be closed once #121 and #122 are also closed.
