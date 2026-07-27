# Release Signing And Versioning Plan

Folentra's installable release-ready builds are produced by the Android app
module at `androidApp/app` (`:app`). The production application ID is
`com.mojtaba.folentra`. Debug builds use the `.debug` application ID suffix
and `-debug` versionName suffix. Benchmark builds are non-debuggable,
profileable, minified, and debug-signed for local benchmark installation.

This plan covers signing and versioning only. It does not add Play Store upload,
Firebase upload, Crashlytics upload, or any credential material to the app.

## Signing Model

Local debug signing:

- `debug` uses the Android Gradle Plugin debug signing configuration.
- It is installable locally as `com.mojtaba.folentra.debug`.
- It may expose debug-only diagnostics such as Debug Health.
- It is not a Play Store, staged rollout, or production artifact.

Benchmark signing:

- `benchmark` initializes from `release`, keeps minification/resource shrinking,
  is non-debuggable/profileable, and uses debug signing.
- This avoids requiring release secrets for benchmark assembly while keeping code
  close to the release path.
- It is not a production or Play Store artifact.

Unsigned release validation:

- `release` can assemble without signing secrets for PR validation, R8/lint
  checks, and local release-path smoke checks.
- Unsigned validation artifacts must not be treated as release-ready installers.
- This keeps `.github/workflows/pr-validation.yml` secret-free.

Signed release-ready builds:

- Signed release APK/AAB builds require all release signing inputs.
- Set `FOLENTRA_REQUIRE_RELEASE_SIGNING=true` for true signed-release
  validation. Missing values fail Gradle configuration clearly.
- Run `:app:validateReleaseSigning` before producing a signed release-ready APK
  or AAB.

Play Store signing expectation:

- The AAB is the preferred Play Store handoff artifact.
- The project should use Play App Signing. The local/CI upload key is still
  private signing material and must be managed outside the repository.
- Play Console upload and staged rollout promotion are intentionally outside
  this task.

## Signing Inputs

Gradle reads signing inputs from Gradle properties or matching environment
variables:

```text
FOLENTRA_RELEASE_STORE_FILE
FOLENTRA_RELEASE_STORE_PASSWORD
FOLENTRA_RELEASE_KEY_ALIAS
FOLENTRA_RELEASE_KEY_PASSWORD
FOLENTRA_REQUIRE_RELEASE_SIGNING
```

For GitHub Actions signed release candidates, configure these repository or
environment secrets:

```text
FOLENTRA_RELEASE_STORE_BASE64
FOLENTRA_RELEASE_STORE_PASSWORD
FOLENTRA_RELEASE_KEY_ALIAS
FOLENTRA_RELEASE_KEY_PASSWORD
```

The release-candidate workflow decodes `FOLENTRA_RELEASE_STORE_BASE64` into
the runner temporary directory and exports the Gradle properties through
`ORG_GRADLE_PROJECT_*`. The keystore file is never uploaded as an artifact.

Never commit or print:

- `.jks` files.
- `.keystore` files.
- Keystore passwords.
- Key aliases.
- Key passwords.
- Service account JSON files.
- Play Store credentials.
- Firebase service account credentials.
- Private tester lists or private release data.

## Version Source Of Truth

Non-secret version defaults live in Gradle properties:

```text
androidApp/gradle.properties
FOLENTRA_VERSION_CODE=1
FOLENTRA_VERSION_NAME=1.0.0
```

The repository-root `gradle.properties` mirrors these values so root Gradle
invocation stays consistent with `androidApp/gradlew.bat`.

Version policy:

- `versionCode` is a positive monotonic integer and must increase for every Play
  Store upload candidate.
- `versionName` uses semantic form such as `1.0.0` or `1.0.0-rc.1`.
- Debug builds append `-debug` through the debug build type.
- Internal Firebase distribution currently uses the debug build and the same
  base version metadata for artifact names.
- CI may override values for release candidates through workflow inputs or
  `FOLENTRA_VERSION_CODE` / `FOLENTRA_VERSION_NAME` Gradle properties.

Developers should bump the checked-in Gradle properties for normal release
trains. CI overrides are for candidate validation and should not hide the need
to update the source-of-truth version before a Play Store handoff.

## Release-Ready Install Boundary

Install-specific evidence for #128 lives in
`docs/release/release-ready-install.md`. This signing document defines how
to produce signed release artifacts; the install runbook defines which artifact
can be installed, how to record device evidence, and when #128 is close-ready.

## Local Commands

Unsigned validation builds, safe for normal PR/release-path checks:

```powershell
.\androidApp\gradlew.bat :app:assembleRelease --console=plain
.\androidApp\gradlew.bat :app:bundleRelease --console=plain
```

Signed release-ready builds require local secret-backed properties or
environment variables:

```powershell
$env:FOLENTRA_RELEASE_STORE_FILE="C:\secure\folentra-upload.jks"
$env:FOLENTRA_RELEASE_STORE_PASSWORD="<secret>"
$env:FOLENTRA_RELEASE_KEY_ALIAS="<secret>"
$env:FOLENTRA_RELEASE_KEY_PASSWORD="<secret>"
$env:FOLENTRA_REQUIRE_RELEASE_SIGNING="true"
.\androidApp\gradlew.bat :app:validateReleaseSigning :app:assembleRelease :app:bundleRelease --console=plain
```

Do not place those values in committed files. If a local `gradle.properties` is
used for secrets, keep it outside the repository or in an ignored user-level
Gradle properties file.

## CI Release Candidate Behavior

`.github/workflows/release-candidate.yml` can receive optional manual inputs:

```text
version_name
version_code
require_release_signing
```

When signing secrets are present, the workflow validates signing and builds
signed release APK/AAB artifacts. When `require_release_signing=true`, missing
signing secrets fail the workflow before Gradle builds artifacts. Without that
flag, the workflow may still produce unsigned validation artifacts for release
path review.

The workflow does not publish to Play Store, upload to Firebase App
Distribution, expose secrets in logs, or upload keystores. Internal tester debug
APK distribution remains owned by `.github/workflows/internal-distribution.yml`.

## Known Limitations And Follow-Ups

- No dedicated `internal` or `beta` build type exists yet; Firebase App
  Distribution currently uses debug APKs.
- No Play Console upload, staged rollout, or promotion workflow is implemented.
- No real signing material is checked in or generated by this project.
- Final Play Store readiness still needs final binary store graphics, hosted
  privacy policy review, app content declarations, physical-device or Play
  internal-testing install evidence, and release manager approval.
