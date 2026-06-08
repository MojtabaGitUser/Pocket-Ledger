# Pocket Ledger Architecture

Pocket Ledger is organized as a thin Android app shell over modular core and feature modules.

## Adaptive UI Architecture

T-E06-01 provides adaptive infrastructure only. It does not redesign
transaction, dashboard, search, settings, or insight screens; feature-specific
adaptive layouts belong to later tasks.

Reusable adaptive decisions live in `:core:designsystem` under
`com.mojtaba.pocketledger.core.designsystem.adaptive`. The app maps window
width into a single `AdaptiveNavigationState`:

- Compact widths use `AdaptiveNavigationType.BottomBar` and
  `AdaptivePaneType.SinglePane`.
- Medium widths use `AdaptiveNavigationType.NavigationRail` and are
  `AdaptivePaneType.ListDetail` capable.
- Expanded widths use `AdaptiveNavigationType.PermanentDrawer` and are
  `AdaptivePaneType.ListDetail` capable.

The `:app` module owns Android-specific adaptive integration. It calculates the
adaptive navigation state, hosts `PocketLedgerAdaptiveApp`, and renders
`AdaptiveNavigationScaffold`, which chooses bottom navigation, navigation rail,
or permanent navigation drawer. The `NavHost` and feature navigation graphs do
not know which navigation chrome is active.

Foldable posture infrastructure also lives in `:app` because it depends on
AndroidX WindowManager. `FoldableUiState` exposes flat, half-opened, book,
tabletop, and unknown postures through composition locals with a safe flat
fallback on non-foldable devices. Future list-detail feature work can consume
the adaptive pane type and foldable state without adding window logic to feature
modules.

### Adaptive Dashboard Layout

T-E06-03 adapts the existing dashboard UI from `:feature:dashboard` without
replacing dashboard widgets or adding new summary logic. The app shell passes
the existing `PocketLedgerWindowWidthSizeClass` from T-E06-01 into the
dashboard navigation graph. Dashboard maps that size class to a feature-local
layout mode only:

- Compact widths use a single-column vertical dashboard with stacked widgets.
- Medium widths keep the cash-flow summary prominent and place category spend
  with budget progress, then insights with recent transactions, in two-column
  rows.
- Expanded widths use a dashboard grid: cash-flow metrics remain full-width,
  category spend, budgets, and insights share the primary grid row, and recent
  transactions use the available width below.

Dashboard content stays in a vertical lazy container across all modes so
landscape, desktop mode, posture changes, and large font scaling remain
scrollable. Widget cards avoid fixed heights and allow text to wrap where user
font scaling can otherwise clip labels. The feature module does not consume
`FoldableUiState` directly because hinge geometry is currently app-local; on
foldables the dashboard falls back to responsive, scrollable width-size-class
behavior rather than hinge-aware pane placement. Adaptive transaction
list/detail remains owned by T-E06-02.

## Background Jobs Architecture

T-E09-01 establishes scheduler infrastructure only. It does not add passkeys,
sync, notifications, analytics, AI work, or production background jobs.

Background scheduling is split across a pure contract and an Android adapter:

```text
UI / future use cases
        |
        v
:core:background BackgroundTaskScheduler
        |
        v
:app WorkManagerScheduler
        |
        v
AndroidX WorkManager
```

`:core:background` owns domain-friendly scheduling models such as
`BackgroundTaskId`, `ScheduledTask`, `TaskSchedule`, `TaskConstraints`,
`TaskPolicy`, `TaskStatus`, and `SchedulerResult`. These APIs do not expose
`Context`, `WorkManager`, `WorkRequest`, or Worker classes, so feature and
business code can depend on scheduling without depending on Android background
execution details.

Typed task definitions live in `:core:background` under
`com.mojtaba.pocketledger.core.background.tasks`. They centralize stable task
IDs for future work such as sync, cleanup, and budget refresh. They are task
definitions only; no worker behavior is implemented by T-E09-01.

