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
