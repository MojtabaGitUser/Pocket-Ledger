# KMP Shared Business Logic

This document records the E-07 KMP extraction boundary for issues #12, #62, #63, and #64.

## Scope

The shared module owns stable, platform-neutral business rules that can run on Android and JVM desktop targets without Android framework, Compose, Room, or feature-module dependencies.

Implemented shared surfaces:

- `:shared` transaction form models and validation under `shared.domain.transaction`.
- `:shared` dashboard aggregation, budget progress, recent transaction, and deterministic insight rules under `shared.domain.dashboard`.
- `:shared` local search ranking and filter-aware matching rules under `shared.domain.search`.

Android feature modules remain responsible for presentation, navigation, repository wiring, and Android-specific UI models. They map to and from shared domain models instead of reimplementing the same business rules locally.

## Issue Traceability

| Issue | Status | Evidence |
| --- | --- | --- |
| #63 Move domain models and validation | Complete | Transaction form state, mode, type, validation errors, validated input, and validation result now live in `androidApp/shared/src/commonMain/.../shared/domain/transaction`. The Android transaction feature exposes compatibility typealiases and delegates validation to the shared implementation. |
| #64 Share search and aggregation rules | Complete | Search ranking lives in `shared.domain.search.SharedSearchRanker`; dashboard aggregation and budget status rules live in `shared.domain.dashboard.SharedDashboardSummaryCalculator`. Android `RuleBasedAiProvider` and `DashboardSummaryCalculator` delegate to those shared rules. |
| #62 Move stable business logic to shared KMP | Complete for current stable local MVP logic | Stable transaction validation, dashboard aggregation, budget progress, deterministic insights, and local search ranking are in `:shared`. Android-only repositories, Compose UI, navigation, Room adapters, and app graph wiring intentionally stay outside the shared boundary. |
| #12 Share business logic through Kotlin Multiplatform | Complete for the currently implemented business logic | The shared logic builds and tests through KMP common tests and is consumed by Android modules. Future product areas such as cloud sync, passkeys, OCR, export, and remote AI remain separate future features rather than blockers for this E-07 extraction. |

## Dependency Boundary

Allowed direction:

```text
:feature:transaction  -> :shared
:feature:dashboard    -> :shared
:core:ai              -> :shared
:desktopApp           -> :core:database / future shared consumers
```

Forbidden direction:

```text
:shared -> :app
:shared -> :feature:*
:shared -> :core:database
:shared -> Android framework APIs
:shared -> Compose UI
```

The shared module should only contain deterministic, side-effect-free rules or platform-neutral models. Any code that needs `Context`, Room builders, WorkManager, Firebase, navigation, Compose UI, or Android resources belongs in platform or feature modules.

## Validation

Run from the repository root:

```powershell
.\androidApp\gradlew.bat --no-daemon :shared:allTests :feature:transaction:testDebugUnitTest :feature:dashboard:testDebugUnitTest :core:ai:testDebugUnitTest :app:compileDebugKotlin :app:compileReleaseKotlin --console=plain --stacktrace
```

Run from `androidApp/`:

```powershell
.\gradlew.bat --no-daemon :shared:allTests :feature:transaction:testDebugUnitTest :feature:dashboard:testDebugUnitTest :core:ai:testDebugUnitTest :app:compileDebugKotlin :app:compileReleaseKotlin --console=plain --stacktrace
```

Shared common tests cover validation edge cases, dashboard aggregation and budget status rules, and deterministic search ranking/filter behavior.