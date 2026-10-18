# Local CSV export

Settings → Export transactions → select inclusive start/end dates → Preview export → Save CSV.
Dates use the device time zone. Preview freezes an in-memory snapshot; changes to the ledger
after preview do not change the saved rows. Changing export options requires a new preview.
Merchant names and notes are excluded by default and require separate opt-ins.

The v1 schema is `date,type,amount,currency` with optional `merchant,note` columns.
UTF-8 with BOM, CRLF records, quoted/escaped cells and ISO dates support spreadsheet use.
Amounts preserve the ledger's existing two-decimal fixed-point convention; currencies are not
converted and transaction type is preserved. IDs, categories, tags and account metadata are
not exported. Formula-like user text is prefixed with an apostrophe for spreadsheet safety.

Generation happens off the main thread. Rows stream to the selected document, without an app
cache copy or network request. The repository currently loads the selected rows into memory;
this is not a paginated export. Formatter coverage includes a 10,000-row fixture.
The Android document picker may offer cloud providers: selecting one can upload financial data.
Files are not encrypted and remain at the user-selected destination until the user deletes them.
No storage permission is added. No financial rows or destination URIs are logged.

Rotation retains the preview via ViewModel. Process death discards the snapshot and requires
preview again; the destination is not opened without it. Cancellation keeps the preview for retry.
A failed provider write can leave a partial file; the UI instructs the user to remove it and retry.

Validation: formatter tests cover private-field omission, Persian text, escaping, formula prefixes,
amounts, DST boundaries and large snapshots. ViewModel tests cover empty state, snapshot stability,
cancellation, invalidation, missing snapshots and provider failure.
Manual release gate: verify the system document picker, local/cloud destinations, rotation,
TalkBack and 200% font scale on a device before publishing. This is export, not backup/restore.
