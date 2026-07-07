# Export And Accountant/Freelancer Path

This document satisfies #145 by defining a future export and
accountant/freelancer path without implementing export UI, file writers, share
flows, accountant dashboards, invoicing, or tax workflows.

## Why Future-Only

Export can intentionally move personal finance data out of app-private storage.
That is useful, but it needs explicit share UX, redaction choices, file storage
policy, format stability, and privacy documentation before implementation.
Accountant and freelancer workflows add more domain decisions and should not be
bundled into a simple export spike without a product decision.

## Safe Simple Export Versus Product Expansion

Safe simple export:

- User-initiated only.
- Local generation only.
- Clear date range, category/tag, and data-field scope.
- Explicit save/share handoff through Android platform UI.
- Redaction options before file creation.

Accountant/freelancer expansion:

- Client/project tagging.
- Tax category mapping.
- Recurring export templates.
- Invoice/accountant handoff.
- Multi-report workflows and more durable audit metadata.

The first implementation should prefer safe simple export. Accountant and
freelancer flows remain future product expansion.

## Preconditions

Before implementation starts, define and review:

- Export format decision and schema stability.
- Privacy/share UX that warns users before data leaves app-private storage.
- File storage and share target policy.
- PII and sensitive-field redaction options.
- Date range, category, tag, transaction type, and account/source filters.
- Audit metadata for generated exports without logging sensitive values.
- Currency, amount, timezone, and locale formatting rules.
- Large dataset memory/performance behavior.
- Privacy policy, Play Store readiness, and release checklist updates.

## Proposed Architecture

Future export should be a local pipeline:

```text
Export request -> local repository read -> filtering/redaction
               -> formatter -> share/save adapter
```

Boundaries:

- Export request boundary: describes user-selected scope and output format.
- Export formatter boundary: converts local domain models to CSV, JSON, or PDF
  summary without depending on Compose UI.
- Redaction/filtering boundary: removes or masks selected fields before
  formatting.
- Share/save boundary: hands generated content to Android platform storage or
  share UI; it does not own business logic.
- Feature flag gate: export workflows stay disabled until release-ready.

## Suggested Formats

- CSV: first choice for spreadsheet and accountant handoff. Needs stable column
  order, encoding, escaping, and locale-independent numeric values.
- JSON: structured backup/interchange format for future tooling, not a silent
  cloud backup replacement.
- PDF summary: future polished report output, not the first implementation
  unless product scope requires it.

## Suggested Contract Shapes

These are documentation-only shapes, not Kotlin APIs:

```text
ExportRequest
- format: csv, json, pdfSummary
- scope
- redactionPolicy
- requestedAt
- destinationHint: share, save, preview

ExportFormat
- csv
- json
- pdfSummary

ExportScope
- dateRange
- transactionTypes
- categoryIds
- tagIds
- includeBudgets
- includeNotes
- includeMerchantNames

ExportRedactionPolicy
- includeExactAmounts
- includeMerchantNames
- includeNotes
- includeTags
- maskPrivateText
- aggregateOnly

ExportResult
- status: success, cancelled, failed
- fileName
- mimeType
- byteSize
- recordCount
- warningCount

ExportAuditMetadata
- generatedAt
- format
- scopeSummary
- redactionSummary
- recordCount
```

## Privacy And Security Constraints

- Export is explicit user action only.
- Do not export silently in background.
- Do not log exported rows, exact amounts, notes, merchant names, file contents,
  destination URIs, or share targets.
- Do not store generated export files inside app-private storage longer than
  needed for the user action unless a retention policy is added.
- Make redaction choices visible before generation.
- Update privacy policy and Play Store disclosures if export becomes available.

## Accountant/Freelancer Future Path

Future product expansion may add:

- Client and project tagging.
- Tax category mapping and review state.
- Recurring export templates.
- Accountant handoff packages.
- Invoice reconciliation hints.
- Quarterly summary reports.

These are non-MVP features. They should be designed after basic export proves
format, redaction, and share behavior.

## Testing Strategy

Future implementation should include:

- Deterministic export fixtures for CSV and JSON.
- Redaction tests for notes, merchant names, tags, categories, exact amounts,
  and aggregate-only output.
- Encoding, escaping, timezone, date, currency, and line-ending tests.
- Large dataset tests using existing deterministic benchmark fixtures.
- Share/save cancellation and failure tests.
- Privacy regression tests for logs, analytics, diagnostics, and generated
  artifact retention.

## Non-Goals

- No export UI implementation now.
- No file writer or Android share integration now.
- No accountant dashboard now.
- No invoicing now.
- No tax advice or regulated accounting claim now.
