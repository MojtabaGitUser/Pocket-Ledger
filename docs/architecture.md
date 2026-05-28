# Pocket Ledger Architecture

This document defines package, module, dependency, and ownership rules for the Pocket Ledger Android/KMP project. Keep it practical: new code should have one clear home, dependencies should point in one direction, and module APIs should stay intentional.

## Current Modules

The Android project currently includes:

- `:app`
- `:core:designsystem`
- `:shared`

Planned modules should follow the same rules:

- `:core:data`
- `:core:database`
- `:core:domain`
- `:feature:transactions`
- `:feature:dashboard`
- `:feature:settings`

## Package Naming

All production Kotlin packages must start with:

```text
com.mojtaba.pocketledger
```

Package names should mirror module ownership:

```text
com.mojtaba.pocketledger                         // :app
com.mojtaba.pocketledger.core.designsystem       // :core:designsystem
com.mojtaba.pocketledger.shared                  // :shared
com.mojtaba.pocketledger.core.domain             // future :core:domain
com.mojtaba.pocketledger.core.data               // future :core:data
com.mojtaba.pocketledger.core.database           // future :core:database
com.mojtaba.pocketledger.feature.transactions     // future :feature:transactions
com.mojtaba.pocketledger.feature.dashboard        // future :feature:dashboard
com.mojtaba.pocketledger.feature.settings         // future :feature:settings
```

Use lowercase package segments. Do not encode implementation details such as `impl`, `new`, `old`, `v2`, or ticket numbers in package names.

## Module Responsibilities

### `:app`

`:app` is the Android application shell. It owns application bootstrap only:

- `MainActivity`
- app manifest and launcher configuration
- top-level Compose host
- top-level navigation shell when navigation is introduced
- dependency wiring that must happen at the application boundary
- build variant, signing, and application ID configuration

Keep `:app` thin. It must not own business logic, data access, repository implementations, database entities, DTO mapping, or feature-specific screens beyond temporary bootstrap placeholders.

### `:core:designsystem`

`:core:designsystem` owns shared UI foundations only:

- Material theme setup
- colors, typography, shapes, spacing, and other design tokens
- reusable low-level UI primitives that are product-wide
- preview theme utilities

It must not depend on feature modules, `:app`, repositories, use cases, databases, network clients, or business domain models. Components in this module should be generic enough to be used by multiple features.

### `:shared`

`:shared` owns reusable KMP-ready common logic:

- pure Kotlin domain/business logic reusable across platforms
- common value objects and platform-independent helpers
- common tests for reusable logic

Prefer `commonMain` for platform-independent code. Use platform-specific source sets only when a real platform API is required. `:shared` should not contain Android Compose UI, Android framework types, database implementations, or network DTOs that only serve Android infrastructure.

### Future `:core:domain`

`:core:domain` should own Android-app domain contracts and use cases:

- use cases/interactors
- repository interfaces
- domain models used by Android features
- validation and business rules that are Android-app specific

It should not know about Retrofit, Room, SQL drivers, Compose, Android `Context`, or feature UI.

### Future `:core:data`

`:core:data` should own data orchestration:

- repository implementations
- DTO-to-domain and entity-to-domain mapping
- coordination between database, network, preferences, and other sources

It may depend on `:core:domain`, `:core:database`, and possibly `:shared`. It must not depend on `:app` or feature modules.

### Future `:core:database`

`:core:database` should own local persistence:

- database configuration
- DAOs/queries
- database entities
- migrations

Database entities are persistence models, not UI or domain models. Map them before exposing data outside the data layer.

### Future `:feature:*`

Feature modules own user-facing workflows:

- Compose screens and screen-level UI state
- ViewModels
- feature navigation entry points
- feature-specific UI models

Feature modules may depend on stable core APIs such as `:core:designsystem`, `:core:domain`, and `:shared`. They must not depend on `:app`, other feature modules, database implementations, or network implementation details.

## Dependency Direction

Dependencies must point inward toward stable, reusable modules:

```text
:app
  -> :feature:*
  -> :core:designsystem
  -> :core:domain
  -> :shared

:feature:*
  -> :core:designsystem
  -> :core:domain
  -> :shared

:core:data
  -> :core:domain
  -> :core:database
  -> :shared

:core:domain
  -> :shared

:core:designsystem
  -> no app, feature, data, database, or domain implementation modules

:shared
  -> no Android app modules
```

Allowed examples:

