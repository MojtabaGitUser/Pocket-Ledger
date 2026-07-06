# Optional Backup-Ready Profile Design

Pocket Ledger does not currently implement an account, passkey sign-in, cloud
sync, or backup-ready profile flow. This document records the repository-ready
design constraints for #81 and explains why #227 keeps Android backup and
device-to-device transfer denied by default for ledger data.

## Current Status

- No user account or server-backed profile exists.
- `PasskeyAccountFlowEnabled` and `CloudSyncEnabled` feature flags default to
  disabled.
- Sensitive preference keys for future passkey/session state are defined, but
  no current flow writes production account credentials or sessions.
- Android Auto Backup and Android 12+ data extraction rules exclude
  `pocket-ledger.db`, SQLite sidecars, encrypted sensitive preferences,
  app-private files, logs, caches, temp files, debug artifacts, generated
  reports, and external app files.
- Ledger records are app-private Room data, but the Room database is not
  encrypted by Pocket Ledger.

The current status is planned/partial for #81. It is not a completed
backup-ready profile implementation.

## Requirements Before Enabling Ledger Backup

A future backup-ready profile must define and implement all of the following
before ledger data can be included in automatic backup, device transfer, cloud
sync, or restore behavior:

- Explicit opt-in from the user before any ledger backup or restore path is
  enabled.
- A documented encryption strategy for ledger backup payloads, including key
  ownership, key rotation, restore-device behavior, and failure recovery.
- A clear account/passkey/backend contract if backup depends on a
  server-backed profile.
- A recovery model for lost devices, lost passkeys, deleted accounts, and
  incompatible app versions.
- A threat model covering compromised devices, transferred device state,
  account takeover, backup replay, and local database extraction.
- Device-to-device transfer behavior that does not silently move local-only
  security state or mislead users about account-backed recovery.
- Android Auto Backup policy updates that name exactly which files are included
  and why they are safe to include.
- Privacy policy, Play Store Data Safety, security model, and release checklist
  updates before public release.
- Tests proving disabled-by-default behavior, opt-in gating, restore handling,
  and failure paths.

## Relationship To #227

#227 intentionally denies app-private ledger backup and transfer until #81 is
implemented. That policy is correct for the current product because ledger
records contain personal finance data and the app does not yet provide a
user-facing encrypted backup, account recovery, or restore contract.

When #81 is implemented, `backup_rules.xml` and `data_extraction_rules.xml`
must be re-reviewed. Any inclusion rule must be narrow, documented, tested, and
traceable to the implemented backup-ready profile behavior.

## Non-Goals For Current Release Readiness

- No cloud sync.
- No passkey account flow.
- No backend account service.
- No Play Integrity enforcement.
- No import/export backup file.
- No production ledger database encryption claim.
- No automatic restore of local ledger records.

## Traceability

- #81: profile design documented; implementation remains future work.
- #227: deny-by-default backup and transfer policy remains active.
- #7: sensitive local data security is supported by encrypted preferences,
  app-private storage, app lock, safe logging, and explicit backup exclusions.
- #129: release review must confirm backup/profile claims before release.
