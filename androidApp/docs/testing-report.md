# Pocket Ledger Testing Report

## Executive Summary

Pocket Ledger currently has a layered Android test strategy that gives solid
MVP confidence for local-first ledger behavior, feature ViewModels and pure
logic, adaptive UI rendering, release assembly, and R8 compatibility.

The strongest automated coverage is in JVM/local unit tests, Paparazzi
screenshot tests, and compile/build validation. Room DAO, repository, Compose
UI, encrypted preferences, and app-shell instrumentation tests are implemented
under `src/androidTest`, and their APKs compile in this environment, but they
require an attached emulator or device to execute.

Implemented layers:

- Shared unit-test infrastructure in `:core:testing`.
- Core and feature JVM unit tests across data, AI, security, dashboard, search,
  transaction, app shell, background scheduling, feature flags, design system,
  and shared KMP code.
- Room and local repository integration tests under Android instrumentation
  source sets.
- Paparazzi adaptive screenshot matrix in `:app`.
- Macrobenchmark module for startup, dashboard render, transaction scrolling,
  and search frame timing.
- Baseline Profile generation in the existing `:macrobenchmark` module.
- Large deterministic dataset generation and a large dataset Macrobenchmark
  scenario for local performance testing.
- Release/R8 validation through `:app:assembleRelease`.
- CI coverage through the PR Validation GitHub Actions workflow.

Important limitations:

- No startup, frame timing, jank, or benchmark numbers are recorded in this
  repository from the current validation run because no device or emulator was
  attached.
- Baseline Profile generation requires an attached device or emulator; profile
  files are only committed after a successful generation run.
- Large dataset benchmark execution also requires an attached device or
  emulator; no large dataset benchmark numbers are recorded in this report.
- Connected Android tests and macrobenchmarks were not executed in this
  environment.

## Scope

This report covers the implemented Android project under `androidApp/`,
including:

- `:app`.
- Core modules: `:core:ai`, `:core:background`, `:core:data`,
  `:core:database`, `:core:designsystem`, `:core:featureflags`,
  `:core:security`, and `:core:testing`.
- Feature modules: `:feature:dashboard`, `:feature:search`, and
  `:feature:transaction`.
- `:shared` KMP tests where they appear in the project and CI.
- Room database, DAO, migration, and local repository integration coverage.
- Adaptive UI screenshot coverage.
- Macrobenchmark and release-build performance validation setup.
- GitHub Actions PR validation.

Out of scope because the current codebase does not implement them as production
flows:

- Cloud sync.
- Remote account, passkey, or server authentication behavior.
- Banking integrations, OCR imports, and export flows.
- Real remote AI inference or network-hosted AI ranking.
- Production device-lab testing.
- Production benchmark dashboards or historical performance trend storage.

## Test Suite Inventory

Commands are shown from the `androidApp` directory because that is where the
Windows Gradle wrapper lives and where CI runs Gradle.

