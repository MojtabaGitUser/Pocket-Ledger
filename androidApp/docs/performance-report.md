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

The transaction and search benchmarks seed the existing demo dataset through a
benchmark-build-only setup activity. The seed path is idempotent, offline-only,
uses `demo-` prefixed records from `:core:data`, and is not present in normal
debug or release builds.

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

The aggregate connected check task is also available:

```powershell
.\gradlew.bat :macrobenchmark:connectedCheck
```

To inspect all module tasks:

```powershell
.\gradlew.bat :macrobenchmark:tasks
```

## Limitations And Follow-Ups

- T-E15-03 will expand deterministic large dataset tooling.
- T-E15-04 will turn benchmark results into a performance findings report.
- T-E15-05 will handle R8/release optimization decisions.
- T-E15-06 will add a focused memory/perf pass.
- No benchmark results are recorded by this document; compare results only from
  repeat runs on the same named device, OS image, app commit, and build variant.
- Baseline Profile files are only committed after a successful generation run
  on a suitable device or emulator.

Do not add Macrobenchmark execution to default PR validation unless a future
manual or scheduled benchmark workflow is explicitly introduced.