```kotlin
// :app can launch feature navigation when feature modules exist.
implementation(project(":feature:transactions"))
implementation(project(":core:designsystem"))

// A feature can use shared UI foundations and domain APIs.
implementation(project(":core:designsystem"))
implementation(project(":core:domain"))
implementation(project(":shared"))

// Data can implement domain repositories.
implementation(project(":core:domain"))
implementation(project(":core:database"))
```

Forbidden examples:

```kotlin
// Features must never depend on the application module.
implementation(project(":app"))

// Design system must not know about product features.
implementation(project(":feature:transactions"))

// Domain must not depend on data implementation details.
implementation(project(":core:data"))

// Database must not depend on UI.
implementation(project(":core:designsystem"))
```

If a desired dependency violates these rules, introduce an interface in the stable owner module and provide the implementation in the outer module.

## Placement Rules

Place code by responsibility:

- Compose UI: feature modules for screens; `:core:designsystem` for shared primitives and theme; `:app` only for top-level host/bootstrap UI.
- ViewModels: feature modules. ViewModels should depend on use cases or domain contracts, not repositories implemented directly in data modules.
- Use cases: future `:core:domain` for Android-app domain workflows; `:shared` only for KMP-ready pure logic reused across platforms.
- Repositories: interfaces in future `:core:domain`; implementations in future `:core:data`.
- DTOs: future `:core:data` network/local data layer packages. Do not expose DTOs to features.
- Database entities: future `:core:database`. Do not use entities directly as UI or domain models.
- Design tokens: `:core:designsystem`, such as colors, typography, shapes, and spacing.
- Shared pure Kotlin logic: `:shared/src/commonMain`, under `com.mojtaba.pocketledger.shared`.

Do not mix data, domain, and UI concerns in the same class. A Compose screen should not parse DTOs. A repository implementation should not import Compose. A domain use case should not depend on Android framework APIs.

## Public API Boundaries

Every module should expose the smallest useful API:

- Prefer `internal` for implementation details.
- Public classes and functions are module contracts and should be stable.
- Public APIs should use domain models or explicit UI models, not persistence entities or network DTOs.
- A module should not expose third-party implementation details unless that dependency is part of the intended contract.
- Keep preview-only helpers scoped to debug/preview usage where practical.

When a feature needs behavior from another layer, depend on a contract owned by a stable module. Do not reach across layers to call implementation classes directly.

## Naming Conventions

Use names that describe responsibility:

- Compose screens: `TransactionsScreen`, `DashboardScreen`, `SettingsScreen`
- Route/navigation entry points: `TransactionsRoute`, `DashboardRoute`
- ViewModels: `TransactionsViewModel`
- UI state: `TransactionsUiState`
- UI events/actions: `TransactionsUiEvent` or `TransactionsAction`
- Use cases: `GetTransactionsUseCase`, `AddTransactionUseCase`
- Repository interfaces: `TransactionRepository`
- Repository implementations: `OfflineFirstTransactionRepository`, `DefaultTransactionRepository`
- DTOs: `TransactionDto`
- Database entities: `TransactionEntity`
- Mappers: `TransactionMapper` or focused extension functions in mapper files
- Design tokens: `PocketLedgerColors`, `PocketLedgerTypography`, `PocketLedgerSpacing`

Avoid vague names such as `Manager`, `Helper`, `Util`, `Common`, or `Base` unless the abstraction is already proven and narrowly defined.

## Ownership and Change Rules

Module owners are responsible for keeping boundaries clean:

- Changes in `:app` should be limited to bootstrap, navigation shell, manifests, and app configuration.
- Changes in `:core:designsystem` must preserve reusable UI foundations and should not introduce product workflow logic.
- Changes in `:shared` must remain KMP-friendly unless a platform-specific source set is deliberately used.
- Feature work should live in the relevant `:feature:*` module once those modules exist.
- Data access changes belong in future `:core:data` or `:core:database`, not in feature UI.

Before adding a dependency, ask:

1. Does this dependency point inward toward a more stable module?
2. Does it expose implementation details across a public API?
3. Would this make the module harder to reuse or test?
4. Is there a smaller contract that should live in a core module instead?

If the answer reveals a boundary problem, adjust the module ownership before merging the change.

## Generated Files

Do not commit generated or local build outputs:

- `.gradle/`
- `build/`
- `.kotlin/`
- `local.properties`
- IDE workspace files

Generated files may exist locally after Gradle or Android Studio runs, but they must remain ignored and untracked.