| Module | Test type | Source set | Purpose | Command | Runtime | Device required | Current status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `:core:testing` | Shared fake/fixture unit tests | `src/test` | Protect deterministic fixtures, large benchmark dataset generation, fake repositories, fake feature flags, and scheduler test helpers. | `.\gradlew.bat :core:testing:testDebugUnitTest` | JVM/local | No | Passed in this validation run. |
| `:core:database` | Migration registry unit test | `src/test` | Verify migration metadata and current-version registration. | `.\gradlew.bat :core:database:testDebugUnitTest` | JVM/local | No | Passed in this validation run. |
| `:core:database` | Room DAO and migration integration tests | `src/androidTest` | Verify DAO CRUD/query/Flow behavior and Room migration path using in-memory databases and schema assets. | `.\gradlew.bat :core:database:connectedDebugAndroidTest` | Android instrumentation | Yes | Not run because no device was attached. APK compiled with `:core:database:assembleDebugAndroidTest`. |
| `:core:data` | Search model unit tests | `src/test` | Verify `SearchQuery`, filter normalization, validation, and deterministic query behavior. | `.\gradlew.bat :core:data:testDebugUnitTest` | JVM/local | No | Passed in this validation run. |
| `:core:data` | Local repository integration tests | `src/androidTest` | Verify local repositories over real in-memory Room database, Flow emissions, sync state, and seed data. | `.\gradlew.bat :core:data:connectedDebugAndroidTest` | Android instrumentation | Yes | Not run because no device was attached. APK compiled with `:core:data:assembleDebugAndroidTest`. |
| `:core:ai` | AI selector unit tests | `src/test` | Verify provider selection, disabled-feature fallbacks, and local fallback behavior. | `.\gradlew.bat :core:ai:testDebugUnitTest` | JVM/local | No | Passed in this validation run. |
| `:core:security` | Security unit tests | `src/test` | Verify app-lock manager, in-memory sensitive preferences, logging policy, logger, and redaction behavior. | `.\gradlew.bat :core:security:testDebugUnitTest` | JVM/local | No | Passed in this validation run. |
| `:core:security` | Encrypted preferences instrumentation | `src/androidTest` | Verify AndroidX Security encrypted preference behavior. | `.\gradlew.bat :core:security:connectedDebugAndroidTest` | Android instrumentation | Yes | Not run because no device was attached. APK compiled with `:core:security:assembleDebugAndroidTest`. |
| `:feature:dashboard` | Feature unit tests | `src/test` | Verify dashboard summary calculation, generator fallback, formatters, layout mode, budget validation, and ViewModel behavior. | `.\gradlew.bat :feature:dashboard:testDebugUnitTest` | JVM/local | No | Passed in this validation run. |
| `:feature:dashboard` | Compose UI instrumentation | `src/androidTest` | Verify dashboard screen, dashboard components, and budget setup screen behavior. | `.\gradlew.bat :feature:dashboard:connectedDebugAndroidTest` | Android instrumentation | Yes | Not run because no device was attached. APK compiled with `:feature:dashboard:assembleDebugAndroidTest`. |
| `:feature:search` | Feature unit tests | `src/test` | Verify search ViewModel and result mapping behavior. | `.\gradlew.bat :feature:search:testDebugUnitTest` | JVM/local | No | Passed in this validation run. |
| `:feature:search` | Compose UI instrumentation | `src/androidTest` | Verify search screen behavior. | `.\gradlew.bat :feature:search:connectedDebugAndroidTest` | Android instrumentation | Yes | Not run because no device was attached. APK compiled with `:feature:search:assembleDebugAndroidTest`. |
| `:feature:transaction` | Feature unit tests | `src/test` | Verify transaction form validation, list/detail/editor ViewModels, and adaptive selection state. | `.\gradlew.bat :feature:transaction:testDebugUnitTest` | JVM/local | No | Passed in this validation run. |
| `:feature:transaction` | Compose UI instrumentation | `src/androidTest` | Verify list, detail, editor, and adaptive list/detail screen behavior. | `.\gradlew.bat :feature:transaction:connectedDebugAndroidTest` | Android instrumentation | Yes | Not run because no device was attached. APK compiled with `:feature:transaction:assembleDebugAndroidTest`. |
| `:app` | App unit tests | `src/test` | Verify app-local adaptive, foldable, WorkManager mapping, and non-screenshot JVM behavior. | `.\gradlew.bat :app:testDebugUnitTest` | JVM/local | No | Passed in this validation run. Screenshot tests are excluded from normal `test` tasks by Gradle configuration. |
| `:app` | App-shell instrumentation | `src/androidTest` | Verify adaptive navigation shell and top-level route wiring. | `.\gradlew.bat :app:connectedDebugAndroidTest` | Android instrumentation | Yes | Not run because no device was attached. APK compiled with `:app:assembleDebugAndroidTest`. |
| `:app` | Paparazzi screenshot matrix | `src/test/java/.../screenshot` plus `src/test/snapshots/images` | Verify adaptive UI visual baselines for app shell, dashboard, search, transaction, theme, and large-font states. | `.\gradlew.bat verifyAdaptiveScreenshots` | JVM screenshot | No | Passed in this validation run. |
| `:macrobenchmark` | Macrobenchmarks and Baseline Profile generation | `src/main` | Measure cold startup, dashboard render, transaction scrolling, search frame timing, large dataset list/search behavior, and release Baseline Profile generation. | `.\gradlew.bat :macrobenchmark:connectedBenchmarkAndroidTest` / `.\gradlew.bat :app:generateReleaseBaselineProfile` | Benchmark instrumentation | Yes | Not run because no device was attached. Artifacts compiled with `.\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble`. |
| `:app` | Debug build validation | main source sets | Verify debug app assembly. | `.\gradlew.bat :app:assembleDebug` | Build | No | Passed in this validation run. |
| `:app` | Release/R8 validation | main source sets | Verify release build, resource shrinking, and R8 minification. | `.\gradlew.bat :app:assembleRelease` | Build | No | Passed in this validation run. |

