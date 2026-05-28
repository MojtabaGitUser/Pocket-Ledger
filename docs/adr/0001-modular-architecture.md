# ADR 0001: Modular Architecture and KMP-Ready Structure

## Status

Accepted

## Context

Pocket Ledger is an Android/Kotlin project that is intended to grow beyond a single application module. The current project contains:

- `:app`
- `:core:designsystem`
- `:shared`

The project is also KMP-ready through the `:shared` module, which allows reusable pure Kotlin logic to live outside Android-specific modules.

As the product grows, expected modules include:

- `:core:data`
- `:core:database`
- `:core:domain`
- `:feature:transactions`
- `:feature:dashboard`
- `:feature:settings`

Without explicit architecture rules, feature code, data access, design tokens, and app bootstrap code can drift into the wrong modules. That creates circular dependencies, hard-to-test classes, unstable public APIs, and poor reuse across Android and future platforms.

## Decision

Pocket Ledger will use a modular architecture with clear dependency direction and ownership:

- `:app` remains a thin Android shell for bootstrap, manifest configuration, top-level Compose hosting, and navigation wiring.
- `:core:designsystem` owns shared UI foundations such as theme, color, typography, shape, spacing, and reusable product-wide UI primitives.
- `:shared` owns KMP-ready pure Kotlin common logic and reusable business rules.
- Future `:feature:*` modules own user-facing screens, screen state, ViewModels, and feature navigation entry points.
- Future `:core:domain` owns domain models, use cases, and repository contracts.
- Future `:core:data` owns repository implementations and data mapping.
- Future `:core:database` owns persistence configuration, DAOs/queries, entities, and migrations.

Dependencies must point from outer modules toward stable inner modules. Feature modules must never depend on `:app`. Domain and shared logic must not depend on UI, Android framework APIs, database implementations, or network DTOs.

All production package names must use `com.mojtaba.pocketledger` as the root package.

## Consequences

Positive outcomes:

- Feature work can be added without turning `:app` into a large mixed-responsibility module.
- Shared theme and design tokens remain consistent across screens.
- Business rules can be tested without Android framework dependencies.
- KMP-ready code has a clear home in `:shared`.
- Future data and database modules can evolve without leaking DTOs or entities into UI.
- Public APIs become more deliberate because each module exposes only stable contracts.

Tradeoffs:

- New features may require adding module wiring before implementation starts.
- Some simple changes may require a mapper or interface to preserve boundaries.
- Contributors must think about ownership before adding dependencies.

These tradeoffs are acceptable because Pocket Ledger is intended to be a maintainable portfolio-quality modular project, not a single-screen prototype.

## Examples

Allowed:

```kotlin
// Feature module uses design system and domain contracts.
implementation(project(":core:designsystem"))
implementation(project(":core:domain"))

// Data module implements domain repositories.
implementation(project(":core:domain"))
implementation(project(":core:database"))
```

Forbidden:

```kotlin
// A feature must not depend on the application shell.
implementation(project(":app"))

// Domain must not depend on data implementation.
implementation(project(":core:data"))

// Design system must not depend on a feature workflow.
implementation(project(":feature:transactions"))
```

## Review Rules

Architecture review should block changes that:

- add a dependency opposite the approved direction,
- put business logic in `:app`,
- put feature-specific workflow logic in `:core:designsystem`,
- expose DTOs or database entities through feature or domain APIs,
- make KMP common code depend on Android-only APIs,
- mix UI, domain, and data responsibilities in a single class.

If a rule needs to change, update this ADR or add a new ADR before merging the implementation that depends on the new rule.
