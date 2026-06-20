# Pocket Ledger Performance Benchmarks

Pocket Ledger uses a dedicated `:macrobenchmark` module for local, manual
Macrobenchmark runs. These benchmarks are performance evidence tools and are
not part of normal PR validation.

The same `:macrobenchmark` module also owns Baseline Profile generation. The
app consumes that module through the AndroidX Baseline Profile Gradle plugin so
generated profiles can be copied into `:app` and merged into release-style
builds.

## Covered Scenarios

- Cold startup to initial dashboard display.
- Dashboard open/render from a cold app start.
- Transaction list navigation and scrolling with deterministic demo data.
- Offline search on deterministic demo data.
- Baseline Profile generation for startup, seeded dashboard content,
  transaction list navigation and scrolling, search results, and settings.
- Large local dataset transaction scrolling and search with deterministic
  benchmark-only data.

The transaction and search benchmarks seed the existing demo dataset through a
benchmark-build-only setup activity. The seed path is idempotent, offline-only,
uses `demo-` prefixed records from `:core:data`, and is not present in normal
debug or release builds.

Large dataset benchmarks use a separate benchmark-only setup mode backed by
the pure generator in:

```text
core/testing/src/main/java/com/mojtaba/pocketledger/core/testing/performance/LargeBenchmarkDataset.kt
```

The app benchmark source set consumes that generator through
`benchmarkImplementation(project(":core:testing"))`, so normal debug and
release builds do not depend on the large dataset generator. The benchmark-only
setup activity resets the benchmark app database before seeding either small
demo data or large data, which keeps scenarios repeatable and prevents a prior
large run from polluting small-demo benchmark runs.

## Build Strategy

The app defines a `benchmark` build type initialized from `release`, signed with
the debug signing config for local installation, non-debuggable, profileable,
minified, resource-shrunk, and with privacy-safe logging disabled. It avoids
requiring release signing secrets while keeping benchmarked code close to
release behavior.

The Macrobenchmark module targets package `com.mojtaba.pocketledger` and the
`:app` project. It declares a matching `benchmark` build type so connected runs
install and measure the app `benchmark` variant. Benchmark results are generated
under:

```text
macrobenchmark/build/outputs/connected_android_test_additional_output/benchmark/
```

For benchmark builds, app-lock sensitive preferences use non-persistent
in-memory storage so a locally enabled biometric/app-lock setting cannot block
startup or navigation measurements. Release builds still use encrypted
preferences and biometric/system authentication normally; biometric/app-lock
performance is outside this setup task.

## Large Dataset Scenario

The large dataset contains:

- 6,000 deterministic local transactions.
- 10 categories: 2 income categories and 8 expense categories.
- 8 tags.
- 48 monthly budgets across the current benchmark history window.
- About 2,400 transaction-tag links.
- Multiple months of fixed UTC timestamps ending on June 20, 2026.
- Mixed income and expense transactions, recurring flags, stable IDs, stable
  amounts, synthetic merchant names, and search-friendly `LedgerMart` rows.

The dataset is generated without stochastic input, wall-clock reads, network,
cloud/account state, real AI, sensitive finance strings, or generated artifacts.
It uses `large-benchmark-` IDs and `large-benchmark` source values so benchmark
records are identifiable. Unit tests in `:core:testing` verify counts,
determinism, ID uniqueness, link integrity, and absence of known sensitive
financial-data examples.

The large benchmark lives in:

```text
macrobenchmark/src/main/java/com/mojtaba/pocketledger/macrobenchmark/LargeDatasetBenchmark.kt
```

`scrollAndSearchLargeDataset` seeds the large dataset, cold-launches the app,
waits for dashboard content, navigates to the transaction list, scrolls the
large-backed recent transaction list, opens search, searches for `LedgerMart`,
and scrolls search results. It uses `FrameTimingMetric` and stable
UIAutomator text/content-description/resource selectors.

## Baseline Profiles

Baseline Profile generation lives in:

```text
macrobenchmark/src/main/java/com/mojtaba/pocketledger/macrobenchmark/BaselineProfileGenerator.kt
```

The generator reuses the benchmark-only demo-data setup activity, launches the
real app, waits for stable UIAutomator selectors, opens the transaction list,
scrolls the transaction list, searches for deterministic demo data, opens the
search results list, and visits settings. It does not use network, cloud,
account, biometric prompts, personal financial data, arbitrary sleeps, or
release-only credentials.

The app applies the AndroidX Baseline Profile plugin and declares
`:macrobenchmark` as its profile producer. It also depends on
`androidx.profileinstaller:profileinstaller` so embedded profiles can be
installed from locally installed APKs on supported Android versions. Release
and benchmark builds merge available ART profile inputs through tasks such as
`mergeReleaseBaselineProfile`, `mergeReleaseArtProfile`,
`mergeBenchmarkArtProfile`, and the matching startup-profile merge tasks.

Generate and copy the release Baseline Profile into the app source set with an
attached representative device or emulator:

```powershell
.\gradlew.bat :app:generateReleaseBaselineProfile
```

The producer-side collection tasks discovered in this project are:

```powershell
.\gradlew.bat :macrobenchmark:collectNonMinifiedReleaseBaselineProfile
.\gradlew.bat :macrobenchmark:collectNonMinifiedBenchmarkBaselineProfile
```

Use `:app:generateReleaseBaselineProfile` for normal profile updates because it
runs the producer and app copy task together. Review the generated
`baseline-prof.txt` before committing it. Do not hand-write or fabricate profile
rules.

Verify profile application by assembling release-style builds and checking that
the merge tasks run:

```powershell
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:assembleBenchmark
```

For a more direct task inspection, run:

```powershell
.\gradlew.bat :app:tasks --all
.\gradlew.bat :macrobenchmark:tasks --all
```

Recommended device conditions are the same as Macrobenchmark runs: keep the
device plugged in, close unrelated apps, let thermal state settle, and use the
same named device, OS image, app commit, and build variant when comparing
results. A physical device is preferred; an emulator is acceptable for profile
generation when no physical device is available.

## Running Locally

Use a physical device when possible. Before running, keep the device plugged in,
close unrelated apps, let the device reach a stable thermal state, and consider
disabling system animations for more stable frame timing.

Assemble the benchmark artifacts:

```powershell
.\gradlew.bat :app:assembleBenchmark :macrobenchmark:assemble
```

Run connected benchmarks when a device or emulator is attached:

```powershell
.\gradlew.bat :macrobenchmark:connectedBenchmarkAndroidTest
```

Run only the large dataset benchmark class on a connected device or emulator:

```powershell
.\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mojtaba.pocketledger.macrobenchmark.LargeDatasetBenchmark
```

The aggregate connected check task is also available:

```powershell
.\gradlew.bat :macrobenchmark:connectedCheck
```

To inspect all module tasks:

```powershell
.\gradlew.bat :macrobenchmark:tasks
```

## Limitations And Follow-Ups

- T-E15-04 will turn benchmark results into a performance findings report.
- T-E15-05 will handle R8/release optimization decisions.
- T-E15-06 will add a focused memory/perf pass.
- No benchmark results are recorded by this document; compare results only from
  repeat runs on the same named device, OS image, app commit, and build variant.
- Baseline Profile files are only committed after a successful generation run
  on a suitable device or emulator.
- Large dataset benchmark numbers are not recorded here; compare them only from
  repeat runs on the same named device, OS image, app commit, and build variant.

Do not add Macrobenchmark execution to default PR validation unless a future
manual or scheduled benchmark workflow is explicitly introduced.
