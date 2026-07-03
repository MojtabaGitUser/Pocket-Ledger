# Release Candidate Smoke Test

This record supports `T-E19-05 - Run release candidate smoke test` under
`E-19 - Play Store Readiness`. It records the release-candidate validation that
was possible from the local repository state and clearly separates verified
results from device-dependent blockers.

## Test Run

| Field | Result |
| --- | --- |
| Date | 2026-07-02 America/Vancouver |
| Repository | `D:\PocketLedger` |
| App module | `:app` at `androidApp/app` |
| Production application ID | `com.mojtaba.pocketledger` |
| Version tested | `versionName=1.0.0`, `versionCode=1` |
| Release build tested | `release` unsigned validation APK/AAB |
| Closest installable release-like build | `benchmark` APK |
| Install target | Blocked: `adb devices -l` returned no attached devices or emulators |
| Manual launch/core-flow smoke | Blocked until a physical device or emulator is attached |

## Commands Run

| Command | Result |
| --- | --- |
| `.\androidApp\gradlew.bat projects --console=plain` | Passed |
| `.\androidApp\gradlew.bat :app:validateReleaseSigning --console=plain` | Failed as expected: release signing inputs are not configured |
| `.\androidApp\gradlew.bat :app:assembleRelease --console=plain` | Passed |
| `.\androidApp\gradlew.bat :app:bundleRelease --console=plain` | Passed |
| `.\androidApp\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble --console=plain` | Passed |
| `.\androidApp\gradlew.bat lintRelease --console=plain` | Passed |
| `.\androidApp\gradlew.bat testDebugUnitTest :shared:allTests --console=plain` | Passed |
| `adb devices -l` | Passed command execution; no devices listed |
| `adb install -r androidApp\app\build\outputs\apk\benchmark\app-benchmark.apk` | Blocked: `adb.exe: no devices/emulators found` |
| `aapt dump badging` on release and benchmark APKs | Passed |

## Generated Artifacts

| Artifact | Result |
| --- | --- |
| `androidApp/app/build/outputs/apk/release/app-release-unsigned.apk` | Generated; unsigned release validation APK |
| `androidApp/app/build/outputs/bundle/release/app-release.aab` | Generated; unsigned release validation AAB |
| `androidApp/app/build/outputs/apk/benchmark/app-benchmark.apk` | Generated; debug-signed, non-debuggable, minified/profileable APK |
| `androidApp/app/build/outputs/mapping/release/` | Generated with release R8 outputs |
| `androidApp/app/build/outputs/mapping/benchmark/` | Generated with benchmark R8 outputs |

Release signing is not configured locally. `:app:validateReleaseSigning` failed
with the expected Gradle message requiring all of:

- `POCKET_LEDGER_RELEASE_STORE_FILE`
- `POCKET_LEDGER_RELEASE_STORE_PASSWORD`
- `POCKET_LEDGER_RELEASE_KEY_ALIAS`
- `POCKET_LEDGER_RELEASE_KEY_PASSWORD`

No release signing secrets were added, printed, uploaded, or inferred. The
release APK/AAB are validation artifacts only and must not be treated as
release-ready installers. The `benchmark` APK is the closest available
installable release-like build because it inherits release behavior, keeps
minification/resource shrinking, is not debuggable, and uses debug signing.

## Build Metadata Verified

| Check | Status | Evidence |
| --- | --- | --- |
| Release application ID | Passed | APK metadata and generated `BuildConfig` show `com.mojtaba.pocketledger` |
| Release version | Passed | APK metadata and generated `BuildConfig` show `1.0.0` / `1` |
| Release debuggability | Passed | Release `BuildConfig.DEBUG=false`; merged manifest has no `android:debuggable` flag |
| Release environment | Passed | Release `BuildConfig.APP_ENV="release"` |
| Release internal diagnostics | Passed | Release `BuildConfig.IS_INTERNAL_BUILD=false` |
| Release logging | Passed | Release `BuildConfig.LOGGING_ENABLED=false` |
| Benchmark application ID | Passed | APK metadata and generated `BuildConfig` show `com.mojtaba.pocketledger` |
| Benchmark version | Passed | APK metadata and generated `BuildConfig` show `1.0.0` / `1` |
| Benchmark debuggability | Passed | Benchmark `BuildConfig.DEBUG=false`; merged manifest has no `android:debuggable` flag |
| Benchmark minified build path | Passed | `:app:assembleBenchmark` ran R8 and generated benchmark mapping outputs |

## Install And Runtime Smoke

| Flow | Status | Result |
| --- | --- | --- |
| Install release candidate | Blocked | No attached device/emulator. Unsigned release APK is not the intended install artifact. |
| Install closest release-like APK | Blocked | `adb install -r androidApp\app\build\outputs\apk\benchmark\app-benchmark.apk` could not run because no target was attached. |
| Launch app without startup crash | Blocked | Requires installed APK on device/emulator. |
| Core navigation | Blocked | Requires runtime manual or connected UI smoke. |
| Dashboard loads | Blocked | Requires runtime manual or connected UI smoke. |
| Transaction creation | Blocked | Requires runtime manual or connected UI smoke. |
| Transaction edit | Blocked | Requires runtime manual or connected UI smoke. |
| Transaction delete | Blocked | Requires runtime manual or connected UI smoke. |
| Transaction validation/errors | Blocked | Requires runtime manual or connected UI smoke. |
| Search flow | Blocked | Requires runtime manual or connected UI smoke. |
| Settings flow | Blocked | Requires runtime manual or connected UI smoke. |
| Security/privacy-related UI flows | Blocked | Requires runtime manual or connected UI smoke. |
| Empty/loading/error states | Blocked for runtime smoke | JVM, Compose/Paparazzi, and feature tests passed through `testDebugUnitTest :shared:allTests`; device runtime was not executed. |

