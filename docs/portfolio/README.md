# Pocket Ledger Portfolio README

Pocket Ledger is an Android-first personal finance sample built to demonstrate production-style mobile engineering in a compact portfolio project. It is intentionally local-first, modular, privacy-aware, and release-hardened rather than a throwaway demo screen.

## Reviewer Snapshot

| Area | Evidence |
| --- | --- |
| Product scope | Offline ledger dashboard, transactions, search, budgets, app lock, local AI-style fallback insights, and a desktop demo. |
| Architecture | Android app shell, feature modules, core modules, Room KMP persistence, shared KMP business rules, and documented dependency boundaries. |
| Quality | JVM tests, shared KMP tests, Room/repository integration test source sets, Paparazzi adaptive screenshots, release/R8 builds, and Macrobenchmark setup. |
| Privacy and security | Local-first storage, encrypted sensitive preferences, app-lock flow, privacy-safe logging rules, AI privacy boundaries, and Play Store privacy documentation. |
| Release readiness | Release signing/versioning docs, release candidate workflow, internal distribution workflow, Play Store readiness checklist, Data Safety notes, and smoke-test checklist. |
| Portfolio traceability | Issue-linked documentation set for E-20 in `docs/portfolio/traceability.md`. |

## What This Project Demonstrates

- Modular Android architecture with explicit ownership boundaries.
- Kotlin Multiplatform reuse where it pays off: Room KMP persistence and shared pure business rules.
- Offline-first local repositories backed by Room and deterministic flows.
- Compose UI with adaptive layouts, screenshot baselines, and accessibility QA guidance.
- Release-aware engineering: R8/resource shrinking, baseline profile setup, CI workflow separation, internal distribution, and Play Store checklists.
- Senior-level documentation discipline: ADRs, architecture diagrams, testing reports, performance reports, security model, release docs, and demo script.

## Fast Orientation

Start with these files in order:

1. `README.md` for project entry points and validation commands.
2. `docs/portfolio/demo-script.md` for a guided walkthrough.
3. `docs/portfolio/architecture-diagram.md` for the module graph and runtime flows.
4. `docs/portfolio/testing-performance-summary.md` for test/performance evidence and remaining honest gaps.
5. `docs/portfolio/review.md` for the final portfolio artifact review.
6. `docs/portfolio/traceability.md` for issue-by-issue closure evidence for #135-#140.

## Demo Narrative

The strongest demo path is:

1. Show the dashboard and explain local-first summary aggregation.
2. Open transactions and show list/detail/adaptive behavior.
3. Open search and explain deterministic local ranking and privacy boundaries.
4. Show settings/app lock and privacy-safe logging/security docs.
5. Show the desktop demo to demonstrate Room KMP persistence reuse.
6. Close with CI, testing, release, and portfolio docs.

The full script is in `docs/portfolio/demo-script.md`.

## Validation Commands

Useful local validation from the repository root:

```powershell
.\androidApp\gradlew.bat lintDebug
.\androidApp\gradlew.bat testDebugUnitTest :shared:allTests
.\androidApp\gradlew.bat :app:assembleDebug :app:assembleRelease
.\androidApp\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble
```

Screenshot and performance-sensitive validation:

```powershell
.\androidApp\gradlew.bat verifyAdaptiveScreenshots
.\androidApp\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest
.\androidApp\gradlew.bat :app:generateReleaseBaselineProfile
```

Connected benchmark and Baseline Profile commands require an attached device or emulator.

## Honest Boundaries

Pocket Ledger does not claim production bank integration, cloud sync, OCR import, real remote AI inference, Play Store publication, or device-lab benchmark numbers. Those areas are documented as future extensions or manual release steps. The portfolio value is in the architecture, local-first implementation, privacy posture, validation strategy, and traceable release readiness work.