# Desktop Demo

Pocket Ledger includes a Compose Multiplatform desktop demo in `:desktopApp`.
It is a JVM desktop target for E-08 and is intentionally a focused demo, not a
full product-grade desktop app.

Related GitHub scope:

- #66 TS-E08-01 - Create desktop target consuming shared logic.
- #67 T-E08-01 - Add desktop demo module.
- #68 T-E08-02 - Implement desktop Search screen.
- #69 T-E08-03 - Implement desktop Insights screen.
- #70 T-E08-04 - Document desktop demo limitations.

## Issue Traceability

| Issue | Status | Evidence |
| --- | --- | --- |
| #67 Add desktop demo module | Complete | `:desktopApp` is registered in `androidApp/settings.gradle.kts`, has its own JVM Compose Desktop Gradle module, and declares `com.mojtaba.pocketledger.desktop.MainKt` as the desktop entry point. |
| #68 Implement desktop Search screen | Complete | `DesktopSearchScreen` provides keyword input, keyword/local semantic modes, income and expense filters, deterministic sample records, result list states, and selected result preview. `DesktopSearchMapperTest` covers mapper behavior. |
| #69 Implement desktop Insights screen | Complete | `DesktopInsightsScreen` renders deterministic aggregate sample data through local rule-based state mapping, provider status, metric cards, top spending groups, insight cards, and empty/error/loading models. `DesktopInsightsStateMapperTest` covers mapper behavior. |
| #70 Document desktop demo limitations | Complete | This document lists run/build/test commands, supported screens, deterministic sample data, unsupported account/sync/passkey/AI/OCR/export/persistence behavior, Android parity limits, and future work boundaries. |
| #66 Create desktop target consuming shared logic | Complete for E-08 demo scope only | The desktop target exists and follows the safe shared/demo boundary available today: JVM-compatible local mappers mirror Android search and E-12 Insights concepts without depending on Android-only modules. Full production shared KMP reuse belongs to E-07 (#12/#62-#65). |

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
.\androidApp\gradlew.bat :desktopApp:test :desktopApp:build --console=plain
```

## Supported Demo Screens

The desktop shell uses a simple navigation rail with `Search` and `Insights`.
`Dashboard` remains a placeholder so the demo can show navigation shape without
claiming full Android feature parity.

### Search

The desktop Search screen uses deterministic in-memory sample transactions. It
supports:

- Keyword input.
- Keyword and local semantic fallback modes.
- Income and expense filters.
- Result list with no-results and empty-ledger states.
- Selected result detail preview.

Semantic mode is local rule-based fallback behavior only. It does not call
Gemini Nano, ML Kit, remote AI, embeddings services, or network APIs.

### Insights

The desktop Insights screen uses deterministic sample aggregate data. It shows:

- Monthly summary for the sample demo period.
- Income, expense, and net overview.
- Top spending groups.
- Local insight cards and suggested actions.
- Provider status for deterministic local rule-based fallback behavior.
- Loading, empty, error, and content state rendering paths in the desktop UI
  model.

## Shared And Core Logic

The desktop demo follows existing Android search and E-12 Insights concepts:
normalized queries, transaction type filtering, local fallback ranking,
aggregate-only insight inputs, provider status, and privacy-safe state mapping.

Current reusable E-12 AI providers, core data repositories, dashboard UI, and
Android search UI live in Android-library modules. The desktop JVM target cannot
depend on those modules without a larger KMP extraction. For this E-08 demo, the
desktop module keeps small JVM-compatible mappers in `:desktopApp` and uses
sample-safe deterministic fixtures.

## E-08 vs E-07 Boundary

E-08 is the desktop demo target. Its closure standard is that a desktop JVM
module exists, builds independently, shows the requested Search and Insights
demo screens, and documents honest demo limitations.

E-07 is the shared Kotlin Multiplatform extraction work. Full production desktop
reuse of Android domain/data/search/dashboard logic remains out of scope for
E-08 because the relevant production modules are still Android-library scoped.
That work should remain tracked by the KMP issues: #12, #62, #63, #64, and #65.

## Limitations

The desktop demo intentionally does not support:

- Cloud sync.
- Account or passkey flows.
- Real Gemini Nano, ML Kit, remote LLM, or cloud AI inference.
- OCR, import, or export.
- Full Android feature parity.
- Full desktop persistence or Room-on-desktop.
- Production user data storage.
- Network calls.

The demo data is deterministic local sample data. It must not be treated as real
personal finance data, and desktop code must not log personal finance data.
