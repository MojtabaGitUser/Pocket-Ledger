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
bounded custom keys only.

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
