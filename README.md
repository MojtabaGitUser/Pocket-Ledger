# Pocket Ledger

Pocket Ledger is an Android-first Kotlin project with a modular foundation and a selectively Kotlin Multiplatform shared module.

The Android project lives in `androidApp/`.

## Documentation

- [Architecture](docs/architecture.md): module responsibilities, dependency rules, ownership boundaries, and architectural rationale.
- [ADR 0001: Modular Architecture and KMP-Ready Structure](docs/adr/0001-modular-architecture.md): decision record for the modular architecture direction.
- [Android testing report](androidApp/docs/testing-report.md): current test suite inventory, validation results, performance checks, release/R8 coverage, and known gaps.
- [Android performance benchmarks](androidApp/docs/performance-report.md): Macrobenchmark and Baseline Profile generation setup, local device requirements, and update commands.
- [Android security model](androidApp/docs/security-model.md): current local data storage, app lock, AI privacy, logging, threat model, and known limitations.
