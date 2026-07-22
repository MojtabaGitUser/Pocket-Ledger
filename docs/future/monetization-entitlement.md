# Monetization And Entitlement Path

This document satisfies #146 by defining a future monetization and entitlement
path without implementing payments, Play Billing, product IDs, subscriptions,
paywalls, entitlement checks, or production billing UI.

## Why Future-Only

Monetization changes product trust. It can affect privacy disclosures, Play
Console setup, refund behavior, offline access, support obligations, and user
expectations. Folentra should not add billing until paid surfaces have
clear user value and the free local-first MVP remains useful.

## Product Principles

- The current local ledger MVP remains usable.
- No ads are in current scope.
- Monetization must be privacy-first and avoid selling personal data.
- Core local transaction entry, basic budgets, basic search, and local access
  should not be paywalled too aggressively.
- Paid features should map to clear additional value, such as optional cloud
  infrastructure or advanced productivity workflows.
- Billing debug overrides must be debug-only and impossible to ship as release
  entitlement bypasses.

## Potential Paid Surfaces

Potential future paid surfaces, subject to product validation:

- Advanced export templates or accountant packages.
- Optional cloud sync and encrypted recovery infrastructure.
- Advanced receipt import/OCR usage or attachment workflows.
- Accountant/freelancer workflows such as project tagging and recurring reports.
- Premium analytics/insights that remain privacy-safe and avoid regulated
  financial advice.

Current MVP features should remain available without payments unless a separate
product decision changes the free baseline.

## Preconditions

Before implementation starts, define and review:

- Play Billing or alternate entitlement decision.
- Entitlement source of truth and server/backend requirements, if any.
- Offline entitlement grace period and local cache rules.
- Refund, revocation, chargeback, account deletion, and device migration
  behavior.
- Privacy policy and Play Store Data Safety updates.
- Feature flag integration and rollout plan.
- Support policy for billing issues.
- Debug/release separation for entitlement overrides.
- Test plan with fake billing provider and release safety checks.

## Proposed Architecture

Future entitlement should use adapter boundaries:

```text
Feature gate -> entitlement provider -> local cache
                              -> billing adapter or backend entitlement API
```

Boundaries:

- EntitlementProvider: stable app-facing contract for entitlement state.
- BillingClient adapter boundary: isolates Play Billing SDK behavior and purchase
  updates from feature modules.
- LocalEntitlementCache: stores non-sensitive entitlement state with an offline
  grace policy; it is not the only source of truth for paid access.
- FeatureEntitlement mapping: maps product capabilities to required
  entitlements without hardcoding billing products in feature UI.
- Debug entitlement override: allowed only in debug/internal builds and covered
  by release separation tests.

## Suggested Contract Shapes

These are documentation-only shapes, not Kotlin APIs:

```text
Entitlement
- id: stable entitlement identifier
- featureKey: cloudSync, advancedExport, receiptImport, accountantTools
- state
- source
- expiresAt
- graceUntil

EntitlementState
- active
- inactive
- pending
- gracePeriod
- revoked
- unknown

PurchaseState
- purchased
- pending
- refunded
- revoked
- expired
- failed

EntitlementSource
- localDebugOverride
- playBilling
- backendVerified
- cachedGrace

FeatureGate
- featureFlag
- requiredEntitlement
- unavailableReason
- releaseOverrideAllowed: false by default
```

## Privacy And Security Constraints

- Do not log purchase tokens, account identifiers, order IDs, emails, exact
  revenue, entitlement cache payloads, or billing errors that contain personal
  data.
- Do not expose billing debug screens in release builds.
- Do not enable production feature access from local-only debug overrides.
- Update privacy policy, Play Store readiness, release checklist, and support
  docs before any billing dependency ships.
- Keep paid-feature analytics coarse and privacy-safe.

## Testing Strategy

Future implementation should include:

- Fake billing provider tests for active, pending, revoked, refunded, expired,
  failed, and unknown states.
- Offline grace tests for cached entitlement behavior.
- Refund/revocation tests proving feature access is removed safely.
- Debug/release separation tests proving debug overrides are unavailable in
  release.
- Feature-gate mapping tests for each paid surface.
- Privacy regression tests for billing logs, analytics, diagnostics, and crash
  metadata.

## Non-Goals

- No Play Billing implementation now.
- No subscription product IDs now.
- No payment UI now.
- No entitlement enforcement in production now.
- No paid feature gating now.
- No ads now.
