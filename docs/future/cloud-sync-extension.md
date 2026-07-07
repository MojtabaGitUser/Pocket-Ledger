# Cloud Sync Extension Path

This document satisfies #143 by defining a future cloud sync path without
implementing cloud sync. Pocket Ledger remains local-first today. The current
app has no account, passkey, backend, remote API, cloud sync worker, sync queue,
or remote ledger storage.

## Why Future-Only

Cloud sync changes the trust model. It can move personal finance records from
app-private local storage to a remote service and can affect Android backup,
restore, account recovery, data retention, privacy policy, Play Store Data
Safety, and support obligations. It must wait until the optional backup-ready
profile and account/recovery model are real, reviewed, and tested.

## Preconditions

Before implementation starts, define and review:

- Optional account/profile flow with explicit user opt-in.
- Backend API contract and operational ownership.
- Encryption strategy for synced ledger data and recovery keys.
- Recovery model for lost device, lost passkey, account deletion, and account
  compromise.
- Conflict resolution policy for edits across devices.
- Pending local change queue and retry policy.
- Sync status model aligned with existing `SyncState` and `LocalChange` shapes.
- Data retention and deletion policy.
- Privacy policy, Play Store Data Safety, security model, and release checklist
  updates.
- Relationship to #227 backup/data extraction: ledger data stays excluded from
  Android backup until a reviewed profile intentionally changes that policy.

## Proposed Architecture

The local database remains the source of truth for feature-facing reads and
writes. Future sync should attach behind repository/data-layer boundaries:

- Local repository boundary: existing repositories continue to expose local
  `Flow` reads and suspend writes.
- Sync adapter boundary: observes pending local changes and applies remote
  changes to local persistence after validation.
- Remote API boundary: owns network DTOs, auth headers, pagination, cursors,
  and retry semantics. DTOs must not leak to UI or domain APIs.
- Conflict resolver boundary: maps divergent local/remote changes into a user
  or policy-resolved outcome.
- Background worker boundary: uses existing background scheduling concepts for
  non-blocking sync work.
- Feature flag gate: cloud sync stays disabled by default until release-ready.

Suggested future flow:

```text
Feature UI -> repository contract -> local Room source of truth
                              -> pending change tracker
                              -> sync adapter -> remote API
                              -> conflict resolver -> local Room updates
```

## Suggested Contract Shapes

These are documentation-only shapes, not Kotlin APIs:

```text
SyncAccount
- id: stable account identifier, not an email in logs
- displayState: signed out, signed in, reauth required
- encryptionState: unavailable, ready, recovery required

SyncDevice
- id: stable device identifier generated for sync
- name: user-visible device label
- lastSeenAt: timestamp
- trustState: current, revoked, unknown

SyncChange
- localId: local ledger object ID
- entityType: transaction, budget, category, tag, link
- operation: insert, update, delete, link, unlink
- changedAt: local timestamp
- payloadHash: non-sensitive integrity marker

SyncCursor
- accountId: account scope
- deviceId: device scope
- remoteCursor: opaque remote cursor
- lastAppliedAt: timestamp

SyncConflict
- entityType
- localVersion
- remoteVersion
- conflictReason: concurrent edit, delete/update, schema mismatch
- resolution: keep local, keep remote, merge, require user review

SyncStatus
- localOnly, disabled, idle, syncing, offline, authExpired, conflict,
  partialFailure, backendUnavailable
```

## Failure Modes

| Failure | Expected behavior |
| --- | --- |
| Offline | Keep local writes available; queue pending changes; show non-alarming sync status. |
| Auth expired | Stop remote requests, keep local-only behavior, require explicit reauth. |
| Conflict | Do not silently overwrite; use deterministic policy or user review. |
| Partial sync | Preserve successfully applied local state and expose retryable failure. |
| Backend unavailable | Back off, keep local features usable, avoid data loss. |
| Schema mismatch | Block unsafe remote application until migration compatibility is proven. |

## Privacy And Security Constraints

- Do not send ledger data to a server unless the user opted into sync.
- Do not log transaction descriptions, merchant names, notes, exact amounts,
  account identifiers, auth tokens, encryption keys, or raw remote payloads.
- Do not reuse Android backup as a hidden cloud sync mechanism.
- Do not include synced data in #227 backup rules until #81 is implemented and
  reviewed.
- Keep account/session credentials in `SensitivePreferences` or a stronger
  reviewed store, never raw shared preferences or logs.

## Testing Strategy

Future implementation should include:

- Fake remote sync service with deterministic responses.
- Conflict resolver unit tests for concurrent edits and deletes.
- Pending queue tests for insert/update/delete/link/unlink.
- Migration tests for synced entities and cursors.
- Offline, auth-expired, backend unavailable, and partial failure tests.
- Privacy regression tests for logs, analytics, diagnostics, and crash metadata.
- Release/debug separation tests for sync diagnostics and debug overrides.

## Non-Goals

- No backend now.
- No remote sync implementation now.
- No account/passkey implementation now.
- No Android backup inclusion now.
- No production remote storage claim now.