## Shared Unit Testing Coverage

`:core:testing` centralizes reusable test-only helpers:

- `TestLedgerFixtures`, `TestIds`, and `TestClock` provide stable ledger
  records, IDs, timestamps, currency codes, and minor-unit money values.
- `LargeBenchmarkDataset` provides 6,000 stable synthetic local transactions,
  10 categories, 8 tags, 48 monthly budgets, and about 2,400 transaction-tag
  links for benchmark-only large dataset seeding.
- `FakeTransactionRepository`, `FakeCategoryRepository`, `FakeTagRepository`,
  and `FakeBudgetRepository` implement the real `:core:data` repository
  contracts over `MutableStateFlow`.
- `FakeFeatureFlagProvider` implements the real `:core:featureflags`
  provider contract with typed deterministic overrides.
- `MainDispatcherRule` supports coroutine ViewModel tests.
- `FakeScheduler` supports background scheduling tests without WorkManager.

Shared tests protect fixture stability, fake repository filtering/sorting/Flow
semantics, feature-flag overrides, and scheduler behavior. This reduces
regression risk in feature tests because dashboard, search, and transaction
tests reuse the same contract-shaped fakes instead of ad hoc mocks.

This layer also helps performance stability indirectly: deterministic fake data
and coroutine dispatching keep unit tests fast, local, and free of Room,
WorkManager, network, sleeps, random IDs, and wall-clock timing.

## Room And Repository Integration Coverage

Room integration tests live in `:core:database` under `src/androidTest`. They
construct isolated in-memory `PocketLedgerDatabase` instances for DAO behavior.
Migration coverage uses Room `MigrationTestHelper` with committed schema JSON
assets under `core/database/schemas`.

DAO coverage includes:

- Transaction CRUD and queries.
- Category active filtering.
- Tag lookup and relationships.
- Budget active, period, and category queries.
- Transaction-tag relationships.
- Flow emission behavior.
- Foreign-key delete behavior.
- Version 1 to current migration path.

Local repository integration tests live in `:core:data` under `src/androidTest`.
They use real local repositories backed by a real in-memory Room database. The
implemented tests cover:

- Local-source-first reads and writes.
- Update and delete behavior.
- Flow emissions from repository operations.
- Transaction, category, tag, and budget relationships.
- Local repository sync-state behavior.
- Search filters over persisted data.
- Deterministic demo data seeding.

Current validation status:

- `:core:database:assembleDebugAndroidTest` passed.
- `:core:data:assembleDebugAndroidTest` passed.
- `:core:database:connectedDebugAndroidTest` and
  `:core:data:connectedDebugAndroidTest` were not run because `adb devices`
  reported no attached device or emulator.

Known gaps:

- Connected Room/repository integration tests need to be run before treating
  the persistence layer as device-verified.
- Migration coverage currently reflects the committed schema history; future
  schema versions must add explicit migration tests and committed schema JSON.

## Screenshot Test Matrix Coverage

Adaptive screenshot coverage uses Paparazzi 2.0.0-alpha05 in `:app`.
Screenshot tests live under:

```text
app/src/test/java/com/mojtaba/pocketledger/screenshot
```

Committed PNG baselines live under:

```text
app/src/test/snapshots/images
```

The repository currently contains 155 committed snapshot images.

Screens and states covered:

- Dashboard content, empty, and error states.
- Transaction list, detail, missing, error, adaptive list/detail, and no
  selection states.