Do not mark `T-E19-05` fully complete until a named physical device or emulator
has installed and launched the release-like APK and the manual flow checklist
above has been executed.

## Firebase And Release Behavior

| Check | Status | Result |
| --- | --- | --- |
| Google Services processing | Passed | `:app:processReleaseGoogleServices` and `:app:processBenchmarkGoogleServices` ran during artifact generation. |
| Firebase clients present | Passed | `androidApp/app/google-services.json` contains release and debug clients for the project package names. |
| Release merged manifest Firebase initialization | Passed | Release merged manifest includes Firebase measurement services and `FirebaseInitProvider`. |
| Runtime Firebase startup | Blocked | Requires app launch on device/emulator. |
| Debug-only Firebase client dependency | Passed by static review | Release application ID matches the release Firebase client; debug suffix is not used by release/benchmark. |
| External distribution | Not applicable | No Firebase App Distribution upload was triggered. Existing workflow distributes debug APKs only and remains separate. |

Firebase Analytics is present in the release app. Product analytics logging in
app code remains no-op for release and benchmark through `NoOpProductAnalyticsLogger`.

## Debug Health Release-Hidden Verification

| Check | Status | Result |
| --- | --- | --- |
| Debug navigation gated | Passed | `PocketLedgerApp` passes `includeDebugDestinations = BuildConfig.DEBUG`; release and benchmark have `DEBUG=false`. |
| Debug top-level item hidden | Passed | `PocketLedgerAppState` only adds `TopLevelDestination.DebugHealth` when debug destinations are included. |
| Debug route not registered | Passed | `PocketLedgerNavGraph` only registers `AppDestination.DebugHealth` inside `if (includeDebugDestinations)`. |
| Release source-set implementation | Passed | Release `DebugHealthScreen` is an empty compile stub and is not normally routable. |
| Normal navigation route access | Passed by static review | No release/benchmark top-level navigation entry registers `debug/health`. |
| Accessibility exposure | Passed by static review | Debug Health UI semantics live in the debug source-set implementation and are not composed in release/benchmark navigation. |
| Runtime route probing | Blocked | Requires installed APK on device/emulator. |

## Privacy-Sensitive Behavior

| Check | Status | Result |
| --- | --- | --- |
| Privacy policy consistency | Passed by document/source review | `docs/privacy-policy.md` matches local-first storage, Firebase Analytics presence, no Crashlytics runtime, no cloud sync, and backup limitations. |
| Play Store app content consistency | Passed by document/source review | `docs/play-store-readiness.md` records Firebase/Data Safety, permissions, backup, support-contact, and policy-hosting limitations. |
| Product event taxonomy | Passed by source/test review | Taxonomy uses approved event names and bucketed parameters; no exact amounts, notes, merchant names, category names, raw IDs, or secrets are allowed. |
| Release product analytics behavior | Passed by source review | Release and benchmark construct `NoOpProductAnalyticsLogger`. |
| Release logging behavior | Passed by source/test review | Release uses `LoggingPolicy.Release`; logging tests passed in `testDebugUnitTest`. |
| Direct unsafe logging search | Passed with expected exception | Source search found Android `Log.*` only in centralized `AndroidLogSink`; sensitive examples were confined to tests/docs. |
| Runtime logcat verification | Blocked | Requires installed APK on device/emulator and a clean launch/session log review. |

No signing credentials, Firebase service account JSON, tester data, Play Store
credentials, stack traces, or private CI metadata were exposed during this task.

## Backup And Device Transfer

| Check | Status | Result |
| --- | --- | --- |
| Manifest backup config | Passed by static review | `allowBackup=true`, `dataExtractionRules`, and `fullBackupContent` are configured. |
| `backup_rules.xml` | Release blocker | File remains template-style with no active include/exclude rules. |
| `data_extraction_rules.xml` | Release blocker | File contains cloud-backup TODO comments and no active device-transfer policy. |
| Runtime backup/device-transfer validation | Blocked | Concrete policy is unresolved, so there is nothing safe to validate as final behavior. |

Backup/device-transfer remains a release blocker. Do not claim ledger data,
encrypted preferences, or app settings are excluded from cloud backup or device
transfer until final rules are implemented and the privacy policy and Play Store
readiness checklist are updated.

## Known Blockers

- Release signing secrets are not configured locally, so signed release-ready
  APK/AAB generation was not possible.
- No physical device or emulator was attached, so install, launch, logcat, and
  manual core-flow smoke testing could not be completed.
- Backup and device-transfer rules are unresolved and remain a pre-release
  blocker.
- Privacy policy still needs a public HTTPS hosting URL and real public support
  contact before Play Store submission.

## Follow-Up Recommendations

1. Attach a named physical device or emulator and install
   `androidApp/app/build/outputs/apk/benchmark/app-benchmark.apk`.
2. Record device name, Android version, install result, launch result, and
   logcat startup review.
3. Execute the manual core-flow checklist in this file and update each blocked
   item to Passed or Failed with evidence.
4. Run a signed release candidate build with `POCKET_LEDGER_REQUIRE_RELEASE_SIGNING=true`
   after release signing inputs are available.
5. Finalize backup/device-transfer rules before public Play Store submission.
