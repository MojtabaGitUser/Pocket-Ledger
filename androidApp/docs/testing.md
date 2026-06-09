# Pocket Ledger Testing

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
