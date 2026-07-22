# Play Feature Delivery Strategy

Folentra does not currently ship Android Play Feature Delivery dynamic
feature modules. The current product is small enough that release safety is
better served by static Gradle modules, typed feature flags, and clear module
boundaries. This document records the strategy for `T-E13-04 - Document Play
Feature Delivery strategy` and explains how future dynamic delivery should be
introduced without weakening the local-first baseline.

## Current Strategy

The app uses compile-time Gradle modules and runtime feature flags:

- `:core:featureflags` owns typed flag definitions, default values, local
  overrides, and the `FeatureFlagEvaluator` contract.
- `DefaultFeatureFlags` is the single source for known runtime flags.
- Optional or incomplete features default to disabled until their user-facing
  behavior, privacy review, and release claims are complete.
- Debug builds expose a debug-only feature flag override screen for local QA and
  internal tester diagnosis.
- Release and benchmark builds do not register the debug override route.

This gives the project controlled rollout behavior without adding Play Store
split-install complexity before there is a real install-size or entitlement
reason.

## When To Add Dynamic Feature Modules

Use Play Feature Delivery only when a future feature meets all of these gates:

- It is large enough to justify install-time or on-demand delivery, such as OCR
  model assets, premium export/report assets, or optional AI runtimes.
- The feature has a stable module boundary and does not require feature-to-feature
  dependencies.
- The base app remains useful, local-first, and testable when the module is not
  installed.
- Feature availability is represented by a typed feature flag and a typed runtime
  capability check.
- The Play Store listing, Data Safety form, privacy policy, and release checklist
  describe the feature accurately.

Do not add a dynamic module just to hide unfinished code. Unfinished behavior
must stay behind disabled feature flags and must not be claimed in release or
Play Store materials.

## Candidate Future Modules

| Candidate | Delivery mode | Required gate before implementation |
| --- | --- | --- |
| Receipt OCR import | On demand | OCR privacy review, temporary-file deletion policy, manual review UX, and model/provider contract. |
| Accountant export pack | On demand | Export file contracts, sample-safe reports, share/save UX, and privacy review. |
| Premium insights pack | Conditional/on demand | Entitlement design, local-only AI contract, release-safe analytics taxonomy, and no regulated advice claims. |
| Cloud backup profile | Conditional/on demand only after account work | Passkey/account backend contract, recovery model, Play Integrity decision, and Data Safety update. |

## Implementation Rules

When a dynamic feature is justified:

1. Keep public contracts in base `:core` or `:shared` modules.
2. Keep UI navigation entry points in the base app, but gate them by typed flags
   and install/capability status.
3. Keep the dynamic module free of direct dependencies on unrelated feature
   modules.
4. Add a local fake/no-op implementation so tests and release builds have a safe
   fallback when the module is unavailable.
5. Add PR validation for module assembly and release validation for generated
   split artifacts.
6. Update `docs/play-store-readiness.md`, `docs/privacy-policy.md`, and
   `docs/release/release-checklist.md` before making public claims.

## Debug Override Policy

The debug feature flag override screen is for local development and internal
QA. It can force a Boolean flag on or off in debug builds and persists those
choices in debug app preferences. It must not be used as release rollout
infrastructure.

Release behavior remains controlled by checked-in defaults, build configuration,
and future remote/delivery infrastructure that has been separately reviewed.

## Closure Readiness

`T-E13-04` is complete when this strategy is linked from the README and architecture
docs, and the debug override screen remains debug-only. With typed flags,
`LocalFeatureFlagProvider`, debug overrides, and this strategy in place, the
parent `TS-E13-01 - Implement feature flag provider abstraction` can be closed.