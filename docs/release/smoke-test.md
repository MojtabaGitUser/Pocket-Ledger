# Release Candidate Smoke Test

## Repeatable Release-Candidate Gate (#134)

The remaining runtime checklist is implemented as an end-to-end UI Automator test against the
installed, minified `benchmark` APK. Unlike isolated Compose tests, this exercises the production
application ID, real navigation, Room persistence, and release/benchmark source-set wiring.

Run from the repository root with one specifically named, authorized device or emulator:

```powershell
.\scripts\run_release_candidate_smoke.ps1 -DeviceSerial <adb-serial>
```

If the device already contains a Play-signed `com.mojtaba.folentra`, Android will reject the
debug-signed benchmark APK because its certificate differs. Prefer a dedicated test device/emulator.
Only when loss of that installation's local Folentra data is acceptable, explicitly authorize its
removal and replacement:

```powershell
.\scripts\run_release_candidate_smoke.ps1 -DeviceSerial <adb-serial> -ReplaceExistingPackage
```

The runner fails unless the requested serial is connected. It builds and installs the candidate,
runs `ReleaseCandidateSmokeTest`, validates backup/device-transfer rules, rejects fatal app
exceptions from a clean logcat session, and writes device metadata, the APK SHA-256, instrumentation
output, and logcat under the git-ignored `androidApp/release-smoke-evidence/<timestamp>/` directory.
The device must be unlocked for the connected UI run; the runner fails its preflight when Android
Keyguard is active, temporarily keeps the display awake while USB-powered, and restores the previous
stay-awake setting afterwards.

The connected test verifies:

- launch and Dashboard rendering;
- top-level navigation and seeded transaction list;
- create-form validation, transaction creation, editing, persistence, search, and deletion;
- Settings security/privacy entries, including App Lock and the backup-ready profile;
- absence of the Debug Health destination before and after navigation.

App Lock authentication itself remains device-dependent: the smoke gate verifies the installed
security control and its availability state without changing the user's biometric/device-credential
configuration. Firebase remains optional locally by design; a Play-signed Firebase-enabled candidate
must still be checked through the protected release workflow when its external configuration is
available. A successful local run must not be represented as a production-signed run.

## 2026-08-31 Physical-Device Gate Result (#134)

| Field | Result |
| --- | --- |
| Branch base / commit | `dev` / `949f436` plus the uncommitted #134 implementation under validation |
| Target | Samsung `SM-S906W`, Android 16 / API 36, serial `R3CT60LKXEA` |
| Installed artifact | Minified, non-debuggable `androidApp/app/build/outputs/apk/benchmark/app-benchmark.apk` |
| Artifact SHA-256 | `5947C3175402A95B35D718B81E1F97AD7CFDC3AF93BC34A6212261DC672DD4B0` |
| Automated result | Passed: one test, zero failures, zero errors, 22.589 seconds |
| Evidence run ID | `20260831-234009` |

The gate passed launch, Dashboard, top-level navigation, deterministic Room seed verification,
incomplete-form validation, transaction creation/detail/edit/search/delete, Settings
security/privacy entries, absence of Debug Health, backup/device-transfer rule validation, and the
clean-session fatal-exception check. The local evidence directory is intentionally git-ignored; its
`result.json`, instrumentation output, and logcat remain available on the validation workstation.

This physical-device result satisfies the connected runtime condition for `T-E19-05`. Production
signing, Play-distributed Firebase configuration, and an actual OS backup/device-transfer exercise
remain separate release-environment checks and are not claimed by this debug-signed benchmark run.

## 2026-07-19 Connected Release-Like Validation (Supersedes Earlier Device Blockers)

| Field | Result |
| --- | --- |
| Branch / commit | `dev` / `adc51e4` plus the uncommitted #10 + #128 implementation under validation |
| Target | Android Emulator `Pixel_9_Pro_XL(AVD)`, model `sdk_gphone16k_x86_64`, API 37 |
| Installed artifact | `androidApp/app/build/outputs/apk/benchmark/app-benchmark.apk` |
| Install | Passed with `adb install -r`; package `com.mojtaba.folentra` |
| Launch | Passed; cold launch reached Dashboard without a fatal exception |
| Build behavior | Non-debuggable, minified, resource-shrunk, profileable, release-like, debug-signed |
| Signed production release | Still blocked because local release-signing inputs are intentionally unavailable |

Connected smoke results:

- Dashboard launched and rendered its empty state.
- Deterministic benchmark data was prepared successfully; Transactions rendered `Neighborhood Market`.
- Keyword search for `Bluebird` rendered `Bluebird Coffee`.
- Settings opened and rendered the Security section.
- Debug-only destinations were absent from the benchmark navigation.
- No fatal application exception was observed in the clean post-fix launch/navigation logcat pass.
- Macrobenchmark startup/dashboard/search scenarios ran, and the large-dataset and transaction-scroll classes passed on the emulator with emulator-error suppression. Emulator numbers are diagnostic only and are not physical-device performance targets.
- `:app:generateReleaseBaselineProfile` passed and copied the generated release profile into `app/src/release/generated/baselineProfiles/baseline-prof.txt`.

Artifacts were rebuilt successfully with `:app:assembleRelease`, `:app:bundleRelease`, `:app:assembleBenchmark`, lint, JVM tests, shared tests, and the macrobenchmark module assembly. The unsigned release APK/AAB remain validation artifacts; the benchmark APK is the installable release-like candidate used for #128 evidence.

Remaining release blockers are limited to production signing credentials, a real Play internal-testing upload/install, physical-device coverage (including lower-end hardware), and runtime backup/device-transfer validation. Earlier statements below that say no emulator/device was attached are retained as historical run evidence and are superseded by this section.

This record supports `T-E19-05 - Run release candidate smoke test` and #128
`US-E19-01 - Install a release-ready Folentra build` under
`E-19 - Play Store Readiness`. It records the release-candidate validation that
was possible from the local repository state and clearly separates verified
results from device-dependent blockers.

## Test Run

| Field | Result |
| --- | --- |
| Date | 2026-07-15 America/Vancouver |
| Repository | `D:\Folentra` |
| App module | `:app` at `androidApp/app` |
| Production application ID | `com.mojtaba.folentra` |
| Version tested | `versionName=1.0.0`, `versionCode=1` |
| Release build tested | `release` unsigned validation APK/AAB rebuilt with `assembleRelease` and `bundleRelease` |
| Closest installable release-like build | `benchmark` APK rebuilt with `assembleBenchmark`; see `docs/release/release-ready-install.md` |
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
| `adb devices -l` | Passed command execution; no devices listed after adb daemon startup |
| `adb install -r androidApp\app\build\outputs\apk\benchmark\app-benchmark.apk` | Blocked: `adb.exe: no devices/emulators found` |
| `aapt dump badging` on release and benchmark APKs | Passed |

## Latest Batch 3 Validation

During the #131 + #128 + #17 readiness batch, the following repository checks
were re-run from `D:\Folentra`:

- `:app:assembleRelease`: passed.
- `:app:bundleRelease`: passed.
- `:app:assembleBenchmark`: passed.
- `:app:validateReleaseSigning`: failed as expected because release signing
  secrets are not configured.
- `adb devices -l`: passed command execution and listed no attached targets.

This is enough to verify repository release-path artifacts, but not enough to
close #128 because no named device/emulator install or Play internal testing
install evidence exists yet.

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

- `FOLENTRA_RELEASE_STORE_FILE`
- `FOLENTRA_RELEASE_STORE_PASSWORD`
- `FOLENTRA_RELEASE_KEY_ALIAS`
- `FOLENTRA_RELEASE_KEY_PASSWORD`

No release signing secrets were added, printed, uploaded, or inferred. The
release APK/AAB are validation artifacts only and must not be treated as
release-ready installers. The `benchmark` APK is the closest available
installable release-like build because it inherits release behavior, keeps
minification/resource shrinking, is not debuggable, and uses debug signing.

## Build Metadata Verified

| Check | Status | Evidence |
| --- | --- | --- |
| Release application ID | Passed | APK metadata and generated `BuildConfig` show `com.mojtaba.folentra` |
| Release version | Passed | APK metadata and generated `BuildConfig` show `1.0.0` / `1` |
| Release debuggability | Passed | Release `BuildConfig.DEBUG=false`; merged manifest has no `android:debuggable` flag |
| Release environment | Passed | Release `BuildConfig.APP_ENV="release"` |
| Release internal diagnostics | Passed | Release `BuildConfig.IS_INTERNAL_BUILD=false` |
| Release logging | Passed | Release `BuildConfig.LOGGING_ENABLED=false` |
| Benchmark application ID | Passed | APK metadata and generated `BuildConfig` show `com.mojtaba.folentra` |
| Benchmark version | Passed | APK metadata and generated `BuildConfig` show `1.0.0` / `1` |
| Benchmark debuggability | Passed | Benchmark `BuildConfig.DEBUG=false`; merged manifest has no `android:debuggable` flag |
| Benchmark minified build path | Passed | `:app:assembleBenchmark` ran R8 and generated benchmark mapping outputs |

