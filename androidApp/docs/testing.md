# Pocket Ledger Testing

The current full testing and performance validation inventory is maintained in
[`testing-report.md`](testing-report.md).

## `:core:testing`

`:core:testing` centralizes shared test-only utilities for Pocket Ledger modules.
It is an Android library so JVM unit tests and Android instrumented tests can
depend on the same fixtures and fakes through Gradle test configurations.

Allowed usage:
- `testImplementation(project(":core:testing"))`
- `androidTestImplementation(project(":core:testing"))`

Disallowed usage:
- Do not add `implementation(project(":core:testing"))` to production source
  sets in app, feature, or core modules.
- Do not put production business logic in `:core:testing`.
- Do not make `:core:testing` depend on `:app` or feature modules.

Current contents:
- Deterministic ledger model fixtures in
  `com.mojtaba.pocketledger.core.testing.fixture`.
- In-memory fake repositories in
  `com.mojtaba.pocketledger.core.testing.repository`.
- `FakeFeatureFlagProvider` in
  `com.mojtaba.pocketledger.core.testing.featureflags`.
- `MainDispatcherRule` in
  `com.mojtaba.pocketledger.core.testing.coroutine`.

Fixtures use stable IDs, stable timestamps, USD currency, and minor-unit money
values. Prefer overriding fixture parameters in individual tests instead of
creating ad hoc duplicate builders.

Fake repositories implement the real `:core:data` repository contracts, expose
local-only sync state, and use `MutableStateFlow` so tests can assert reactive
updates without Room or Android framework dependencies.

Repository fake tests in `:core:testing` protect shared filter, sort, fixture,
and Flow behavior that feature modules reuse for dashboard, transaction, and
search tests. Add coverage there when changing fake repository semantics,
cross-module query behavior, or deterministic fixture data so feature tests do
not silently drift from the repository contracts they are exercising.

`FakeFeatureFlagProvider` implements the real `:core:featureflags` contract so
tests can enable, disable, and set typed flag values deterministically without
depending on `BuildConfig`, Android framework APIs, or remote configuration.

## Compose UI Tests

Prefer focused feature-module Compose tests over broad end-to-end flows. Screen
tests should render the composable under `PocketLedgerTheme`, drive user actions
through Compose UI APIs, and assert stable text, accessibility semantics, or
test tags that describe user-visible behavior.

Use deterministic fakes from `:core:testing` when a route or app-shell test needs
repositories. Do not use Room, WorkManager, network, sleeps, or wall-clock timing
for critical-flow UI tests unless the test is specifically validating that
integration.

Critical-flow coverage should be split by ownership:
- `:app` verifies adaptive navigation shell and top-level route wiring.
- Feature modules verify screen states, form validation, callbacks, filter
  behavior, list/detail interactions, and accessible action semantics.
- Adaptive list/detail behavior may use a small stateful harness when the real
  ViewModel behavior is already covered by unit tests.

Prefer accessibility semantics over arbitrary test tags when the same signal is
useful to users and tests. Add test tags only for stable containers or controls
that cannot be addressed reliably by text/content description.

When no emulator or device is attached, assemble Android test APKs for changed
modules. Run connected Android tests when a device is available.

## Room And Repository Integration Tests

Room integration tests live in `:core:database` under
`core/database/src/androidTest`. They use isolated in-memory databases for DAO
behavior and `MigrationTestHelper` with the committed schema JSON files under
`core/database/schemas` for file-backed migration validation. Coverage includes
transaction CRUD, date/category/tag/search queries, category and budget active
filters, transaction-tag relationships, budget period/category queries, Flow
emissions, foreign-key delete behavior, and the schema version 1 to current
migration path.

Local repository integration tests live in `:core:data` under
`core/data/src/androidTest`. They construct the real local repositories over a
real in-memory `PocketLedgerDatabase`, not fake repositories, and verify
local-source-first reads/writes, update/delete behavior, Flow emissions, search
filters, and category/tag/budget relationship behavior.

Run the focused suites with an attached emulator or device:

```bash
./gradlew :core:database:connectedDebugAndroidTest
./gradlew :core:data:connectedDebugAndroidTest
```

When no emulator or device is available, compile the Android test APKs instead:

```bash
./gradlew :core:database:assembleDebugAndroidTest
./gradlew :core:data:assembleDebugAndroidTest
```

Macrobenchmarks live in `:macrobenchmark` and are local/manual performance
checks, not default PR validation. Run guidance and device assumptions are
documented in `docs/performance-report.md`.

The macrobenchmark module has its own `benchmark` build type and should be run
against the app `benchmark` variant, not the debug app:

```bash
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
```

## Adaptive Screenshot Tests

Adaptive screenshot coverage uses Paparazzi in `:app` so one JVM-based test
suite can render the app shell and the dashboard, transaction, and search
feature screens without an emulator. The matrix lives under
`app/src/test/java/com/mojtaba/pocketledger/screenshot` and uses centralized
fixtures and device definitions:

- compact phone: Pixel 5 portrait
- compact phone landscape: Pixel 5 landscape
- medium tablet: Nexus 7 portrait
- expanded tablet: Pixel Tablet landscape
- foldable open: Pixel Fold open
- foldable closed: folded phone-sized Pixel Fold configuration
- desktop/freeform: 1440 x 1000 dp-style desktop window

The screenshot suite covers dashboard content, empty and error states;
transaction list, detail, missing, error, and adaptive list/detail states;
search initial, populated, empty-ledger, no-results, error, and filter-visible
states; adaptive navigation chrome; and key large-font variants at 1.3 and 1.5
font scale. `ThemeScreenshotMatrixTest` adds an explicit light/dark matrix over
compact phone and expanded tablet layouts for dashboard content, transaction
adaptive content, populated search, settings app-lock availability states, and
the locked app-lock screen.

Normal `test` tasks exclude screenshot tests to keep PR unit-test feedback
focused. Run screenshot verification explicitly:

```bash
./gradlew verifyAdaptiveScreenshots
```

Update goldens after an intentional UI change:

```bash
./gradlew recordAdaptiveScreenshots
```

Paparazzi stores committed PNG baselines under
`app/src/test/snapshots/images`. Failed verification diffs are written under
`app/build/paparazzi/failures`, and an HTML report is generated under
`app/build/reports/paparazzi/debug`.

When adding a screenshot case, prefer existing screen-level composables with
explicit UI state and deterministic fixtures from the screenshot package or
`:core:testing`. Avoid repository, Room, biometric hardware, network, current
time, random IDs, and animations. Add broad matrix dimensions only when they
protect a meaningful visual contract; otherwise add the smallest focused case
with a descriptive group and state name.

CI keeps screenshot verification off the default pull-request path to avoid
adding screenshot runtime to every PR. The PR Validation workflow can be run
manually with `verify_screenshots=true` to execute `verifyAdaptiveScreenshots`
on GitHub Actions.
