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

Out of scope for the current dashboard implementation:
- Repository orchestration.
- Room/database access.
- Cloud sync.
- AI or LLM-generated insights.

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
