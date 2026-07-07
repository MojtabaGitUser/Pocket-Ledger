# OCR And Receipt Import Path

This document satisfies #144 by defining a future OCR and receipt import path
without implementing OCR, camera capture, file import, attachment persistence,
or transaction creation automation.

## Why Future-Only

Receipt import touches sensitive images, merchant names, line items, dates,
amounts, payment details, and sometimes location-like store data. Even a local
OCR flow needs permission design, deletion behavior, manual correction, duplicate
detection, and privacy review before it becomes product behavior.

## Preconditions

Before implementation starts, define and review:

- Image/file permission strategy for camera, photo picker, document picker, or
  share targets.
- On-device OCR provider decision and fallback behavior.
- Explicit privacy review for receipt images and parsed text.
- Manual review/edit UX before any transaction is saved.
- Import confidence model and thresholds.
- Duplicate detection for existing transactions and repeated imports.
- Attachment storage policy, including whether raw images are persisted.
- Deletion behavior for temporary images, parsed text, and saved attachments.
- Accessibility behavior for correction screens and confidence messages.
- Privacy policy, Play Store readiness, and release checklist updates.

## Proposed Architecture

Future OCR should be a pipeline with clear boundaries:

```text
Receipt input -> OCR provider -> receipt parser -> transaction candidate
              -> user confirmation/edit -> transaction repository write
```

Boundaries:

- Receipt image input boundary: receives a user-selected image/file URI or
  captured image handle without assuming permanent storage.
- OCR provider boundary: abstracts local OCR engine behavior and errors.
- Receipt parser boundary: converts OCR text blocks into structured receipt
  facts without writing transactions.
- Candidate transaction mapper: maps parsed receipt facts to a draft
  transaction candidate.
- User confirmation/edit step: user reviews and corrects amount, date,
  merchant, category, tags, notes, and recurring state before saving.
- Transaction creation boundary: uses existing transaction repository contracts
  only after user confirmation.
- Feature flag gate: receipt import remains disabled until production-ready.

## Suggested Contract Shapes

These are documentation-only shapes, not Kotlin APIs:

```text
ReceiptInput
- source: camera, photoPicker, filePicker, shareSheet
- temporaryUri: platform handle
- mimeType
- capturedAt
- retentionPolicy: temporary, userSavedAttachment

OcrResult
- textBlocks: ordered text blocks
- providerStatus: available, unavailable, failed
- confidence: import confidence value
- warnings: non-sensitive messages

ParsedReceipt
- merchantCandidate
- purchasedAtCandidate
- totalAmountCandidate
- currencyCandidate
- lineItems
- taxAmountCandidate
- paymentHint

ReceiptLineItem
- descriptionCandidate
- quantityCandidate
- amountCandidate
- confidence

TransactionCandidate
- type: expense by default unless user changes it
- amount
- date
- merchant
- note
- categoryCandidate
- tagCandidates
- source: receipt import

ImportConfidence
- level: high, medium, low, unknown
- reasons: missing total, ambiguous date, duplicate possible, parser warning
```

## Privacy And Security Constraints

- Do not upload receipt images by default.
- Avoid cloud OCR unless a later issue explicitly adds opt-in cloud behavior,
  provider disclosure, privacy policy updates, and Play Store Data Safety
  updates.
- Do not store raw receipt images unless the user explicitly chooses attachment
  persistence.
- Delete temporary files and parsed raw OCR text after the review flow unless a
  documented retention policy says otherwise.
- Do not log OCR text, merchant names, line items, exact amounts, payment hints,
  image URIs, or provider diagnostics.
- Do not save a transaction automatically from OCR output.

## Failure Modes

| Failure | Expected behavior |
| --- | --- |
| OCR unavailable | Show local fallback/disabled state; allow manual transaction entry. |
| Low confidence | Require manual correction and avoid prefilled certainty claims. |
| Duplicate possible | Warn user and show candidate match before saving. |
| Unsupported image/file | Reject safely without retaining the file. |
| Parser ambiguity | Keep candidate editable and explain the ambiguous fields. |
| Permission denied | Keep app usable and offer manual entry. |

## Testing Strategy

Future implementation should include:

- Fake OCR provider tests with deterministic text blocks.
- Fixture receipt parser tests for common receipt layouts.
- Confidence threshold tests for high, medium, low, and unknown cases.
- Duplicate detection tests against existing local transaction fixtures.
- Temporary file cleanup tests.
- Accessibility tests for manual correction and confidence/status messaging.
- Privacy regression tests proving OCR text and receipt images are not logged or
  sent to analytics.

## Non-Goals

- No OCR provider implementation now.
- No camera integration now.
- No photo picker or document picker implementation now.
- No receipt image persistence now.
- No automatic transaction save now.
- No cloud OCR provider now.
