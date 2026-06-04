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
- `MainDispatcherRule` in
  `com.mojtaba.pocketledger.core.testing.coroutine`.

Fixtures use stable IDs, stable timestamps, USD currency, and minor-unit money
values. Prefer overriding fixture parameters in individual tests instead of
creating ad hoc duplicate builders.

Fake repositories implement the real `:core:data` repository contracts, expose
local-only sync state, and use `MutableStateFlow` so tests can assert reactive
updates without Room or Android framework dependencies.
