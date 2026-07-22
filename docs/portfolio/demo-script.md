# Portfolio Demo Script

This script supports #140. It is written for a 10 to 15 minute portfolio walkthrough.

## Setup

From the repository root:

```powershell
.\androidApp\gradlew.bat :app:assembleDebug
.\androidApp\gradlew.bat :desktopApp:run --console=plain
```

Use Android Studio or Gradle to run the Android app on an emulator/device. The desktop demo is optional but useful for showing Room KMP persistence reuse.

## Opening Pitch - 45 Seconds

"Folentra is an Android-first personal finance app sample. I built it to show production-style mobile engineering: modular architecture, offline-first Room persistence, selective KMP reuse, privacy-aware local behavior, accessibility and screenshot validation, release/R8 hardening, and traceable documentation. It is intentionally honest about what is implemented and what remains future work."

## Walkthrough 1 - Product Surface - 3 Minutes

1. Open the app and start on Dashboard.
2. Point out cash flow, top categories, budget progress, recent transactions, and local insights.
3. Navigate to Transactions.
4. Show list/detail behavior and explain adaptive list/detail support on larger widths.
5. Open Search.
6. Search for a transaction and explain local-only query/ranking behavior.
7. Open Settings/App Lock if available in the build.
8. Mention privacy-safe logging and encrypted sensitive preferences.

Talking points:

- Core flows are local-first and do not require a backend.
- UI state is ViewModel-driven and testable.
- The app is designed around privacy boundaries, not remote data collection.

## Walkthrough 2 - Architecture - 3 Minutes

Open `docs/portfolio/architecture-diagram.md`.

1. Show the module graph.
2. Explain that `:app` is the thin shell.
3. Explain feature modules: dashboard, search, transaction.
4. Explain core modules: data, database, security, AI, feature flags, analytics, background.
5. Explain `:shared` for pure KMP business rules.
6. Explain `:core:database` as the Room KMP persistence boundary.
7. Show ADRs in `docs/adr`.

Talking points:

- Dependencies point inward toward stable modules.
- KMP is used selectively, not forced everywhere.
- Android UI and repository wiring stay Android-scoped where that is the simpler, safer boundary.

## Walkthrough 3 - Quality And Release - 3 Minutes

Open `docs/portfolio/testing-performance-summary.md`.

1. Show validation commands in README.
2. Show testing report inventory.
3. Show Paparazzi screenshot matrix and accessibility QA docs.
4. Show Macrobenchmark/performance report.
5. Show release checklist and release candidate workflow.
6. Show Play Store readiness and privacy policy docs.

Talking points:

- JVM and shared tests cover fast feedback.
- Screenshot tests protect adaptive UI and large font states.
- Release/R8 build validation is treated as a normal quality gate.
- Device-required tests are documented honestly rather than over-claimed.

## Walkthrough 4 - Desktop Demo - 2 Minutes

Run:

```powershell
.\androidApp\gradlew.bat :desktopApp:run --console=plain
```

1. Open Search in the desktop demo.
2. Show persisted local demo data.
3. Open Insights.
4. Explain that desktop uses the shared Room KMP database but intentionally remains a focused demo, not a full production desktop app.

Talking points:

- The desktop demo proves the persistence boundary is portable.
- The project avoids pretending Android UI and full workflows are already cross-platform.

## Walkthrough 5 - Traceability Close - 1 Minute

Open `docs/portfolio/traceability.md`.

Explain:

- #137 is covered by the portfolio README.
- #138 is covered by architecture diagrams and ADRs.
- #139 is covered by testing/performance summary and existing detailed reports.
- #140 is covered by this demo script.
- #136 is covered by the traceable documentation set.
- #135 is covered by the final portfolio review.

## Good Closing Answer

"The project is not meant to be a production finance company by itself. It is a focused artifact showing how I structure an Android product for maintainability, privacy, testing, release confidence, and future growth. I made the boundaries explicit so a reviewer can see both what is implemented and where I would take it next."