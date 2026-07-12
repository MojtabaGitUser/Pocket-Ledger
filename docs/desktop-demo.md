# Desktop Demo

Pocket Ledger includes a Compose Multiplatform desktop demo in `:desktopApp`.
It is a JVM desktop target for E-08 and remains a focused demo, not a full
product-grade desktop app. The demo now uses the shared Room KMP database from
`:core:database` for its local Search and Insights data snapshot.

Related GitHub scope:

- #66 TS-E08-01 - Create desktop target consuming shared logic.
- #67 T-E08-01 - Add desktop demo module.
- #68 T-E08-02 - Implement desktop Search screen.
- #69 T-E08-03 - Implement desktop Insights screen.
- #70 T-E08-04 - Document desktop demo limitations.
- #232 T-E04-06 - Implement Room KMP shared database target and desktop persistence.

## Issue Traceability

| Issue | Status | Evidence |
| --- | --- | --- |
| #67 Add desktop demo module | Complete | `:desktopApp` is registered in `androidApp/settings.gradle.kts`, has its own JVM Compose Desktop Gradle module, and declares `com.mojtaba.pocketledger.desktop.MainKt` as the desktop entry point. |
| #68 Implement desktop Search screen | Complete | `DesktopSearchScreen` provides keyword input, keyword/local semantic modes, income and expense filters, Room-backed demo records, result list states, and selected result preview. `DesktopSearchMapperTest` covers mapper behavior. |
| #69 Implement desktop Insights screen | Complete | `DesktopInsightsScreen` renders aggregate data loaded from the desktop Room database through local rule-based state mapping, provider status, metric cards, top spending groups, insight cards, and empty/error/loading models. `DesktopInsightsStateMapperTest` covers mapper behavior. |
| #70 Document desktop demo limitations | Complete | This document lists run/build/test commands, supported screens, Room-backed local demo persistence, unsupported account/sync/passkey/AI/OCR/export behavior, Android parity limits, and future work boundaries. |
| #66 Create desktop target consuming shared logic | Complete for E-08 demo scope only | The desktop target exists and consumes the shared Room KMP database while keeping desktop UI mappers local to the demo. Full production shared KMP reuse for all Android domain, repository, dashboard, and feature logic remains tracked separately by the KMP extraction issues. |
| #232 Implement Room KMP shared database target and desktop persistence | Complete | `:core:database` is KMP-compatible with Android and desktop/JVM targets. Desktop creates and reopens a real local Room database at `~/.pocket-ledger/pocket-ledger.db`, and `PocketLedgerDesktopDatabaseTest` verifies persistence across database instances. |

## Run

From the repository root:

```powershell
.\androidApp\gradlew.bat :desktopApp:run --console=plain
```

From `androidApp/`:

```powershell
.\gradlew.bat :desktopApp:run --console=plain
```

## Compile And Test

From the repository root:

```powershell
.\androidApp\gradlew.bat projects --console=plain
.\androidApp\gradlew.bat :core:database:desktopTest :desktopApp:test :desktopApp:compileKotlin --console=plain
```

From `androidApp/`:

```powershell
.\gradlew.bat :core:database:desktopTest :desktopApp:test :desktopApp:compileKotlin --console=plain
```

## Supported Demo Screens

The desktop shell uses a simple navigation rail with `Search` and `Insights`.
`Dashboard` remains a placeholder so the demo can show navigation shape without
claiming full Android feature parity.

### Search

The desktop Search screen uses records loaded from the desktop Room database.
On first launch, `DesktopLedgerLocalDataSource` seeds deterministic demo records
only when the transaction table is empty. Later launches read the persisted
local database file instead of rebuilding process-only sample data.

Search supports:

- Keyword input.
- Keyword and local semantic fallback modes.
- Income and expense filters.
- Result list with no-results and empty-ledger states.
- Selected result detail preview.

Semantic mode is local rule-based fallback behavior only. It does not call
Gemini Nano, ML Kit, remote AI, embeddings services, or network APIs.

### Insights

The desktop Insights screen uses an aggregate snapshot derived from the desktop
Room database. It shows:

- Monthly summary for the current persisted demo ledger snapshot.
- Income, expense, and net overview.
- Top spending groups.
- Local insight cards and suggested actions.
- Provider status for deterministic local rule-based fallback behavior.
- Loading, empty, error, and content state rendering paths in the desktop UI
  model.

## Desktop Persistence Behavior

Desktop persistence is local-only and file-backed through Room KMP:

- Database module: `androidApp/core/database`.
- Shared schema, entities, DAOs, database class, and migrations:
  `androidApp/core/database/src/commonMain`.
- Desktop builder: `androidApp/core/database/src/desktopMain`.
- Desktop database path: `~/.pocket-ledger/pocket-ledger.db`.
- Desktop smoke test: `androidApp/core/database/src/desktopTest`.

The desktop app currently persists the local demo ledger used by Search and
Insights. It does not yet provide a full desktop transaction editor, account
profile, sync engine, import/export flow, or production desktop data management
surface.

## Shared And Core Logic

`:core:database` is now the shared Room KMP persistence boundary for Android and
desktop/JVM. Database construction remains platform-specific, while schema,
entities, DAOs, migrations, and database versioning stay in common source.

The desktop demo still keeps small JVM-compatible UI mappers in `:desktopApp`.
Feature screens do not depend directly on Room entities or DAOs; the desktop
entry point converts the local database snapshot into desktop UI models.

Current Android production repositories and feature flows still live in Android
modules and continue to use Room through `:core:data` and `:core:database`.
Broader KMP extraction of all domain/data/search/dashboard logic remains tracked
outside this desktop demo scope.

## E-08 vs E-07 Boundary

E-08 is the desktop demo target. Its closure standard is that a desktop JVM
module exists, builds independently, shows the requested Search and Insights
demo screens, persists its local demo ledger through Room KMP, and documents
honest demo limitations.

E-07 is the broader shared Kotlin Multiplatform extraction work. Full production
desktop reuse of every Android domain/data/search/dashboard workflow remains out
of scope for E-08 because several production modules are still Android-library
scoped. That work should remain tracked by the KMP issues: #12, #62, #63, #64,
and #65.

## Limitations

The desktop demo intentionally does not support:

- Cloud sync.
- Account or passkey flows.
- Real Gemini Nano, ML Kit, remote LLM, or cloud AI inference.
- OCR, import, or export.
- Full Android feature parity.
- Full desktop transaction editing or production desktop data management.
- Production account backup or multi-device storage.
- Network calls.

Desktop data is local Room-backed demo data. It must not be treated as a full
production desktop finance vault, and desktop code must not log personal finance
data.
