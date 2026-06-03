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
