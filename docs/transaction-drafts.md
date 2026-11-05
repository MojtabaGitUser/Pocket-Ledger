# Transaction drafts

Open Settings → Transaction drafts → New receipt draft. Enter receipt details, save an incomplete
draft, reopen and edit it, then choose Confirm and add transaction and explicitly confirm the dialog.
Discard requires a separate confirmation. Closing the editor through its button or Android Back
saves the draft. Edits also queue local saves and checkpoint saved screen state; an abrupt force-stop before a write finishes can still lose the newest keystroke.
Photo capture and on-device OCR now feed this same flow; see [receipt-scanner.md](receipt-scanner.md).

The existing transaction feature owns the screen and ViewModel. AppGraph supplies a local draft
repository. The repository reuses shared transaction validation and the Room KMP database owns
atomic confirmation. No new library or network service is introduced. core:data now depends on
shared to validate confirmation using the same financial rules as manual entry.

Schema 3 adds transaction_drafts through migration 2→3, preserving migrations from schema 1.
Drafts are isolated from all transactions queries, including dashboards, budgets, search and CSV.
Incomplete text is allowed in drafts; only valid confirmed data becomes a normal ledger transaction.
On confirmation, the DAO inserts a transaction and stores a confirmation marker in one database
transaction. The transaction ID is stable per draft. Concurrent confirmations and retries return
the same ID; deleting the ledger transaction does not allow a retry to recreate it.
Financial draft fields are cleared after confirmation, leaving the ID, source, timestamps and marker.
This prevents duplicate confirmation of one draft, not duplicate receipts entered as separate drafts.
Cross-source receipt matching belongs to the later ingestion/reconciliation work.

Drafts stay in the existing app-private database and inherit its backup exclusions. They are not
separately encrypted. Receipt scanning uses temporary image cache; raw OCR text is not persisted. See receipt-scanner.md for retention and SDK diagnostics.
Pending drafts can be discarded from the review screen. Clearing app data removes all drafts and
confirmation markers. Removing a confirmed ledger transaction leaves no hidden draft financial copy.

Tests cover incomplete draft persistence across database reopen, ledger isolation, schema migration,
concurrent confirmation, rollback on failed insert, retry after deletion, input validation and UI
state after errors. Device checks still needed before release: update an installed schema-2 build,
save/reopen/edit/confirm/discard, TalkBack, 200% fonts, rotation and process restart.

The receipt scanner now uses this stable draft ID and the same review/confirmation path.

## Physical-device verification — 2026-11-01

Two instrumented tests passed on Samsung SM-S906W running Android 16 using
`DraftTransactionDeviceTest` and a separate temporary Room database, deleted after each test.
The real Compose route and local repositories were exercised: incomplete save, reopen and edit,
cancel confirmation without a ledger write, explicit confirmation, and discard. A second test
verified persistence after database close/reopen and idempotent confirmation retries.

Command (from the repository root):
`androidApp\gradlew.bat -p androidApp :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mojtaba.folentra.draft.DraftTransactionDeviceTest`

This does not verify app process-death restoration, schema-2 upgrade on the physical device,
TalkBack, 200% fonts, rotation, or OCR. Those checks remain separate release follow-ups.
