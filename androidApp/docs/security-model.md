# Pocket Ledger Security Model And Limitations

This document describes the security and privacy behavior implemented in the
current Android app. It is intended for developers and reviewers. It does not
claim compliance certification, full device protection, or security features
that are not present in code.

## Overview

Pocket Ledger is a local-first personal finance app. The current security and
privacy model is built around these goals:

- Keep ledger data local to the Android app unless a future feature explicitly
  changes that behavior.
- Minimize exposure of sensitive ledger data in UI, logs, and optional AI
  flows.
- Use on-device or deterministic local AI paths only in the current build.
- Provide an optional app lock that uses Android system authentication.
- Centralize sensitive preference storage behind `SensitivePreferences`.
- Centralize logging behind `AppLogger` and redact known sensitive values.
- Keep optional and incomplete capabilities behind safe default feature flags.

The app does not currently implement accounts, passkeys, cloud sync, remote AI,
production analytics collection, server-backed authentication, or full database encryption.

## Data Storage Model

Ledger data is stored locally in the app-private Room database
`pocket-ledger.db`, created by `PocketLedgerDatabase` in `:core:database` and
opened from `AppGraph` in `:app`.

The database stores normal ledger records such as transactions, budgets,
categories, tags, and transaction-tag links. These records are protected by the
Android app sandbox and normal Android filesystem permissions, but they are not
encrypted at rest by Pocket Ledger. The project does not currently use SQLCipher
or an equivalent encrypted database implementation.

Sensitive app preferences use a separate encrypted preferences file rather than
Room. Production construction creates `EncryptedSensitivePreferences` in
`AppGraph`; tests can use `InMemorySensitivePreferences`.

