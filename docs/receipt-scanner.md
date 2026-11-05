# On-device receipt scanning

## Flow and boundaries

Settings → Transaction drafts → Take photo / Choose from gallery → local OCR → existing editable
draft → explicit confirmation. Manual draft creation remains available. Scanner actions also appear
inside a draft. A rescan only fills empty fields: clearing a field explicitly requests a new suggestion.
Category, transaction type, notes and existing currency/date/amount/merchant are preserved.

The app graph injects ReceiptTextRecognizer into the existing transaction route/ViewModel.
core:data owns SDK isolation, image loading, plain structured OCR models, deterministic parsing and
draft mapping. The transaction feature owns Activity Result launchers, saved screen state, editing and
validation. Existing LocalDraftTransactionRepository and atomic DraftTransactionDao confirmation are
reused. No new schema or parallel transaction creation path is introduced.

## Images and recognition

System TakePicture and PickVisualMedia contracts are used. No embedded camera or broad storage
permission is needed. The camera receives a scoped FileProvider URI under cache/receipts.
Photo Picker automatically falls back to system document selection on older supported devices.
Cancellation, missing camera application, denied access and unreadable images leave manual entry available.

The bundled com.google.mlkit:text-recognition:16.0.1 Latin model handles English/French printed text.
The input is copied on Dispatchers.IO with a 20 MiB limit, decoded with a power-of-two sample so its
longest dimension is at most 2400 pixels, and transformed for all eight EXIF orientations. At most two
sampled bitmaps coexist during orientation. Oversized/unreadable files fail with a recoverable message.
Recognition releases its client and bitmap after the SDK task completes, including cancellation;
parsing runs on Dispatchers.Default. No full text or image is logged or stored in the ledger.

Normal processing/cancellation removes temporary copies/captures. Interrupted files older than 24 hours
are pruned next time the receipt screen opens. OS cache eviction can remove them earlier. Gallery
originals are untouched. No receipt image archive or OCR history is kept.
Recognition needs no model download. A cloud-backed picker image may require network retrieval.
ML Kit diagnostics are separate from image/text processing; see the updated privacy policy and
https://developers.google.com/ml-kit/android-data-disclosure. Review the release's Data safety answers
for the added SDK before publishing; the application does not implement a custom telemetry toggle for it.

## Extraction decisions

- Amounts are parsed into exact Long minor units through BigDecimal, never Double.
- Final-total labels outrank general TOTAL / AMOUNT PAID. Subtotals, tax, change, cash tendered,
  discounts, tips, suggested amounts and loyalty balances are excluded. Equal-ranked conflicting
  totals remain empty. No unlabelled or largest-value fallback. Refund receipts require manual entry.
- Spatially aligned fragments (label left, value right) and adjacent numeric-only lines are supported.
- Merchant uses early positioned, plausible alphabetic business names, excluding generic labels,
  addresses and identifiers. It is always marked uncertain; missing names stay empty.
- Dates use valid, unambiguous numeric or English/French month formats. Ambiguous numeric,
  conflicting and future dates are left empty. OCR never substitutes today's date.
- Dates map from LocalDate to the device zone's start of day. The shared DateSelector converts
  Material DatePicker UTC dates to local calendar dates before persistence.
- Only explicit CAD/USD evidence fills a blank currency; '$' alone is insufficient.
- Fields carry nullable values, categorical evidence strength, evidence text and a review flag.
  Raw evidence exists in memory only. The review UI shows field confidence and inline validation;
  every result requires user verification. Scores rank evidence, not probability of OCR correctness.

Limits: printed Latin receipts, two decimal places, explicit year, simple supported layouts. Handwriting,
long/tiny receipts, tax-inclusive layouts, dual-currency receipts and complex tips can require manual
correction. Conservative missing values are intentional. No cross-receipt duplicate detection exists.

## Recovery and confirmation

Every correction checkpoints the selected draft in SavedStateHandle and queues a serialized local
draft save. Database failures preserve the editor and show a retry message. Camera URI and the target
draft ID use saved Activity Result state, so a late result cannot be applied to another selected draft.
A recreated interrupted OCR operation asks for a new image instead of silently repeating it. Restored
draft data never triggers transaction confirmation. Force-stop/device loss before a write completes
is not guaranteed to persist the newest keystroke; normal Android saved-state recreation is supported.

ViewModel busy gating prevents concurrent confirmations. A stable 'draft:<id>' transaction primary key
and Room transaction insert the ledger row and mark confirmation atomically. Retries return that ID.
Financial draft fields are cleared after confirmation; a tombstone prevents recreation even if the ledger
transaction is later deleted. The guarantee is at most one transaction per draft, not per photograph:
scanning the same receipt into distinct drafts can produce separately confirmed transactions.

## Verification

Parser unit fixtures cover Canadian taxes, French accents, subtotal/total, cash/change, conflicts,
discounts/tips, missing values, decimal/group separators, ambiguous dates, invalid/future dates,
spatial fragments, refunds, currency ambiguity and merge/date-zone preservation.
ViewModel tests cover OCR success/failure/empty results, manual fallback, corrections, saved-state
restoration, validation, save retry and rapid confirmations. Existing real Room concurrency/migration
tests remain the persistence guard.

DraftTransactionDeviceTest includes a synthetic bitmap → actual bundled ML Kit → ViewModel →
persisted draft → editable Compose review → one confirmed transaction, followed by replaying confirmation.
No real customer receipt fixtures are committed.

Commands (from the repository root):
- androidApp\gradlew.bat -p androidApp :core:data:testDebugUnitTest :feature:transaction:testDebugUnitTest :app:assembleDebug
- androidApp\gradlew.bat -p androidApp :core:database:desktopTest :app:lintDebug
- androidApp\gradlew.bat -p androidApp :app:assembleDebugAndroidTest
- Install debug and test APKs with adb install -r, then run the focused class using am instrument.
  Direct instrumentation preserves the existing debug app installation/data, unlike some connected-test cleanup configurations.

Actual run results and real-camera acceptance status must be recorded after execution, not inferred from code.

### Executed results — November 1, 2026

- Final Debug APK and AndroidTest APK built successfully.
- core:data: 58 tests, zero failures (including 15 parser cases).
- feature:transaction: 61 tests, zero failures (including 7 draft workflow cases).
- core:database desktop: 10 tests, zero failures, including real SQLite concurrency and migration.
- app lintDebug: success, zero errors, 67 warnings; no warning locations in the new receipt/draft files.
- Samsung SM-S906W / Android 16: all five focused instrumentation tests passed.
  The initial run passed with Wi-Fi disabled and mobile data set to 0; original enabled settings were
  restored in a finally block. The final APK/test APK were then installed with adb install -r and all
  five tests passed again (9.798 seconds). No uninstall or app-data clearing was used.
- Device tests exercised real bundled OCR and EXIF rotation, invalid-image rejection, editable
  review/confirmation, persistence after reopen, and idempotent replay. Fixtures were synthetic.
- git diff --check passed.

Real camera/photo-picker acceptance with a real receipt is awaiting user verification. TalkBack,
200% fonts, a physical process-kill during camera capture, and a broad receipt-quality corpus have not
been manually verified. Do not label those checks passed based on synthetic OCR tests alone.
