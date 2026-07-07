# Future Extension Contracts

This index satisfies #142 by defining future extension contract boundaries in
documentation only. It does not add Kotlin interfaces, Gradle modules,
backend APIs, feature flags, UI routes, billing integrations, OCR providers, or
sync implementations.

## Extension Documents

- #143: [Cloud sync extension](cloud-sync-extension.md).
- #144: [OCR and receipt import](ocr-receipt-import.md).
- #145: [Export and accountant path](export-accountant-path.md).
- #146: [Monetization and entitlement](monetization-entitlement.md).
- #141: [Future product growth plan](../future-growth.md).

## Shared Contract Principles

Future extension work must preserve these boundaries:

- Feature flag gating: incomplete or optional capabilities stay disabled by
  default and must fail closed in release builds.
- Local-first source of truth: Room/local repositories remain the user-facing
  read/write source unless a reviewed ADR changes that model.
- Privacy/security review: any network, file sharing, receipt image, export,
  account, billing, analytics, or backup change must update the privacy policy,
  Play Store readiness docs, security model, and release checklist.
- Background work boundary: long-running work should use the existing
  `:core:background` scheduling vocabulary and should not block UI flows.
- Data migration boundary: schema changes require migration tests, rollback
  notes where applicable, and release checklist updates.
- Debug/release separation: debug overrides and diagnostics must not be routable
  or active in release builds.
- Testing requirement: deterministic fakes should come before production
  adapters; tests must cover offline, disabled, error, privacy, and release
  separation paths.

## Extension Table

| Extension | Issue | Proposed boundary | Required prerequisite | MVP status | Implementation status |
| --- | --- | --- | --- | --- | --- |
| Cloud sync | #143 | Sync adapter, remote API, conflict resolver, background worker. | Account/profile opt-in, encryption/recovery model, backend contract. | Out of MVP. | Not implemented. |
| OCR/receipt import | #144 | Receipt input, OCR provider, parser, candidate mapper, user confirmation. | Permission strategy, on-device provider decision, attachment/deletion policy. | Out of MVP. | Not implemented. |
| Export/accountant | #145 | Export request, formatter, redaction/filtering, share/save handoff. | Format decision, privacy UX, redaction policy, file sharing policy. | Out of MVP. | Not implemented. |
| Monetization/entitlement | #146 | Entitlement provider, billing adapter, local cache, feature-gate mapping. | Play Billing decision, product strategy, refund/revocation policy. | Out of MVP. | Not implemented. |

## Suggested Package And Module Direction

These are future placement hints, not implemented modules:

- Stable pure contracts can live in `:core:data`, `:core:security`, or a future
  `:core:domain` only after implementation work starts.
- Android framework adapters stay out of `:shared` and common code.
- Feature UI stays in `:feature:*` modules and does not depend on `:app`.
- Backend DTOs must not leak into UI or domain public APIs.
- Desktop demo code should consume shared pure logic only after a deliberate KMP
  extraction, not Android-only modules.

## Closure Checklist

- [x] #143 cloud sync extension path documented.
- [x] #144 OCR and receipt import path documented.
- [x] #145 export and accountant/freelancer path documented.
- [x] #146 monetization and entitlement path documented.
- [x] #142 index links all child docs and marks implementation as future-only.
- [x] #141 future growth plan links all child contracts and recommends next
  implementation candidates.

## Non-Goals

- No cloud sync implementation.
- No account/passkey/backend implementation.
- No OCR, camera, import, or attachment implementation.
- No export UI, file writer, accountant workflow, or invoicing implementation.
- No Play Billing, payments, product IDs, entitlement checks, or paywall.