- Search initial, populated, empty-ledger, no-results, error, and
  filter-visible states.
- Adaptive navigation shell.
- Settings app-lock availability and app-lock locked states through the theme
  matrix.
- Large-font variants for dashboard, search, and transaction list/detail.

Device and adaptive coverage:

- Compact phone portrait.
- Compact phone landscape.
- Medium tablet.
- Expanded tablet.
- Pixel Fold open.
- Pixel Fold closed.
- Desktop/freeform window.
- Explicit light/dark matrix for selected key states on compact and expanded
  layouts.
- Font scale 1.3 and 1.5 for selected accessibility cases.

Determinism strategy:

- Screenshot tests use screen-level composables and explicit UI state.
- Data comes from `ScreenshotTestData` and shared deterministic fixtures.
- The suite avoids Room, repository IO, biometric hardware, network, current
  time, random IDs, and animations.

Commands:

```powershell
.\gradlew.bat verifyAdaptiveScreenshots
.\gradlew.bat recordAdaptiveScreenshots
.\gradlew.bat :app:verifyPaparazziDebug
.\gradlew.bat :app:recordPaparazziDebug
```

Current validation status:

- `.\gradlew.bat verifyAdaptiveScreenshots` passed in this validation run.
- Paparazzi generated its report at
  `app/build/reports/paparazzi/debug/index.html`.

Current limitations:

- Screenshot verification is not part of default pull-request execution.
- CI only runs screenshots when the PR Validation workflow is manually
  dispatched with `verify_screenshots=true`.
- Paparazzi validates static rendering, not touch interaction, TalkBack
  traversal, runtime animation smoothness, or real-device rendering.

## Performance And Startup Validation

Implemented performance validation:

- `:macrobenchmark` module exists.
- `:app` defines a `benchmark` build type initialized from `release`, signed
  with debug signing, non-debuggable, profileable, minified, resource-shrunk,
  and with logging disabled.
- `:macrobenchmark` defines a matching `benchmark` build type so
  `connectedBenchmarkAndroidTest` installs and measures the app benchmark
  variant.
- Benchmark-only setup activity exists under `app/src/benchmark` for
  deterministic demo-data seeding.
- Macrobenchmark tests cover:
  - Cold startup with `StartupTimingMetric`.
  - Dashboard render/open path with `FrameTimingMetric`.
  - Transaction list navigation and scroll with `FrameTimingMetric`.
  - Search path with `FrameTimingMetric`.
  - Large dataset transaction list scrolling and keyword search with
    `FrameTimingMetric`.
- Transaction and search benchmark scenarios seed deterministic demo data.
- Large dataset benchmark scenarios seed deterministic `large-benchmark-`
  prefixed data through a benchmark-only setup mode.
- Benchmark builds use non-persistent app-lock sensitive preferences so a local
  biometric/app-lock setting cannot block benchmark startup or navigation.

Current validation status:

- `.\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble` passed.
- `adb devices` returned no attached devices, so
  `.\gradlew.bat :macrobenchmark:connectedBenchmarkAndroidTest` was not run.
- No startup timing, frame timing, scroll jank, or lower-end-device metrics
  were produced in this validation run.

Baseline Profiles:

- Baseline Profile generation is implemented in the existing `:macrobenchmark`
  module with `BaselineProfileGenerator`.
- The app applies the AndroidX Baseline Profile plugin, consumes
  `:macrobenchmark` through the `baselineProfile` configuration, and includes
  `androidx.profileinstaller` for local APK profile installation support.
- The generator covers seeded startup/dashboard content, transaction list
  navigation and scrolling, search results, and settings.
- App-lock biometric/system authentication is out of scope for profile
  generation. Benchmark builds use non-persistent sensitive preferences so
  app-lock cannot block profile collection, while release builds keep encrypted
  preferences and normal authentication behavior.
- Release and benchmark builds execute profile merge tasks such as
  `mergeReleaseBaselineProfile`, `mergeReleaseArtProfile`, and
  `mergeBenchmarkArtProfile` when profiles are available.
- Profile files are only committed after
  `.\gradlew.bat :app:generateReleaseBaselineProfile` succeeds on a suitable
  device or emulator.

