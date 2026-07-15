# ADR 0002: Local-First Persistence And Room KMP Boundary

## Status

Accepted

## Context

Pocket Ledger needs realistic local persistence for Android while also demonstrating a credible Kotlin Multiplatform boundary. A portfolio project can easily over-abstract by moving every layer into KMP, or under-deliver by keeping all persistence Android-only.

The project has Android production flows and a focused Compose Desktop demo. Both need stable local data behavior, but they do not need identical UI or repository composition.

## Decision

Pocket Ledger uses `:core:database` as the Room KMP persistence boundary.

- Room entities, DAOs, database class, schema versioning, and migration registration live in common source.
- Android and desktop source sets own platform-specific database construction only.
- Android production flows consume persistence through `:core:data` repositories.
- The desktop demo reads local Room data through a small desktop data source and maps it to desktop UI models.
- Pure business rules that do not require Room or Android APIs belong in `:shared`.

## Consequences

Positive outcomes:

- The schema and DAO contracts are shared across Android and desktop/JVM.
- The desktop demo proves the persistence boundary without claiming full desktop product parity.
- Android feature modules stay focused on UI, state, navigation, and repository integration.
- Future platforms can reuse the database boundary without pulling in Android UI code.

Tradeoffs:

- Android and desktop still need platform-specific construction and UI mapping code.
- Repository parity is intentionally not forced until a future product need justifies it.
- Contributors must keep Room entities out of feature public APIs.

## Review Rules

- Do not put Android `Context`, Compose UI, navigation, or WorkManager dependencies in common database code.
- Do not expose Room entities directly from feature UI contracts.
- Add migrations and schema snapshots when the database version changes.
- Keep desktop demo limitations documented when persistence reuse changes.