The Android implementation lives in `:app` because the app module is the
composition root and already owns Android-specific construction. `AppGraph`
creates a `WorkManagerScheduler`, which maps contract models to WorkManager
requests and policies. A separate app-side `TaskWorkerRegistry` maps registered
task IDs to concrete Worker classes. Until future tasks add real workers, that
registry is empty and scheduling an unbound task returns a failure rather than
silently running placeholder work.

`:core:testing` provides `FakeScheduler` for future use-case and ViewModel
tests. It records enqueued tasks and cancellations, exposes configurable task
states, and never depends on WorkManager.

## Feature Flags Architecture

T-E13-01 establishes typed feature flag infrastructure only. It does not add
remote config, Firebase, analytics, A/B testing, server-driven flags, or enable
AI, passkey, cloud, sync, or screenshot testing behavior.

Feature flag evaluation is split across a pure contract and local providers:

```text
UI / future use cases
        |
        v
:core:featureflags FeatureFlagEvaluator
        |
        v
:core:featureflags FeatureFlagProvider
        |
        v
Local or test provider
```

`:core:featureflags` owns typed flag definitions and provider contracts under
`com.mojtaba.pocketledger.core.featureflags`. App, feature, and business code
should depend on `FeatureFlag`, `FeatureFlagProvider`, or
`FeatureFlagEvaluator` rather than raw string keys, raw booleans, direct
`BuildConfig` checks, or Android `Context`. The module has no dependency on
`:app`, feature modules, database, WorkManager, networking, or remote
configuration.

Default flags for incomplete or optional capabilities live in
`DefaultFeatureFlags` and use safe disabled defaults. Current defaults include
semantic search, AI insights, passkey account flow, cloud sync, production
background jobs, demo data tools, and screenshot testing. These flags are
definitions only; adding a flag does not make the underlying feature visible or
active.

`LocalFeatureFlagProvider` accepts typed overrides and falls back to each flag's
default value when no override is present. Type mismatches fail fast instead of
coercing strings or silently changing behavior. Boolean flags can be queried
through `FeatureFlagProvider.isEnabled` or `FeatureFlagEvaluator.isEnabled`:

```kotlin
if (featureFlags.isEnabled(DefaultFeatureFlags.SemanticSearchEnabled)) {
    // Route to a future semantic search experience.
}
```

Test-only overrides live in `:core:testing` through `FakeFeatureFlagProvider`.
Tests can enable, disable, or set typed values deterministically without using
Android framework APIs, remote services, or build-type checks.

When adding a new flag:
1. Define it once in `DefaultFeatureFlags` with a stable key, safe default, and
   clear description.
2. Add or update tests that verify metadata, default safety, and any gating
   behavior.
3. Inject `FeatureFlagEvaluator` or `FeatureFlagProvider` through the existing
   dependency boundary instead of reading `BuildConfig` inside feature or
   business logic.
4. Keep remote config, analytics exposure tracking, persistence, and server
   rollout behavior behind future provider implementations.

## Security Architecture

T-E10-02 establishes encrypted sensitive preference infrastructure only. It
does not add passkeys, login, cloud sync, biometric unlock, key rotation, or
production storage of real user secrets.

Sensitive preference storage is isolated in `:core:security` under
`com.mojtaba.pocketledger.core.security.preferences`:

```text
UI / future use cases
        |
        v
:core:security SensitivePreferences
        |
        v
EncryptedSensitivePreferences
        |
        v
AndroidX Security / Android Keystore
```

Business and feature code should depend on `SensitivePreferences` and typed
keys such as `StringPreferenceKey`, `BooleanPreferenceKey`, and
`LongPreferenceKey`. Consumers must not depend directly on `Context`,
`SharedPreferences`, `EncryptedSharedPreferences`, `MasterKey`, or raw
preference key strings.

`DefaultSensitivePreferenceKeys` centralizes stable key definitions for future
sensitive values such as passkey credential IDs, account session tokens, last
security check timestamps, and biometric unlock state. Defining these keys does
not write values or enable the related features.

`EncryptedSensitivePreferences` uses AndroidX Security Crypto with an AES-256
GCM `MasterKey`, encrypted preference keys, and encrypted preference values. It
stores data in the stable app-private file
`pocket_ledger_sensitive_prefs`, uses the application context, does not log
sensitive values, and does not include keys or values in error messages.
AndroidX Security Crypto is kept behind the `SensitivePreferences` abstraction
so a future Android Keystore implementation or key-rotation strategy can replace
it without changing feature code.

