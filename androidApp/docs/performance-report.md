# Pocket Ledger Performance Benchmarks

Pocket Ledger uses a dedicated `:macrobenchmark` module for local, manual
Macrobenchmark runs. These benchmarks are performance evidence tools and are
not part of normal PR validation.

## Covered Scenarios

- Cold startup to initial dashboard display.
- Dashboard open/render from a cold app start.
- Transaction list navigation and scrolling with deterministic demo data.
- Offline search on deterministic demo data.

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

- T-E15-02 will add Baseline Profile generation.
- T-E15-03 will expand deterministic large dataset tooling.
- T-E15-04 will turn benchmark results into a performance findings report.
- T-E15-05 will handle R8/release optimization decisions.
- T-E15-06 will add a focused memory/perf pass.
- No benchmark results are recorded by this document; compare results only from
  repeat runs on the same named device, OS image, app commit, and build variant.

Do not add Macrobenchmark execution to default PR validation unless a future
manual or scheduled benchmark workflow is explicitly introduced.
