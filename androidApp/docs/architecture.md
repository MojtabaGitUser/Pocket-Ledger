# Pocket Ledger Architecture

Pocket Ledger is organized as a thin Android app shell over modular core and feature modules.

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