T-E10-06 (#227) hardens Android backup and data extraction rules with explicit
deny-by-default XML policy. `backup_rules.xml` covers pre-Android 12 Auto
Backup, and `data_extraction_rules.xml` covers Android 12+ cloud backup and
device-to-device transfer. The policy excludes the Room ledger database, SQLite
sidecar files, encrypted sensitive preferences, app-private files, shared
preferences, external app files, device-protected storage, caches, logs, temp
folders, debug folders, and generated report folders.

No app-private Pocket Ledger data is intentionally included in automatic cloud
backup or device-to-device transfer. This is a privacy-safe default because the
optional backup-ready profile from #81 is not implemented and the ledger
database is not encrypted by Pocket Ledger. This hardening supports related
security story #7 and release-hardening story #129, but it does not complete
those parent stories by itself.

## Sensitive Preferences

`SensitivePreferences` is the typed abstraction for sensitive local preference
values. It supports string, boolean, and long keys through
`StringPreferenceKey`, `BooleanPreferenceKey`, and `LongPreferenceKey`.

Production storage uses `EncryptedSensitivePreferences`, backed by AndroidX
Security Crypto:

- `MasterKey` uses `AES256_GCM`.
- Preference keys use `AES256_SIV`.
- Preference values use `AES256_GCM`.
- Values are stored in the app-private file `pocket_ledger_sensitive_prefs`.
- Read/write work runs on an IO dispatcher.
- Error messages avoid including preference keys or stored values.

Defined keys live in `DefaultSensitivePreferenceKeys`:

- `PasskeyCredentialId`
- `AccountSessionToken`
- `LastSecurityCheckAt`
- `BiometricUnlockEnabled`

Only `BiometricUnlockEnabled` is used by current app-lock behavior. The passkey,
session, and security-check keys are definitions for future work; they do not
mean those features are implemented or populated.

`InMemorySensitivePreferences` is used for deterministic unit and UI tests. It
does not persist to disk and does not provide encryption.

Limitations:

- `EncryptedSharedPreferences` protects stored values with Android Keystore
  backed keys, but it still depends on the device's Android security model.
- It does not protect against a compromised device, extracted app process
  memory, or a malicious process with equivalent privileges.
- It is not a replacement for full database encryption.

## Optional App Lock Model

App lock is optional and disabled by default. Users can enable or disable it
from the Settings screen under the Security section.

The app-lock implementation is split by responsibility:

- `:core:security` owns `AppLockAuthenticator`, `AppLockManager`, and app-lock
  state models.
- `:app` owns `AndroidBiometricAppLockAuthenticator`, which adapts AndroidX
  Biometric APIs.
- `AppLockGate` wraps protected app content in `PocketLedgerAdaptiveApp`.
- Settings UI calls `AppLockManager.setAppLockEnabled`.

When app lock is enabled, Android system authentication is required before
protected app content is composed. The Android adapter uses `BiometricPrompt`
with `BIOMETRIC_STRONG or DEVICE_CREDENTIAL`, so device credential fallback is
allowed when the device supports it.

Authentication behavior:

- On app launch, `AppLockGate` initializes lock state. If the stored
  `BiometricUnlockEnabled` value is true and authentication is available, the
  app starts locked.
- On app resume, `MainActivity.onStart()` calls
  `AppLockManager.onAppForegrounded()`. If app lock is enabled and content was
  unlocked, the manager returns to the locked state.
- `AppLockGate` displays only a minimal lock screen while locked, loading, or
  authenticating.
- Successful authentication sets state to unlocked and protected content is
  composed.
- Cancelled, failed, or errored authentication keeps state locked and protected
  content hidden.
- The manager avoids starting a new authentication request while already
  authenticating.

Unsupported-device behavior:

- If Android reports no suitable system authentication support, no enrolled
  biometric or credential, unsupported credential fallback, or unknown
  availability, app lock cannot be enabled.
- If the app previously stored app lock as enabled and the device later reports
  authentication as unavailable, the stored enabled flag is cleared.
- The core app remains usable with app lock disabled.

App lock limits casual access to the UI, but it does not replace OS-level device
security. It does not encrypt the Room database, prevent screenshots, prevent
screen recording, protect against malware with screen or accessibility access,
or protect a device with weak or shared credentials.


## Android Backup And Data Extraction Policy

T-E10-06 (#227) keeps `android:allowBackup="true"` so Android backup plumbing
is explicit, but both configured rule files deny app-private data by default:

- `androidApp/app/src/main/res/xml/backup_rules.xml` is used by pre-Android 12
  Auto Backup behavior through `android:fullBackupContent`.
- `androidApp/app/src/main/res/xml/data_extraction_rules.xml` is used by
  Android 12+ for both `cloud-backup` and `device-transfer` behavior.

Backed up or transferred by Pocket Ledger rules:

- No app-private Pocket Ledger files are intentionally included.

Excluded by both cloud backup and device-to-device transfer rules:

- `pocket-ledger.db` and SQLite sidecars: `pocket-ledger.db-shm`,
  `pocket-ledger.db-wal`, and `pocket-ledger.db-journal`.
- `pocket_ledger_sensitive_prefs.xml` encrypted sensitive preferences.
- App-private files, databases, shared preferences, external app files, and
  device-protected storage domains.
- Cache, code-cache, no-backup, logs, temp, debug, generated, and report
  folders when such folders exist under app-controlled domains.

The ledger database contains personal finance data and is not encrypted by
Pocket Ledger, so it is excluded from automatic cloud backup and device transfer
until a separate #81 backup-ready profile designs safe user-facing behavior.
Encrypted preferences are also excluded because their security depends on
Android Keystore state and they can contain local-only security settings or
future account/passkey values.

This #227 policy supports #7 local-data security and #129 release hardening, but
it is not a claim that #7, #81, or #129 are complete.

## AI Privacy Model

The current AI design is local and provider-based. UI and feature modules do
not call Gemini Nano, ML Kit, or other provider implementations directly. They
call `AiProviderSelector` and `AiFallbackStrategy`.

Current providers:

- `GeminiNanoAiProvider` is a compile-safe stub. It reports unavailable by
  default and returns unavailable inference results.
- `MlKitAiProvider` is a compile-safe stub. It reports unavailable by default,
  does not support summaries, and returns unavailable semantic search results.
- `RuleBasedAiProvider` is local, deterministic, and always available. It
  generates summaries from supplied local facts and ranks semantic search
  documents by local token matching.
- `NoOpAiProvider` is used when an AI capability is disabled. It returns empty
  successful results and performs no inference.

`AiProviderSelector` uses feature flags to choose a provider:

- `AiInsightsEnabled` gates monthly summary capability.
- `SemanticSearchEnabled` gates semantic search capability.

Both flags default to false in `DefaultFeatureFlags`. When disabled, `NoOp` is
selected. When enabled, unavailable or failing preferred providers fall back to
`RuleBasedAiProvider`.

Search and dashboard integration:

- Dashboard summary generation is routed through `DashboardSummaryGenerator`
  and `AiFallbackStrategy`.
- Search can expose semantic search only when the semantic flag is enabled.
  If semantic search is not available on the device, the UI falls back to
  keyword mode with an unavailable message.
- Semantic ranking, when reached, uses locally observed candidate transactions
  and local provider behavior.

Limitations:

- There is no real Gemini Nano or ML Kit inference in the current build.
- There is no remote AI provider in the current code.
- There is no model download, cloud ranking, embeddings service, or network AI
  request path in the current implementation.
- Future AI providers must be reviewed before transaction notes, merchant
  names, tags, categories, search text, or budget data are sent outside the
  device.

## Feature Flags And Fallback Behavior

Feature flags live in `:core:featureflags` and are evaluated through
`FeatureFlagEvaluator`. `LocalFeatureFlagProvider` supplies local overrides and
falls back to safe defaults.

Relevant defaults:

- `SemanticSearchEnabled`: false.
- `AiInsightsEnabled`: false.
- `PasskeyAccountFlowEnabled`: false.
- `CloudSyncEnabled`: false.
- `BackgroundJobsEnabled`: false.
- `DemoDataToolsEnabled`: false.
- `ScreenshotTestingEnabled`: false.

Disabled flags must keep the app usable. Current AI behavior selects `NoOp`
when disabled and `RuleBased` when a preferred provider is unavailable or
fails. Current app-lock behavior keeps the app usable when system
authentication is unavailable by clearing the app-lock preference and leaving
content unlocked.

## Product Event Taxonomy

Product event definitions live in `:core:analytics`. They provide typed,
provider-safe event names and approved parameter keys for future analytics,
observability, app-health, and release monitoring work.

The taxonomy does not enable production analytics collection by itself. Debug
builds may map typed events through a debug sink and the existing safe logger;
release and benchmark builds use no-op analytics behavior until a reviewed
provider integration is added.

Do not add event parameters for exact amounts, balances, merchant names, notes,
account names, category names, search text, raw IDs, emails, tokens, exception
messages, stack traces, service-account data, Firebase app IDs, or CI secrets.
See `docs/product-event-taxonomy.md` for the approved event and parameter list.

## Logging And Privacy

Logging is centralized in `:core:security`:

- Application and feature code should depend on `AppLogger`.
- `SafeAppLogger` applies a `LoggingPolicy` and `SensitiveValueRedactor`.
- Debug policy allows sanitized debug, info, warning, and error logs.
- Release policy allows sanitized warning and error logs only.
- The app composition root selects `LoggingPolicy.Debug` when
  `BuildConfig.LOGGING_ENABLED` is true and `LoggingPolicy.Release` otherwise.

The redactor masks known sensitive key-value pairs, authorization headers,
Bearer tokens, and JWT-shaped values. Current tests cover debug sanitization,
release debug suppression, sanitized warnings, sanitized error messages, and
sanitized throwables.

Do not log:

- Transaction amounts.
- Merchant names.
- Transaction notes.
- Tags or user-created categories.
- Search text or filters.
- Budget values.
- Raw AI prompts or generated sensitive content.
- Tokens, credentials, passkeys, secrets, encryption keys, or encrypted
  payloads.

Limitations:

- Redaction is a safety net, not permission to log sensitive values.
- Redaction only covers known patterns.
- Exception messages can contain user data, so callers must pass generic log
  messages and let `SafeAppLogger` sanitize throwables.

Detailed logging guidance is in `androidApp/docs/logging-policy.md`.

## Threat Model

In scope for the current implementation:

- Prevent casual access to app screens when app lock is enabled.
- Avoid composing or showing protected ledger UI while locked.
- Require Android system authentication on launch and resume when app lock is
  enabled and available.
- Keep AI processing local, no-op, or rule-based in the current build.
- Avoid remote AI/network inference in current AI paths.
- Avoid logging sensitive ledger, credential, and raw user-entered values.
- Keep incomplete AI, sync, passkey, and background capabilities disabled by
  default through feature flags.

Out of scope and known limitations:

- Rooted, compromised, or debug-instrumented devices.
- Malware with screen, accessibility, input, filesystem, or process access.
- User screenshots or screen recording. The app does not currently set a secure
  window flag.
- Full database encryption. The Room database is not currently encrypted by
  Pocket Ledger.
- Weak, shared, or compromised device credentials.
- Cloud backup or device-transfer of ledger data. Android backup rules are
  explicitly restricted by #227, but runtime restore/transfer behavior still
  depends on Android platform services and should be release-tested.
- Real Gemini Nano or ML Kit inference. Current providers are stubs.
- Remote AI or cloud sync privacy controls. Those features are not implemented.
- Multi-user profile edge cases beyond Android's normal app sandbox behavior.
- Protection against sensitive data visible in OS recents thumbnails, unless a
  future task adds explicit platform controls.

## Release And Build Security Notes

Release builds keep minification enabled. `:app` uses R8 through the release
build type.

AndroidX Security Crypto introduces Google Tink transitively:

```text
androidx.security:security-crypto:1.1.0
        -> com.google.crypto.tink:tink-android:1.8.0
```

Tink bytecode references annotation-only classes from Error Prone and JSR-305.
The project resolves those release R8 missing-class references with the real
annotation artifacts from the version catalog:

- `com.google.errorprone:error_prone_annotations`
- `com.google.code.findbugs:jsr305`

Those artifacts are scoped as `compileOnly` in `:app` and `:core:security`.
This keeps R8's classpath complete without packaging annotation-only jars as
release runtime dependencies.

The app does not use catch-all warning suppression rules, does not disable
minification, and does not add broad unrelated keep rules for this issue. If R8
generates future missing-class rules, review whether each missing class is an
annotation metadata dependency, an optional runtime integration, or a real
missing runtime dependency before adding rules.

## Developer Checklist

Before adding or changing security/privacy behavior:

- Do not log transaction amounts, merchant names, notes, tags, budgets, search
  text, raw AI prompts, credentials, tokens, or secrets.
- Do not route AI data to a network provider without an explicit product and
  security decision.
- Do not bypass `AppLockGate` for protected app screens.
- Do not store app lock secrets, passwords, or PINs in plain text.
- Prefer Android platform authentication over custom credential systems.
- Store sensitive preferences through `SensitivePreferences`, not raw
  `SharedPreferences`, DataStore, Room, or logs.
- Keep feature flags disabled by default for incomplete or optional security
  and privacy behavior.
- Keep fallback behavior safe and usable when providers, devices, or flags are
  unavailable.
- Add tests for new security, privacy, logging, AI fallback, and app-lock
  behavior.
- Update this document when implemented security behavior changes.

## User-Facing Limitations

Pocket Ledger currently stores ledger data locally on the device. The optional
app lock helps prevent casual access to app screens by requiring Android system
authentication, but it does not encrypt the ledger database and does not replace
the security of the device lock screen.

Current AI behavior is local, disabled by default, stubbed, or rule-based. The
app does not currently send ledger data to a remote AI service.

Logs are designed to be sanitized and must not include personal finance data,
credentials, or raw user text. Logging redaction reduces risk but does not make
it acceptable to log sensitive values.

Cloud backup and device transfer rules are explicitly locked down by #227 to
exclude app-private ledger data and local-only sensitive state. This remains a
privacy-safe default until a separately reviewed optional backup-ready profile
from #81 exists.
