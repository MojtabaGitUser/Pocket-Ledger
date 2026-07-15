# Release-Ready Install Runbook

This runbook supports #128, `US-E19-01 - Install a release-ready Pocket Ledger
build`. It defines what can be verified from the repository, what requires
release signing secrets, and what requires a named Android device or emulator.

## Artifact Classes

| Artifact | Command | Installable | Play Store candidate | Notes |
| --- | --- | --- | --- | --- |
| Unsigned release APK | `:app:assembleRelease` | No, not a release-ready installer | No | Used for R8/resource-shrinking/release-path validation only. |
| Unsigned release AAB | `:app:bundleRelease` | No local install | No | Used for bundle/release-path validation only. |
| Signed release APK | `:app:validateReleaseSigning :app:assembleRelease` with signing inputs | Yes | Optional handoff artifact | Requires all `POCKET_LEDGER_RELEASE_*` values and `POCKET_LEDGER_REQUIRE_RELEASE_SIGNING=true`. |
| Signed release AAB | `:app:validateReleaseSigning :app:bundleRelease` with signing inputs | Via Play/internal app sharing tooling | Preferred | Preferred artifact for Play Console internal testing and release. |
| Benchmark APK | `:app:assembleBenchmark` | Yes | No | Closest local release-like installer when signing secrets are unavailable; non-debuggable, minified, profileable, debug-signed. |
| Debug APK | `:app:assembleDebug` or Firebase App Distribution workflow | Yes | No | Internal tester feedback only; contains debug identity/diagnostics. |

## Repository Validation Commands

Run from the repository root:

```powershell
.\androidApp\gradlew.bat --no-daemon :app:validateReleaseSigning :app:assembleRelease :app:bundleRelease :app:assembleBenchmark --console=plain --stacktrace
```

Expected behavior without signing secrets:

- `:app:validateReleaseSigning` fails clearly unless signing inputs are present.
- `:app:assembleRelease` and `:app:bundleRelease` can still validate release/R8
  paths when `POCKET_LEDGER_REQUIRE_RELEASE_SIGNING` is not true.
- `:app:assembleBenchmark` produces the closest locally installable
  release-like APK without private signing material.

For signed release-ready validation:

```powershell
$env:POCKET_LEDGER_RELEASE_STORE_FILE="C:\secure\pocket-ledger-upload.jks"
$env:POCKET_LEDGER_RELEASE_STORE_PASSWORD="<secret>"
$env:POCKET_LEDGER_RELEASE_KEY_ALIAS="<secret>"
$env:POCKET_LEDGER_RELEASE_KEY_PASSWORD="<secret>"
$env:POCKET_LEDGER_REQUIRE_RELEASE_SIGNING="true"
.\androidApp\gradlew.bat --no-daemon :app:validateReleaseSigning :app:assembleRelease :app:bundleRelease --console=plain --stacktrace
```

Never commit, print, or attach signing material.

## Install Verification

Use a named physical device or emulator. Record the exact device model, Android
version, build fingerprint if available, artifact path, versionName,
versionCode, and result.

Device discovery:

```powershell
adb devices -l
```

Install benchmark APK when release signing secrets are unavailable:

```powershell
adb install -r androidApp\app\build\outputs\apk\benchmark\app-benchmark.apk
adb shell monkey -p com.mojtaba.pocketledger 1
```

Install signed release APK when signing inputs are available:

```powershell
adb install -r androidApp\app\build\outputs\apk\release\app-release.apk
adb shell monkey -p com.mojtaba.pocketledger 1
```

For Play Console internal testing, upload the signed release AAB and install
through Play's internal testing track or internal app sharing. Do not use the
Firebase debug distribution artifact as Play Store evidence.

## Runtime Smoke Checklist

Mark each item Passed, Failed, or Blocked in `docs/release/smoke-test.md`:

- Install succeeds on the named target.
- App launches without startup crash.
- Dashboard opens with sample-safe data or empty state.
- Transaction list opens.
- Transaction create/edit/delete flows work with sample-safe values.
- Search opens and handles a sample-safe query.
- Insights opens without remote AI claims.
- Settings opens and app lock controls remain optional.
- Backup-ready profile entry does not claim encrypted backup/restore.
- Debug Health is not visible in release/benchmark navigation.
- Logcat startup review shows no secrets, stack traces, tester emails, or ledger
  values.

## Close Readiness

#128 can be closed when one of these evidence paths is recorded:

1. Signed release APK/AAB was built with `validateReleaseSigning`, installed or
   delivered through Play internal testing, launched on a named target, and the
   runtime smoke checklist passed.
2. If signing secrets are intentionally unavailable for the portfolio pass, the
   benchmark release-like APK was installed and launched on a named target, the
   runtime smoke checklist passed, and the release checklist explicitly records
   that final signed Play Store install remains a manual release gate.

#17 can only be closed after #128 evidence, Play Store assets, privacy policy
hosting, app-content declarations, and release checklist review are all updated
with final evidence.
