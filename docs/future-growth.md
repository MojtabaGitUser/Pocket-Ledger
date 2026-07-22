# Future Product Growth Plan

E-21 plans future Folentra product growth without implementing future
features. The current product remains a local-first Android MVP with private
local ledger storage, explicit deny-by-default Android backup/data extraction
rules, local or rule-based AI behavior, and no account, cloud sync, OCR, export,
billing, entitlement, passkey, or backend flow.

This document covers #141 and summarizes the technical contract work in #142.
The detailed extension paths live under `docs/future/`.

## Purpose

E-21 defines where future expansion can attach to the existing architecture so
future work does not blur MVP boundaries. It gives maintainers a shared map for
cloud sync, OCR/receipt import, export/accountant workflows, and monetization
without adding production code or user-facing claims.

## MVP Boundary

Current MVP behavior:

- Ledger data is stored locally in the app-private Room database.
- Repository APIs are local-first and expose `SyncState.localOnly()` today.
- Sensitive preferences use AndroidX Security Crypto and Android Keystore.
- Android backup and device transfer exclude app-private ledger data by default.
- AI behavior is local, unavailable-provider shell, no-op, or deterministic
  rule-based fallback.
- Debug diagnostics are not part of release navigation.

Intentionally out of MVP:

- Cloud sync and remote backup.
- Account, passkey, backend profile, and recovery flows.
- OCR, camera capture, receipt file import, and attachment persistence.
- Export UI, accountant dashboards, invoicing, and tax workflows.
- Payments, subscriptions, Play Billing, and production entitlement checks.

## Product Growth Phases

| Phase | Scope | Entry criteria | E-21 guidance |
| --- | --- | --- | --- |
| Phase 0 | Current local-first MVP | Local ledger, dashboard, search, transactions, app lock, private insights. | Preserve this as the free baseline. |
| Phase 1 | Release hardening and portfolio readiness | Security, privacy, Play Store, accessibility, performance, desktop demo docs. | Keep claims accurate and sample-safe. |
| Phase 2 | Optional backup/account foundation | Explicit user opt-in, account/passkey design, encrypted recovery model. | Use `docs/backup-ready-profile.md` before changing #227 rules. |
| Phase 3 | Optional cloud sync | Account foundation, backend contract, conflict policy, pending change queue. | Follow `docs/future/cloud-sync-extension.md`. |
| Phase 4 | OCR/import/export productivity workflows | Permission strategy, local provider choices, share UX, redaction policy. | Follow OCR and export docs before UI work. |
| Phase 5 | Monetization/entitlement | Proven user value, billing decision, entitlement source of truth. | Follow `docs/future/monetization-entitlement.md`; do not gate core MVP too aggressively. |

## Prioritization Matrix

| Extension | User value | Privacy risk | Complexity | Backend dependency | Release risk | Portfolio value |
| --- | --- | --- | --- | --- | --- | --- |
| Cloud sync | High for multi-device users | High | High | High | High | High if implemented safely |
| OCR/receipt import | Medium-high for faster entry | Medium-high | Medium-high | Low if on-device only | Medium | High demo value |
| Export/accountant path | High for freelancers/accountants | Medium | Medium | Low for local export | Medium | High practical value |
| Monetization/entitlement | Business value, indirect user value | Medium | High | Medium-high | High | Medium unless paired with strong paid surfaces |

Recommended next implementation candidates:

1. Simple local export spike, after a separate export privacy/redaction design
   issue, because it has high user value and can stay offline.
2. Backup/account foundation design validation, because it is a prerequisite
   for safe cloud sync and changes to Android backup policy.
3. OCR provider proof-of-concept only after permission, deletion, and manual
   confirmation rules are accepted.

Do not start monetization implementation until there is a clear paid feature
with enough user value to justify entitlement complexity.

## Cross-Cutting Constraints

Future extensions must:

- Be optional and feature-flagged until production-ready.
- Keep the local database as source of truth unless a specific ADR changes it.
- Avoid sending ledger data, receipt images, exports, prompts, or analytics to a
  network service without explicit user opt-in and documentation updates.
- Keep debug overrides out of release builds.
- Add deterministic fakes and privacy regression tests before integration.
- Update privacy policy, Play Store readiness, security model, release
  checklist, and backup rules when behavior changes.

## Traceability

| Issue | Document | Status |
| --- | --- | --- |
| #141 | This future growth plan | Documentation complete; no implementation added. |
| #142 | `docs/future/extension-contracts.md` | Contract index links child paths. |
| #143 | `docs/future/cloud-sync-extension.md` | Future-only cloud sync path. |
| #144 | `docs/future/ocr-receipt-import.md` | Future-only OCR/import path. |
| #145 | `docs/future/export-accountant-path.md` | Future-only export/accountant path. |
| #146 | `docs/future/monetization-entitlement.md` | Future-only monetization/entitlement path. |

## Decision Log And Open Questions

- Decision: Do not implement future features in E-21; document boundaries only.
- Decision: Keep #227 deny-by-default backup rules until a real optional
  backup-ready profile is implemented.
- Decision: Favor local-first extension designs and deterministic fakes.
- Open question: Which account/passkey/backend model, if any, should own cloud
  sync identity and recovery?
- Open question: Should receipt OCR remain strictly on-device or allow an
  explicit opt-in cloud provider in a later phase?
- Open question: Which export formats are required for real accountant use in
  the first implementation pass?
- Open question: Which paid surfaces have enough proven value to justify Play
  Billing and entitlement complexity?

## Non-Goals

- No production code.
- No module additions.
- No placeholder Kotlin interfaces.
- No backend, account, billing, OCR, export, or cloud sync implementation.
- No Play Store claim that future features are shipped.