Startup metrics:

- No current startup metric values are recorded in this repository.
- To produce startup metrics, attach a representative physical device or
  emulator and run:

```powershell
.\gradlew.bat :macrobenchmark:connectedBenchmarkAndroidTest
```

Scroll jank and recomposition:

- Scroll/frame timing coverage exists through the transaction list
  macrobenchmark and large dataset macrobenchmark, but they were not executed
  in this environment.
- No separate recomposition benchmark or Compose recomposition report artifact
  was found.
- Current safeguards are mostly architectural and test-driven: deterministic
  UI state, ViewModel unit tests, adaptive screenshot coverage, and benchmark
  scenarios for frame timing.

Lower-end-device confidence:

- Not established by this validation run. The report should not claim
  lower-end-device performance confidence until benchmarks are run on named
  hardware and results are recorded.

## Release Build And R8 Validation

`:app` release configuration:

- `isDebuggable = false`.
- `isMinifyEnabled = true`.
- `isShrinkResources = true`.
- `LOGGING_ENABLED = false`.
- Uses `getDefaultProguardFile("proguard-android-optimize.txt")` and
  `app/proguard-rules.pro`.
- Uses release signing only when all release signing Gradle properties are
  present.

R8 and dependency notes:

- The security model documents a previous release R8 missing-class issue from
  AndroidX Security Crypto's Tink dependency.
- The project resolves those annotation references through real dependencies
  declared in the version catalog and `:core:security`:
  `error_prone_annotations` and `jsr305`.
- The project does not use broad `-dontwarn **` rules for this issue.

Current validation status:

- `.\gradlew.bat :app:assembleRelease` passed in this validation run.
- The output included `:app:minifyReleaseWithR8`.
- The output included release lint vital tasks.

CI memory configuration:

- `gradle.properties` sets `org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g`.
- The PR Validation workflow sets `GRADLE_OPTS=-Dorg.gradle.workers.max=2`.
- These settings reduce memory pressure during full CI builds and release/R8
  work.

Remaining risks:

- Release signing was not validated unless local signing properties were
  present.
- Runtime behavior of the minified release APK was not smoke-tested on a
  device in this validation run.
- Future R8 missing-class rules need specific dependency analysis rather than
  broad warning suppression.

## CI Validation

Workflow file:

```text
.github/workflows/pr-validation.yml
```

Triggers:

- Pull requests targeting `dev` or `main`.
- Manual `workflow_dispatch` with optional `verify_screenshots` boolean input.

CI working directory:

```text
androidApp
```

Default CI commands:

```bash
./gradlew projects
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew :app:compileDebugKotlin :app:compileReleaseKotlin
./gradlew :core:designsystem:assembleDebug
./gradlew :shared:compileKotlinMetadata :shared:compileAndroidMain
./gradlew :shared:allTests
./gradlew clean build
```

Manual screenshot CI command:

```bash
./gradlew verifyAdaptiveScreenshots
```

CI characteristics:

- CI validates debug and release app assembly.
- CI runs shared KMP tests.
- CI runs full `clean build`, which includes available non-connected local
  checks.
- CI does not run connected Android instrumentation tests by default.
- CI does not run macrobenchmarks by default.
- Screenshot verification is opt-in through manual workflow dispatch.
- No emulator/device service is configured in the current workflow.

Known CI limitations:

- Room/repository instrumentation tests are not device-executed in default CI.
- Compose UI instrumentation tests are not device-executed in default CI.
- Macrobenchmark startup and frame metrics are not generated in CI.
- Screenshot regressions are only caught when the manual screenshot option is
  used.

## Risk Matrix