`InMemorySensitivePreferences` provides deterministic non-Android storage for
unit tests and future use-case tests. It supports the same typed API, returns
defaults for missing values, and never persists data to disk.

Security rules:
- Never store tokens, credential IDs, account state, or biometric unlock state
  in plain `SharedPreferences`, DataStore, logs, or Room.
- Add new sensitive preference keys only in `DefaultSensitivePreferenceKeys`.
- Inject `SensitivePreferences` through the existing dependency boundary when a
  future passkey, account, or privacy feature needs sensitive local storage.
- Keep remote sync, authentication, biometric prompts, analytics, and key
  rotation out of this module until dedicated tasks define those requirements.

## Feature Modules

### `:feature:dashboard`

The dashboard feature module owns dashboard-facing summary models, deterministic derivation logic, and dashboard-specific Compose UI for budget overview and non-AI insights.

Current scope:
- Pure dashboard summary data models.
- `DashboardSummaryCalculator`, a side-effect-free calculator that accepts in-memory core data models.
- Deterministic insight generation such as cash-flow status, concentrated category spending, and budget progress signals.
- Material 3 dashboard rendering components for cash flow, category spend, budget progress, recent transactions, and deterministic insights.
- Simple offline-first budget setup UI and state for local monthly budgets.

Out of scope for the current dashboard implementation:
- Room/database access.
- Cloud sync.
- AI or LLM-generated insights.
- Advanced budget schedules, custom recurrence, alerts, and notifications.

Budget setup is owned by `:feature:dashboard` because budgets feed dashboard
progress and non-AI insights. The MVP is local-only and monthly-only: users can
create or edit stored budgets with a name, amount, currency, optional category,
period start/end, and active state. The feature validates input in pure Kotlin
before calling `BudgetRepository`; it does not create default budgets or seed
production data. Dashboard progress summaries consume stored budgets through
the core data layer, while future custom schedules, alerts, and sync remain
deferred.

Dependency rules:
- `:feature:dashboard` may depend on `:core:data` for reusable ledger models.
- `:feature:dashboard` must not depend on `:app`.
- `:feature:dashboard` must not depend on `:core:database` directly.
- `:core:data` and `:core:database` must not depend on dashboard feature code.

The calculator uses a single-currency MVP rule: `DashboardSummaryInput.currencyCode` selects the summary currency, and records in other currencies are excluded rather than converted or combined.

### `:feature:search`

The search feature module owns the offline transaction search UI, Compose state
handling, filter controls, result mapping, and result-to-detail navigation
boundary. It consumes `SearchQuery` and filter models from `:core:data` and
does not define a parallel ad hoc query contract.

Current scope:
- Keyword transaction search.
- Transaction type, category, tag, date range, and amount range filters.
- Local repository-backed loading, empty-ledger, no-results, and error states.
- Accessible result rows and filter controls.

Out of scope:
- AI or semantic search.
- OCR search.
- Cloud search.
- Remote sync.
- Advanced ranking beyond the shared search and deterministic sort rules.

Indexed local search execution is provided by `:core:data` over
`:core:database`. `:feature:search` depends on `:core:data` and
`:core:designsystem`; it must not depend on `:app` or access Room directly.
Future AI/semantic search support is deferred to E-12/T-E05-04 and must remain
feature-flagged with a normal offline fallback before any UI suggests that it is
active.

### `:feature:transaction`

The transaction feature owns transaction list, detail, editor, and adaptive
list/detail composition. T-E06-02 adapts the existing list and detail screens
without replacing their repository-backed state holders or compact navigation
flow.

Compact widths use the original single-pane route behavior:
`transactions/list` opens the list, and selecting a row navigates to
`transactions/detail/{transactionId}` with normal back behavior. Medium and
expanded widths route both the list destination and transaction detail deep
links through `TransactionAdaptiveRoute`, which keeps the list visible and
updates the detail pane from a saved `selectedTransactionId`.

