# Portfolio Architecture Diagram

This document gives reviewers a quick architecture view. The detailed architecture rules live in `docs/architecture.md` and `androidApp/docs/architecture.md`.

## Module Graph

```mermaid
flowchart TD
    app[":app<br/>Android shell, DI wiring, navigation"]
    dashboard[":feature:dashboard<br/>Dashboard UI, budget setup, Android mapping"]
    search[":feature:search<br/>Search UI and filters"]
    transaction[":feature:transaction<br/>Transaction list/detail/editor UI"]
    data[":core:data<br/>Repositories, data models, seed data"]
    database[":core:database<br/>Room KMP schema, DAOs, migrations"]
    shared[":shared<br/>KMP business rules and validation"]
    ai[":core:ai<br/>Local AI/provider abstraction"]
    flags[":core:featureflags<br/>Typed feature flags and debug overrides"]
    security[":core:security<br/>App lock, encrypted prefs, logging"]
    design[":core:designsystem<br/>Theme and reusable UI"]
    analytics[":core:analytics<br/>Privacy-safe analytics contracts"]
    background[":core:background<br/>Scheduler contracts"]
    testing[":core:testing<br/>Fakes, fixtures, benchmark data"]
    desktop[":desktopApp<br/>Compose Desktop demo"]
    macro[":macrobenchmark<br/>Startup, frame timing, baseline profile"]

    app --> dashboard
    app --> search
    app --> transaction
    app --> design
    app --> data
    app --> security
    app --> flags
    app --> analytics
    app --> background

    dashboard --> data
    dashboard --> design
    dashboard --> ai
    dashboard --> shared
    search --> data
    search --> design
    transaction --> data
    transaction --> design
    transaction --> ai
    transaction --> shared

    ai --> flags
    ai --> shared
    data --> database
    desktop --> database
    macro --> app
    testing -. test fixtures .-> data
```

## Runtime Data Flow

```mermaid
sequenceDiagram
    participant UI as Compose feature UI
    participant VM as ViewModel
    participant Repo as core:data repository
    participant DB as core:database Room KMP
    participant Shared as shared KMP rules

    UI->>VM: User action or screen load
    VM->>Repo: Observe or mutate local ledger data
    Repo->>DB: Query or write Room entities
    DB-->>Repo: Flow/update result
    Repo-->>VM: Ledger models
    VM->>Shared: Validate, rank, or aggregate stable business rules
    Shared-->>VM: Pure result model
    VM-->>UI: UI state
```

## Release Validation Flow

```mermaid
flowchart LR
    pr[PR Validation] --> unit[Unit and shared tests]
    pr --> lint[lintDebug]
    pr --> debug[assembleDebug]
    pr --> release[assembleRelease with R8]
    pr --> benchBuild[Benchmark artifact assembly]
    screenshot[Manual or scheduled screenshot workflow] --> paparazzi[verifyAdaptiveScreenshots]
    rc[Release candidate workflow] --> signing[Signing and version checks]
    rc --> artifacts[APK/AAB artifacts]
    internal[Internal distribution workflow] --> firebase[Firebase App Distribution]
```

## Key Decisions For Reviewers

- The app shell is intentionally thin; feature behavior belongs in feature modules or reusable core/shared modules.
- Room schema, DAOs, migrations, and versioning are shared through `:core:database` common source.
- Stable business rules that do not need Android APIs live in `:shared`.
- Android repositories and Compose UI stay Android-scoped to avoid premature cross-platform abstraction.
- Debug/release/benchmark behavior is separated so demo tooling and test helpers do not leak into release builds.

## ADR Index

- `docs/adr/0001-modular-architecture.md`: modular architecture and KMP-ready structure.
- `docs/adr/0002-local-first-room-kmp.md`: local-first persistence and Room KMP boundary.
- `docs/adr/0003-portfolio-release-quality.md`: portfolio-quality documentation, validation, and release evidence strategy.