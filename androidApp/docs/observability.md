# Observability And Crash Reporting

Folentra uses privacy-safe observability for production issue detection.
The runtime surface is intentionally small: sanitized app logging, Firebase
Crashlytics for release crash reporting, a startup failure reporter, and the
Debug Health screen for debug-only inspection.

## Crash Reporting

Crash reporting is wired through `CrashReporter` in the app module. Production
builds use Firebase Crashlytics when Firebase resources are present. Debug and
benchmark builds keep Crashlytics configured but disable collection through
`BuildConfig.CRASH_REPORTING_ENABLED`.

Build-type behavior:

| Build type | Crash collection | Debug Health route | Notes |
| --- | --- | --- | --- |
| debug | Disabled | Visible | Local diagnostics only. |
| release | Enabled | Hidden | Release-safe crash reports only. |
| benchmark | Disabled | Hidden | Benchmarks avoid diagnostic noise. |

Crash reports must not include raw transaction data, notes, amounts, search
text, credentials, tokens, Firebase service account values, tester emails, or
private CI metadata. The crash reporter records sanitized event names and
bounded custom keys only. Custom keys use an explicit allowlist:
`build_variant`, `component`, `operation`, `stage`, and `throwable_class`.
Unknown keys are discarded. Throwable messages are redacted, causes are not
forwarded, and only the original stack frames are retained.

`google-services.json` is not stored in Git. CI writes it from the protected
`FIREBASE_GOOGLE_SERVICES_JSON` environment secret with owner-only file
permissions, validates both Folentra package clients and the release Firebase
App ID, and deletes the file in an `always()` cleanup step.

## Startup Failure Reporting

`StartupFailureReporter` records critical startup failures from the app
composition root and `MainActivity`. It keeps only:

- The sanitized startup stage.
- The throwable class name.
- The event timestamp.
- Whether the event was sent to crash reporting.

The reporter sends release startup failures to the configured crash reporter and
logs sanitized operational errors through `SafeAppLogger`.

## Debug Health

Debug Health exposes safe runtime status for:

- Build, CI, testing, benchmark, and Firebase readiness.
- Crashlytics configuration and collection gating.
- Startup failure tracker state and the last sanitized startup failure summary.
- Logging/redaction behavior and release diagnostics privacy.
- Background worker status.

Release and benchmark variants use source-set stubs and do not register the
Debug Health destination.

## Validation

When changing observability code, run:

```powershell
.\androidApp\gradlew.bat --no-daemon :app:testDebugUnitTest :app:compileReleaseKotlin --console=plain --stacktrace
```

Use `git diff --check` before review to catch whitespace issues.

### Explicit non-fatal smoke event

The instrumentation probe is skipped by default and must never contain real
financial or tester data. With a valid local debug Firebase client installed,
run only this class and opt in explicitly:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest `
  -Pandroid.testInstrumentationRunnerArguments.class=com.mojtaba.folentra.observability.FirebaseCrashlyticsSmokeTest `
  -Pandroid.testInstrumentationRunnerArguments.firebaseCrashlyticsSmoke=true `
  --console=plain
```

Confirm `firebase_non_fatal_smoke_probe` appears for the debug Firebase client,
then verify collection is disabled again. This probe is validation-only;
normal debug and benchmark collection remains disabled.

## Internal distribution secrets

Configure these only in the protected `firebase-internal` GitHub environment:

- `FIREBASE_GOOGLE_SERVICES_JSON`
- `FIREBASE_APP_ID`
- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `FIREBASE_TESTER_GROUPS`
- `FOLENTRA_RELEASE_STORE_BASE64`
- `FOLENTRA_RELEASE_STORE_PASSWORD`
- `FOLENTRA_RELEASE_KEY_ALIAS`
- `FOLENTRA_RELEASE_KEY_PASSWORD`

The workflow builds and verifies a signed, minified release APK, uploads its R8
mapping as a GitHub artifact, distributes the APK to the configured Firebase
tester group, and removes transient credentials and configuration afterward.