Selection state lives in `TransactionSelectionViewModel` with `SavedStateHandle`
backing so selection survives recomposition, rotation, posture changes, and
window resizing. If a selected transaction disappears from the locally observed
list, the selection is cleared and the detail pane falls back to a safe
"Select a transaction" placeholder. If a deep link targets a missing
transaction, the existing detail not-found state is shown instead of crashing.

The adaptive transaction screen consumes `AdaptivePaneType` from the T-E06-01
design-system infrastructure. It does not duplicate window-size rules, foldable
posture detection, or navigation chrome decisions. Search result clicks keep
using the transaction detail route; on larger screens that route resolves to the
adaptive list/detail experience with the selected transaction open.

## Core Data Search Models

`:core:data` owns pure Kotlin search and filter query models under
`com.mojtaba.pocketledger.core.data.search`. These models describe a
type-safe, offline-first query contract for future local transaction search and
repository/Room query mapping.

`SearchQuery` supports free text, transaction type filters, category and tag
filters, optional date and amount ranges, optional currency filtering, recurring
transaction filtering, and deterministic sort options. Search models do not
depend on Android UI, Compose, Room annotations, feature modules, network
services, or AI/LLM behavior.

Callers should normalize and validate a `SearchQuery` before repository
execution. Normalization handles whitespace, ID cleanup, and currency casing;
validation reports structured errors for invalid ranges, invalid currency codes,
blank raw IDs, and abusive text length. UI entry points and actual Room query
execution are deferred to future search and filtering tasks.

T-E05-02 adds the first repository-backed execution path for transaction
keyword search. `TransactionRepository.searchTransactions(SearchQuery)` observes
local Room results as a `Flow` and normalizes/validates the query before
execution. The MVP searches transaction `merchant`, `note`, and `source` fields
with escaped prefix `LIKE` patterns and deterministic DAO-level sorting. Room
B-tree indices exist on those text columns, which supports prefix searches such
as `coffee%`; arbitrary contains searches such as `%coffee%`, FTS ranking,
category-name keyword search, and tag-name keyword search are deferred.

## Core Database

`:core:database` owns the Room database, entities, DAOs, exported schema snapshots, and migration registration.

Room migration rules:
- Version 1 is the initial Pocket Ledger schema.
- Future schema changes must bump `PocketLedgerDatabase` and `DatabaseMigrations.CURRENT_VERSION` together.
- Every released migration must live in `DatabaseMigrations.ALL`.
- Room schema JSON files under `core/database/schemas` are source artifacts and must be committed.
- Migration tests must validate each version step and the full path to the current version before release.

Migration workflow:
1. Change the entity or schema surface.
2. Bump the database version.
3. Add an explicit Room `Migration`.
4. Register it in `DatabaseMigrations.ALL`.
5. Run schema export.
6. Commit the new schema JSON.
7. Add or update `MigrationTestHelper` coverage.
8. Run `:core:database` unit, androidTest assembly, and connected migration tests when a device is available.

## Demo Seed Data

Deterministic local seed data tools live in `:core:data` under
`com.mojtaba.pocketledger.core.data.seed`. They populate Room-backed local
repositories with realistic categories, budgets, tags, transactions, and
transaction-tag links for developer, preview, test, and demo use.

Seed data is offline-only and must not trigger network calls, sync behavior, or
release runtime insertion. App-side entry points, if added later, must be guarded
behind `BuildConfig.DEBUG` or an equivalent internal-build flag.

Demo records use stable IDs with the `demo-` prefix, such as
`demo-category-groceries`, so repeated runs can upsert the same local records
without duplication. Transaction-tag links are inserted through the repository
API and rely on the database composite key to ignore duplicate links.

`DemoDataSeeder` intentionally does not expose a broad clear operation. Removing
demo categories can affect user-created records that reference those categories,
so any future clear tool must only target demo-prefixed records and must account
for foreign-key side effects before being wired into a debug flow.

Category and tag names are unique in the schema. Seeding is deterministic and
idempotent for databases that do not already contain non-demo records with the
same unique names as the demo category or tag definitions.