| Risk | Current mitigation | Remaining gap | Severity |
| --- | --- | --- | --- |
| Room migration regression | Room schema assets, migration registration unit test, migration instrumentation test. | Connected migration test was not run in this environment. Future schema versions need new migration tests. | High |
| Repository Flow emission regression | `:core:data` instrumentation tests over real in-memory Room plus shared fake Flow tests. | Connected repository integration tests require emulator/device. | High |
| UI visual regression | Paparazzi adaptive matrix with 155 committed snapshots. | Screenshots are opt-in in CI and do not validate runtime interaction. | Medium |
| Startup regression | `StartupBenchmark` with `StartupTimingMetric` and `BaselineProfileGenerator` for startup/navigation profile coverage. | No device run, stored startup metrics, or generated profile artifact in this validation. | High |
| Scroll jank regression | Transaction list macrobenchmark and large dataset macrobenchmark with `FrameTimingMetric`. | No connected benchmark run in this validation and no threshold/trend storage. | High |
| Recomposition regression | ViewModel unit tests, deterministic state, adaptive screenshots, and architectural separation. | No explicit recomposition benchmark/report found. | Medium |
| Release/R8 regression | `:app:assembleRelease` with minification and resource shrinking; CI also runs release assemble. | Minified APK runtime smoke test was not run on device. | High |
| CI memory pressure | `-Xmx4g`, 1 GB metaspace, and CI worker cap of 2. | Full `clean build` can still be memory-intensive as modules and screenshots grow. | Medium |
| Screenshot flakiness | Deterministic fake data, Paparazzi JVM rendering, excluded from normal unit tasks. | Paparazzi version is alpha and screenshot verification is manual in CI. | Medium |
| Missing device/emulator for benchmarks | Benchmark artifacts compile locally. | Real startup, jank, and lower-end-device evidence requires attached hardware. | High |
| Sensitive data in tests | Deterministic fake data and docs require avoiding personal financial data. | Future contributors can still add realistic sensitive strings unless reviewed. | Medium |

## Developer Checklist

- Add unit tests for shared logic, validation, state reducers, formatters, and
  repository-independent behavior.
- Add or update `:core:testing` fixtures/fakes when feature tests need shared
  deterministic data or repository behavior.
- Add repository integration tests for persistence changes.
- Add or update Room DAO and migration tests for schema changes.
- Commit new Room schema JSON when changing the database schema.
- Update screenshots for intentional UI changes with
  `.\gradlew.bat recordAdaptiveScreenshots`.
- Verify screenshots with `.\gradlew.bat verifyAdaptiveScreenshots`.
- Run `.\gradlew.bat :app:assembleRelease` before merging release-path,
  dependency, security, or shrinker-sensitive changes.
- Run benchmark/profile tasks when touching startup, navigation, app shell,
  transaction list scrolling, search, or dashboard first render.
- Avoid personal financial data in tests, screenshots, logs, and benchmark
  fixtures.
- Keep fake data deterministic: stable IDs, stable timestamps, stable currency,
  and no random or wall-clock input.
- Document emulator/device requirements for instrumentation or benchmark-only
  validation.
- Update this report when the test strategy, CI workflow, screenshot matrix,
  benchmark setup, or release validation changes.

## Commands Appendix

Run all commands from `androidApp`.

Focused unit tests:

```powershell
.\gradlew.bat :core:testing:testDebugUnitTest
.\gradlew.bat :core:database:testDebugUnitTest
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :core:ai:testDebugUnitTest
.\gradlew.bat :core:security:testDebugUnitTest
.\gradlew.bat :feature:dashboard:testDebugUnitTest
.\gradlew.bat :feature:search:testDebugUnitTest
.\gradlew.bat :feature:transaction:testDebugUnitTest
.\gradlew.bat :app:testDebugUnitTest
```

Additional local suites used by CI:

```powershell
.\gradlew.bat :shared:allTests
.\gradlew.bat :shared:compileKotlinMetadata :shared:compileAndroidMain
.\gradlew.bat :core:designsystem:assembleDebug
```

Database and repository instrumentation tests with an attached device or
emulator:

```powershell
.\gradlew.bat :core:database:connectedDebugAndroidTest
.\gradlew.bat :core:data:connectedDebugAndroidTest
```

Compile Android test APKs when no device is available:

```powershell
.\gradlew.bat :core:database:assembleDebugAndroidTest
.\gradlew.bat :core:data:assembleDebugAndroidTest
.\gradlew.bat :core:security:assembleDebugAndroidTest
.\gradlew.bat :feature:dashboard:assembleDebugAndroidTest
.\gradlew.bat :feature:search:assembleDebugAndroidTest
.\gradlew.bat :feature:transaction:assembleDebugAndroidTest
.\gradlew.bat :app:assembleDebugAndroidTest
```

