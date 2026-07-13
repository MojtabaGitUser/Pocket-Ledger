# Pocket Ledger

Pocket Ledger is an Android-first Kotlin project with a modular foundation and a selectively Kotlin Multiplatform shared module.

[![PR Validation](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/pr-validation.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/pr-validation.yml)
[![Screenshot And Benchmark Validation](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/screenshot-benchmark.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/screenshot-benchmark.yml)
[![Release Candidate](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/release-candidate.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/release-candidate.yml)
[![Internal Distribution](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/internal-distribution.yml/badge.svg?branch=dev)](https://github.com/MojtabaGitUser/Pocket-Ledger/actions/workflows/internal-distribution.yml)

The Android project lives in `androidApp/`.

The Compose Multiplatform desktop demo lives in `androidApp/desktopApp`.
Run it with `.\androidApp\gradlew.bat :desktopApp:run --console=plain` and
open `Search` or `Insights` from the desktop navigation rail. The desktop demo
uses the shared Room KMP database for local file-backed demo persistence. More
details are in [Desktop demo](docs/desktop-demo.md).

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
- [Desktop demo](docs/desktop-demo.md): Compose Multiplatform desktop run command, Search/Insights access, Room KMP local persistence behavior, and current limitations.
- [Future product growth](docs/future-growth.md): E-21 future extension plan for cloud sync, OCR/import, export/accountant workflows, and monetization without implementing those features.
- [Future extension contracts](docs/future/extension-contracts.md): issue-traceable future contract index for #142-#146.
- [ADR 0001: Modular Architecture and KMP-Ready Structure](docs/adr/0001-modular-architecture.md): decision record for the modular architecture direction.
- [Android testing report](androidApp/docs/testing-report.md): current test suite inventory, validation results, performance checks, release/R8 coverage, and known gaps.
- [Accessibility QA pass](androidApp/docs/accessibility-qa.md): #14 TalkBack, keyboard/D-pad, 200% font-scale, and contrast QA gates for UI changes and release candidates.
- [Android performance benchmarks](androidApp/docs/performance-report.md): Macrobenchmark and Baseline Profile generation setup, local device requirements, and update commands.
- [Android security model](androidApp/docs/security-model.md): current local data storage, app lock, AI privacy, logging, threat model, and known limitations.
- [CI/CD strategy](docs/ci-cd.md): PR validation, controlled screenshot and benchmark workflows, artifacts, and release-safety boundaries.
- [Release signing and versioning](docs/release/signing-versioning.md): secure signing inputs, version policy, local release-ready commands, and CI release candidate behavior.
- [Release candidate workflow](docs/release/release-candidate.md): release workflow triggers, version inputs, signing behavior, retained artifacts, and Play Store handoff boundaries.
- [Release readiness checklist](docs/release/release-checklist.md): signing, versioning, artifact, accessibility, privacy, and rollout review items.
- [Play Store readiness and app content checklist](docs/play-store-readiness.md): Play Console declarations for privacy policy, Data Safety, permissions, backup/device transfer, ads, financial features, and target audience.
- [Privacy policy](docs/privacy-policy.md): Play Store-ready privacy policy draft based on current app behavior, Firebase setup, permissions, and diagnostics.
- [Product event taxonomy](docs/product-event-taxonomy.md): privacy-safe product events, approved parameters, and analytics provider boundaries.
- [Internal distribution](docs/internal-distribution.md): Firebase App Distribution tester flow, required secrets, artifacts, and release notes.
