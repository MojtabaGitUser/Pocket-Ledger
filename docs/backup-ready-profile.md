# Optional Backup-Ready Profile Foundation

Pocket Ledger now implements the local-first foundation for #81. The feature is
intentionally conservative: users can see and eventually opt in to a
backup-ready profile state, but ledger data remains excluded from Android cloud
backup and device-to-device transfer until a reviewed encrypted backup and
restore pipeline exists.

## Implemented Scope

Implemented pieces:

- `BackupReadyProfileManager` in `:core:security` owns the backup-ready profile
  state machine.
- `BackupReadyProfileState` exposes local-only, account-required,
  backup-pending, and backup-ready states.
- Explicit opt-in state is stored through `SensitivePreferences` using typed
  keys in `DefaultSensitivePreferenceKeys`.
- Settings shows a `Backup-ready profile` row under Security and keeps the
  switch disabled until account/passkey prerequisites are ready.
- App graph wiring derives prerequisites from feature flags and sensitive
  passkey/session state.
- Unit tests cover default local-only behavior, opt-in persistence, prerequisite
  gating, ready-state calculation, and opt-out cleanup.
- Android backup and data extraction XML rules remain deny-by-default for ledger
  data and sensitive preferences.

## State Machine

| State | Meaning | User-facing behavior |
| --- | --- | --- |
| `LocalOnly` | No backup-ready opt-in is stored. | Ledger data stays local and excluded from Android backup. |
| `WaitingForAccountIdentity` | User opted in but passkey/account prerequisites are missing. | Settings explains that a passkey account profile is required. |
| `WaitingForEncryptedBackupPipeline` | Account identity is present but cloud/encrypted backup is not enabled. | Opt-in is remembered, but no ledger backup or restore path runs. |
| `ReadyForEncryptedBackupPipeline` | Account identity and cloud-sync feature gate are present. | The profile is ready for a separately reviewed encrypted backup pipeline. |

`androidBackupIncludesLedgerData` is always false in the current
implementation. This makes the #227 backup policy explicit in code, not only in
XML comments.

## Prerequisites

A backup-ready profile requires all of these signals before it can move beyond
local-only behavior:

- User opt-in stored in `BackupReadyProfileOptInAccepted`.
- Policy version stored in `BackupReadyProfilePolicyVersion`.
- Passkey account flow feature flag enabled.
- Stored passkey credential id.
- Stored account session token.
- Cloud sync feature flag enabled before an encrypted backup pipeline can be
  considered ready.

The current production defaults keep `PasskeyAccountFlowEnabled` and
`CloudSyncEnabled` disabled. That means normal app builds remain local-first.

## Relationship To Android Backup And #227

#81 does not loosen Android backup or device-transfer rules. The XML policy from
#227 remains correct:

- `androidApp/app/src/main/res/xml/backup_rules.xml` denies pre-Android 12 Auto
  Backup for app-private data.
- `androidApp/app/src/main/res/xml/data_extraction_rules.xml` denies Android 12+
  cloud backup and device-transfer extraction for app-private data.
- `pocket-ledger.db`, SQLite sidecars, encrypted preferences, logs, caches,
  temp files, debug files, generated reports, and external app files remain
  excluded.

Any future change that includes ledger data in automatic backup must be narrow,
reviewed, tested, and tied to a real encrypted payload and restore contract.

## Security Rules

- Do not store backup profile state outside `SensitivePreferences`.
- Do not log account identifiers, credential ids, session tokens, backup policy
  payloads, restore payloads, or encryption material.
- Do not treat Android Auto Backup as cloud sync.
- Do not include ledger data in `backup_rules.xml` or
  `data_extraction_rules.xml` until encryption, recovery, and restore behavior
  are implemented and release-reviewed.
- Keep the app fully usable without an account or backup profile.
- Keep failure states user-safe: missing passkey, missing session, disabled
  cloud sync, and unavailable backend must leave the app local-only.

## Explicit Non-Goals

Not implemented by #81:

- Production cloud sync.
- Server-backed account service.
- Passkey account recovery or deletion support workflow.
- Encrypted ledger backup payload format.
- Restore flow.
- Server-side Play Integrity verdict verification.
- Inclusion of ledger data in Android backup or device transfer.
- Full Room database encryption.

## Issue Traceability

| Issue | Status | Evidence |
| --- | --- | --- |
| #81 Create an optional backup-ready profile | Complete for the local-first foundation scope | `BackupReadyProfileManager`, Settings entry, sensitive opt-in keys, prerequisite state model, tests, and this document are implemented. Ledger backup remains excluded until future encrypted backup/restore work. |
| #227 Deny-by-default backup policy | Still active | Backup XML and data extraction XML continue to exclude app-private ledger data and sensitive preferences. |
| #7 Secure sensitive local data | Supported | Backup-ready profile state uses encrypted sensitive preferences and does not weaken local-only storage. |
| #13 Optional passkey account flow | Dependency | #81 consumes passkey/account readiness signals but does not create a production backend. |

## Validation

Run from the repository root:

```powershell
.\androidApp\gradlew.bat --no-daemon :core:security:testDebugUnitTest :app:testDebugUnitTest :app:compileDebugKotlin :app:compileReleaseKotlin --console=plain --stacktrace
```
## 2026-07-20 Closure Validation For #81

The backup-ready profile foundation was re-audited as an opt-in state and policy boundary, not as a claim that ledger backup exists. The manager persists opt-in, timestamp, and policy version through `SensitivePreferences`; derives account and encrypted-pipeline prerequisites; keeps ledger data excluded from Android backup; and exposes local-only, account-required, backup-pending, and backup-ready states to Settings.

Consent evaluation is now explicitly fail-closed. A partial preference write, missing acceptance timestamp, or consent recorded under an outdated policy version resolves to `LocalOnly`, clears consent metadata from the returned state, and cannot prepare an encrypted backup. Tests cover these corrupted and outdated records in addition to opt-in, prerequisite, ready, and opt-out transitions.

Validated commands:

```powershell
.\gradlew.bat :core:security:testDebugUnitTest --tests '*BackupReady*'
.\gradlew.bat :app:testDebugUnitTest --tests '*OptionalAccount*'
.\gradlew.bat :app:assembleDebug :app:assembleRelease lintRelease
```

#81 is complete only for the local-first backup-ready profile foundation. No encrypted payload format, cloud transport, restore, recovery, retention, or account backend is implemented or implied.