Screenshot verification and recording:

```powershell
.\gradlew.bat verifyAdaptiveScreenshots
.\gradlew.bat recordAdaptiveScreenshots
.\gradlew.bat :app:verifyPaparazziDebug
.\gradlew.bat :app:recordPaparazziDebug
```

Macrobenchmark artifact assembly:

```powershell
.\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble
```

Macrobenchmark execution with an attached representative device or emulator:

```powershell
.\gradlew.bat :macrobenchmark:connectedBenchmarkAndroidTest
.\gradlew.bat :macrobenchmark:connectedCheck
```

Focused large dataset benchmark execution with an attached representative
device or emulator:

```powershell
.\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mojtaba.pocketledger.macrobenchmark.LargeDatasetBenchmark
```

Baseline Profile generation with an attached representative device or emulator:

```powershell
.\gradlew.bat :app:generateReleaseBaselineProfile
.\gradlew.bat :macrobenchmark:collectNonMinifiedReleaseBaselineProfile
.\gradlew.bat :macrobenchmark:collectNonMinifiedBenchmarkBaselineProfile
```

Baseline Profile task discovery and release merge verification:

```powershell
.\gradlew.bat :app:tasks --all
.\gradlew.bat :macrobenchmark:tasks --all
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:assembleBenchmark
```

Debug and release build validation:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
```

Full CI-equivalent local validation:

```powershell
.\gradlew.bat projects
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:compileDebugKotlin :app:compileReleaseKotlin
.\gradlew.bat :core:designsystem:assembleDebug
.\gradlew.bat :shared:compileKotlinMetadata :shared:compileAndroidMain
.\gradlew.bat :shared:allTests
.\gradlew.bat clean build
```

Optional full local validation including screenshots:

```powershell
.\gradlew.bat clean build verifyAdaptiveScreenshots
```

## Validation Results From This Report Update

Commands run successfully:

```powershell
.\gradlew.bat :core:testing:testDebugUnitTest
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :core:database:testDebugUnitTest
.\gradlew.bat :macrobenchmark:assemble
.\gradlew.bat :app:assembleBenchmark
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
```

Discovery commands run:

```powershell
git branch --show-current
git status --short
rg -n "test|testing|unit|integration|Room|repository|screenshot|Paparazzi|macrobenchmark|baseline profile|BaselineProfile|startup|Startup|R8|minify|jank|recomposition|performance|benchmark|assembleRelease|assembleDebug|CI|workflow|gradle" .
rg --files . | rg "test|androidTest|benchmark|macrobenchmark|baseline|profile|screenshot|paparazzi|docs|workflow|README|gradle"
adb devices
.\gradlew.bat :macrobenchmark:tasks --all --console=plain
.\gradlew.bat :app:tasks --all --console=plain
```

Commands not run because no device or emulator was attached:

```powershell
.\gradlew.bat :core:database:connectedDebugAndroidTest
.\gradlew.bat :core:data:connectedDebugAndroidTest
.\gradlew.bat :core:security:connectedDebugAndroidTest
.\gradlew.bat :feature:dashboard:connectedDebugAndroidTest
.\gradlew.bat :feature:search:connectedDebugAndroidTest
.\gradlew.bat :feature:transaction:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :macrobenchmark:connectedBenchmarkAndroidTest
.\gradlew.bat :macrobenchmark:connectedCheck
.\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mojtaba.pocketledger.macrobenchmark.LargeDatasetBenchmark
.\gradlew.bat :app:generateReleaseBaselineProfile
.\gradlew.bat :macrobenchmark:collectNonMinifiedReleaseBaselineProfile
.\gradlew.bat :macrobenchmark:collectNonMinifiedBenchmarkBaselineProfile
```

Backlog source note:

- `Pocket_Ledger_Complete_Backlog.docx` was not found in the repository during
  this report update. The report uses the GitHub task description, parent
  story context, existing docs, current code, tests, Gradle setup, CI workflow,
  and validation commands as source of truth.
