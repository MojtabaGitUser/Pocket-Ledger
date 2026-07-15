# Portfolio Testing And Performance Summary

This is the reviewer-facing summary for #139. Full detail lives in `androidApp/docs/testing-report.md` and `androidApp/docs/performance-report.md`.

## Automated Coverage Summary

| Layer | Evidence | Command |
| --- | --- | --- |
| Shared KMP rules | Common tests for shared validation, dashboard aggregation, and search ranking. | `.\androidApp\gradlew.bat :shared:allTests` |
| Core JVM tests | Data, AI, security, feature flags, background, and testing fixtures. | `.\androidApp\gradlew.bat testDebugUnitTest` |
| Feature JVM tests | Dashboard, search, transaction ViewModels and pure feature logic. | `.\androidApp\gradlew.bat :feature:dashboard:testDebugUnitTest :feature:search:testDebugUnitTest :feature:transaction:testDebugUnitTest` |
| Room KMP desktop tests | Database migration registry and desktop persistence behavior. | `.\androidApp\gradlew.bat :core:database:desktopTest` |
| Screenshot tests | Paparazzi adaptive UI matrix, large-font states, and visual baselines. | `.\androidApp\gradlew.bat verifyAdaptiveScreenshots` |
| Release build | Release/R8/resource shrinking validation. | `.\androidApp\gradlew.bat :app:assembleRelease` |
| Benchmark artifacts | Macrobenchmark and benchmark APK assembly. | `.\androidApp\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble` |

## Performance Evidence

Implemented performance infrastructure:

- `:macrobenchmark` module for startup and frame timing scenarios.
- Startup benchmark with `StartupTimingMetric`.
- Dashboard, transaction list, search, and large dataset frame timing scenarios.
- Baseline Profile generator covering startup, dashboard, transaction list, search, and settings paths.
- Benchmark build type modeled after release: non-debuggable, profileable, minified, resource-shrunk, logging disabled.
- Large deterministic benchmark dataset in `:core:testing` for repeatable local performance work.

Device-required commands:

```powershell
.\androidApp\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest
.\androidApp\gradlew.bat :app:generateReleaseBaselineProfile
```

## CI And Release Confidence

CI separates fast PR validation from heavier manual/scheduled validation:

- PR validation: lint, unit/shared tests, debug build, release build, benchmark artifact assembly.
- Screenshot and benchmark workflow: manual/scheduled screenshot verification and benchmark artifact assembly.
- Release candidate workflow: version/signing checks and release artifacts.
- Internal distribution workflow: Firebase App Distribution handoff for testers.

## Known Gaps To State Honestly

- Connected Android instrumentation tests require an attached emulator/device and are not default CI gates.
- Macrobenchmark metrics and Baseline Profile generation require an attached representative device/emulator.
- The repository documents performance infrastructure and commands, but should not claim lower-end-device performance numbers until named hardware runs are recorded.
- Play Store publication itself is a release operation outside this documentation batch.

## Portfolio Talking Points

Use this framing in interviews:

- The project does not pretend every validation layer can run locally without hardware.
- The important engineering signal is that the layers are separated, documented, and command-addressable.
- Fast feedback protects most regressions; connected/manual workflows are reserved for hardware-dependent confidence.
- Release/R8 validation and screenshot baselines make the sample more realistic than a basic CRUD demo.