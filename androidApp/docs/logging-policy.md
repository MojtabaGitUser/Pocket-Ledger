# Privacy-Safe Logging Policy

Pocket Ledger logs operational state only. Logs must never contain personal
financial data, security credentials, secrets, or raw user-entered text.

## Data Classification

### Highly Sensitive

Never log:
- Transaction amounts.
- Transaction notes.
- Tags.
- User-created categories.
- Merchant names.
- Account identifiers.
- Tokens.
- Passkeys.
- Credentials.
- Secrets.
- Encryption keys.
- Encrypted payloads.
- Exported ledger content.

### Sensitive

Do not log raw values:
- Search queries.
- Filters.
- User preferences.
- Budget values.
- Analytics identifiers.

### Safe

These may be logged when useful:
- Screen names.
- Navigation events.
- Build information.
- Generic lifecycle events.
- Feature flag keys and enabled/disabled state.
- Success/failure states.
- Anonymized counts.

## Logging API

Application and feature code must use `AppLogger` instead of direct Logcat
APIs:

```kotlin
logger.info("Screen navigation succeeded screen=Dashboard")
logger.warning("Repository refresh failed repository=transactions")
logger.error(
    throwable = exception,
    message = "Transaction save failed",
)
```

Do not use these in feature or business code:

```kotlin
Log.d("PocketLedger", query)
Log.e("PocketLedger", exception.message)
println(transaction)
exception.printStackTrace()
```

The only approved Android Logcat usage is inside the centralized logging sink
in `:core:security`.

## Redaction Behavior

`SafeAppLogger` sanitizes messages and throwable messages before writing to the
sink. Known sensitive key-value pairs are replaced with `[REDACTED]`.

Examples:

```text
token=abcd1234 -> token=[REDACTED]
password=myPassword -> password=[REDACTED]
access_token=XYZ -> access_token=[REDACTED]
merchant=Starbucks -> merchant=[REDACTED]
note=Vacation in Vancouver -> note=[REDACTED]
```

Bearer tokens and JWT-shaped values are also redacted.

## Allowed Events

Logging may describe:
- App startup.
- Screen navigation.
- Repository lifecycle events.
- Sync lifecycle events.
- Cache events.
- Migration status.
- WorkManager scheduling.

## Forbidden Data

Logging must not include:
- Transaction note contents.
- Transaction amounts.
- Search text.
- Budget values.
- Credentials.
- Tokens.
- Secrets.
- Encrypted payloads.

## Debug And Release Behavior

Debug builds allow sanitized debug, info, warning, and error logs.

Release builds suppress debug and info logs automatically. Release logs are
limited to sanitized warnings and errors.

The app composition root selects the policy from build configuration. Feature
code must not branch on build type to decide whether a log is safe.

## Error Handling

Exception messages can contain user data. Do not pass exception messages as log
messages:

```kotlin
// Bad
logger.error(message = exception.message ?: "Save failed")

// Good
logger.error(
    throwable = exception,
    message = "Transaction save failed",
)
```

`SafeAppLogger` sanitizes throwable messages before they reach Logcat.
## Crash Reporting And Startup Failures

Release crash reporting uses the app-level `CrashReporter` abstraction backed by
Firebase Crashlytics. Debug and benchmark builds keep collection disabled with
`BuildConfig.CRASH_REPORTING_ENABLED=false`.

Critical startup failures are reported through `StartupFailureReporter`. The
reporter may log and submit only sanitized operational metadata: startup stage,
throwable class name, timestamp, and whether Crashlytics accepted the event.
Never add user-entered financial data, credentials, Firebase service account
contents, tester emails, tokens, or raw exception messages as crash attributes.