## #128 Release-Ready Install Evidence

The repository now keeps the install runbook in
`docs/release/release-ready-install.md`. This smoke record remains the place to
record actual device/emulator evidence. At the time of this repository pass,
install and launch evidence is still blocked until a named device or emulator
is attached.

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

The blocked rows above are historical results from the 2026-07-15 run. The named physical-device
gate recorded in the 2026-08-31 section now satisfies this connected runtime condition for
`T-E19-05`.

## Firebase And Release Behavior

| Check | Status | Result |
| --- | --- | --- |
| Google Services processing | CI-ready, runtime evidence pending | Protected CI injects and validates matching Folentra clients before Gradle runs. |
| Firebase clients present | External configuration required | Store reviewed release/debug JSON in `FIREBASE_GOOGLE_SERVICES_JSON`; the file is intentionally not committed. |
| Release merged manifest Firebase initialization | Automated configuration ready | Re-test the generated configured release manifest and installed artifact. |
| Runtime Firebase startup | Blocked | Requires app launch on device/emulator. |
| Firebase client/package match | Automated gate ready | CI rejects missing package clients and a release App ID mismatch. |
| External distribution | External execution pending | Workflow distributes a signed, minified release APK after protected secrets are configured. |

Firebase Analytics configuration is supplied only by protected CI. Product
analytics logging in app code remains no-op for release and benchmark through
`NoOpProductAnalyticsLogger`; automatic Firebase SDK behavior must still be
reviewed and disclosed.

## Debug Health Release-Hidden Verification

| Check | Status | Result |
| --- | --- | --- |
| Debug navigation gated | Passed | `FolentraApp` passes `includeDebugDestinations = BuildConfig.DEBUG`; release and benchmark have `DEBUG=false`. |
| Debug top-level item hidden | Passed | `FolentraAppState` only adds `TopLevelDestination.DebugHealth` when debug destinations are included. |
| Debug route not registered | Passed | `FolentraNavGraph` only registers `AppDestination.DebugHealth` inside `if (includeDebugDestinations)`. |
| Release source-set implementation | Passed | Release `DebugHealthScreen` is an empty compile stub and is not normally routable. |
| Normal navigation route access | Passed by static review | No release/benchmark top-level navigation entry registers `debug/health`. |
| Accessibility exposure | Passed by static review | Debug Health UI semantics live in the debug source-set implementation and are not composed in release/benchmark navigation. |
| Runtime route probing | Blocked | Requires installed APK on device/emulator. |

## Privacy-Sensitive Behavior

| Check | Status | Result |
| --- | --- | --- |
| Privacy policy consistency | Passed by document/source review | `docs/privacy-policy.md` matches local-first storage, Firebase dependencies with runtime disabled pending new config, no cloud sync, and backup limitations. |
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
| `backup_rules.xml` | Passed by static review | Active deny-by-default rules exclude app-private ledger data, encrypted preferences, app-private files, caches, logs, temp/debug/generated files, and external app files. |
| `data_extraction_rules.xml` | Passed by static review | Android 12+ cloud-backup and device-transfer sections both use active deny-by-default rules for ledger data and local-only state. |
| Runtime backup/device-transfer validation | Blocked | Device/emulator validation is still required; static XML policy is no longer unresolved. |

Backup/device-transfer XML is no longer a release blocker by source review.
Runtime backup/device-transfer behavior still needs device or release-candidate
validation before public Play Store submission. Do not claim encrypted ledger
backup or restore; #81 is only a local-first foundation.

## Known Blockers

- Release signing secrets are not configured locally, so signed release-ready
  APK/AAB generation was not possible.
- No physical device or emulator was attached, so install, launch, logcat, and
  manual core-flow smoke testing could not be completed.
- Runtime backup/device-transfer behavior still needs device or release-candidate
  validation; source XML policy is deny-by-default and documented.
- Privacy policy still needs a public HTTPS hosting URL and real public support
  contact before Play Store submission.

## Follow-Up Recommendations

1. Attach a named physical device or emulator and install
   `androidApp/app/build/outputs/apk/benchmark/app-benchmark.apk`.
2. Record device name, Android version, install result, launch result, and
   logcat startup review.
3. Execute the manual core-flow checklist in this file and update each blocked
   item to Passed or Failed with evidence.
4. Run a signed release candidate build with `FOLENTRA_REQUIRE_RELEASE_SIGNING=true`
   after release signing inputs are available.
5. Re-run backup/device-transfer review before public Play Store submission and
   keep ledger data excluded unless encrypted backup/restore is implemented.
