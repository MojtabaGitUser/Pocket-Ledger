# Pocket Ledger

Pocket Ledger is an Android-first Kotlin project with a modular foundation and a selectively Kotlin Multiplatform shared module.

[![PR Validation](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/pr-validation.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/pr-validation.yml)
[![Screenshot And Benchmark Validation](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/screenshot-benchmark.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/screenshot-benchmark.yml)
[![Release Candidate](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/release-candidate.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/release-candidate.yml)
[![Internal Distribution](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/internal-distribution.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/internal-distribution.yml)

The Android project lives in `androidApp/`.

## CI Commands

Run local validation from the repository root before opening a PR. These
commands mirror the GitHub Actions workflows where possible; detailed workflow
strategy, artifacts, and release-safety notes are in
[CI/CD strategy](docs/ci-cd.md).

Quick PR validation:

```powershell
.\androidApp\gradlew.bat lintDebug
.\androidApp\gradlew.bat testDebugUnitTest :shared:allTests
.\androidApp\gradlew.bat :app:assembleDebug :app:assembleRelease
.\androidApp\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble
```

For macOS/Linux, run the same tasks from `androidApp/` with `./gradlew`.

UI, accessibility, theme, layout, or font-scale changes:

```powershell
.\androidApp\gradlew.bat verifyAdaptiveScreenshots
```

Performance-sensitive or Baseline Profile changes:

```powershell
.\androidApp\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble
.\androidApp\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest
.\androidApp\gradlew.bat :app:generateReleaseBaselineProfile
```

Connected benchmark and Baseline Profile commands require an attached emulator
or device and are intentionally not part of default PR validation.

## Documentation

- [Architecture](docs/architecture.md): module responsibilities, dependency rules, ownership boundaries, and architectural rationale.
- [ADR 0001: Modular Architecture and KMP-Ready Structure](docs/adr/0001-modular-architecture.md): decision record for the modular architecture direction.
- [Android testing report](androidApp/docs/testing-report.md): current test suite inventory, validation results, performance checks, release/R8 coverage, and known gaps.
- [Android performance benchmarks](androidApp/docs/performance-report.md): Macrobenchmark and Baseline Profile generation setup, local device requirements, and update commands.
- [Android security model](androidApp/docs/security-model.md): current local data storage, app lock, AI privacy, logging, threat model, and known limitations.
- [CI/CD strategy](docs/ci-cd.md): PR validation, controlled screenshot and benchmark workflows, artifacts, and release-safety boundaries.
- [Product event taxonomy](docs/product-event-taxonomy.md): privacy-safe product events, approved parameters, and analytics provider boundaries.
- [Internal distribution](docs/internal-distribution.md): Firebase App Distribution tester flow, required secrets, artifacts, and release notes.
