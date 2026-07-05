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

## T-E15-04 Recomposition And Jank Review

This pass reviewed the implemented Compose surfaces that matter most for
startup, scroll, and frame timing:

- App entry and shell: `MainActivity`, `PocketLedgerApp`,
  `PocketLedgerAppShell`, adaptive navigation, app lock gate, and settings.
- Dashboard: route/ViewModel, adaptive dashboard content, recent transactions,
  budget progress, top spending, insights, and metric cards.
- Transactions: list route/ViewModel, list screen, list item rows, adaptive
  list/detail entry points, and transaction detail tag rows.
- Search: route/ViewModel, filter bar, result list, result rows, and mapping
  from local ledger models to UI models.
- Benchmark infrastructure: startup, dashboard, transaction list scroll,
  search, large dataset, baseline profile generation, benchmark setup helpers,
  app benchmark build type, and benchmark app-lock behavior.

Findings:

- Top-level transaction and search result lists already used stable item keys
  and stable benchmark test tags (`TransactionList`, `SearchResultsList`).
- Nested tag chip rows in transaction and search results did not provide item
  keys. Keys were added so chip identity is stable when rows recompose.
- Transaction and search result rows were rebuilding subtitle and accessibility
  strings during composition. Those row-local derived strings now use
  `remember` keyed by the immutable row model and selected state.
- Dashboard recent transactions repeatedly took the first five rows and
  formatted amount/date/subtitle values during composition. The card now maps
  the visible display rows once per transaction-list change.
- Route and app-lock/settings `StateFlow` collection used plain
  `collectAsState`. These collectors now use lifecycle-aware collection so
  backgrounded screens do not keep UI collection active unnecessarily.

No benchmark numbers were produced for this review because `adb devices`
reported no attached emulator or device. Runtime startup, frame timing, jank,
thermal, and lower-end-device confidence still require connected benchmark
runs on named hardware.

Commands for this review:

```powershell
.\gradlew.bat :macrobenchmark:assemble
.\gradlew.bat :app:assembleBenchmark
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :feature:dashboard:testDebugUnitTest
.\gradlew.bat :feature:search:testDebugUnitTest
.\gradlew.bat :feature:transaction:testDebugUnitTest
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat verifyAdaptiveScreenshots
```

All commands above passed in the local validation run. The first
`:macrobenchmark:assemble` attempt timed out and left a Gradle/Kotlin cache
lock; after `.\gradlew.bat --stop`, the command was rerun and passed.

Connected benchmark commands remain required when hardware is available:

```powershell
.\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest
```

Remaining gaps:

- No recomposition counter/report artifact is generated by the current project.
- Macrobenchmark results are local/manual only and are not trended in CI.
- Large dataset and scroll jank metrics still need a connected device/emulator
  run before any performance numbers can be claimed.
- Lower-end-device confidence is not established by compile-only validation.

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
.\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest
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
- T-E15-05 reviewed and tuned release/R8 optimization decisions.
- T-E15-06 will add a focused memory/perf pass.
- No benchmark results are recorded by this document; compare results only from
  repeat runs on the same named device, OS image, app commit, and build variant.
- Baseline Profile files are only committed after a successful generation run
  on a suitable device or emulator.
- Large dataset benchmark numbers are not recorded here; compare them only from
  repeat runs on the same named device, OS image, app commit, and build variant.

Do not add Macrobenchmark execution to default PR validation unless a future
manual or scheduled benchmark workflow is explicitly introduced.

## T-E15-05 Release Build And R8 Tuning

This pass reviewed the release, benchmark, debug, R8, baseline profile,
security, AI, Room, WorkManager, macrobenchmark, screenshot, and CI build
configuration.

Release build behavior after the review:

- `release` remains non-debuggable, minified with R8, resource-shrunk, and uses
  `proguard-android-optimize.txt` plus `app/proguard-rules.pro`.
- `benchmark` remains initialized from `release`, non-debuggable, profileable,
  debug-signed for local install, minified, resource-shrunk, and logging-off.
- `debug` remains developer-friendly and debuggable with debug logging enabled.
- The app still applies the AndroidX Baseline Profile plugin, consumes
  `:macrobenchmark` as the profile producer, and keeps
  `androidx.profileinstaller` as a release runtime dependency so embedded
  profiles can be installed from locally installed APKs.

R8 tuning:

- No broad app keep rules or broad warning suppressions were added.
- `app/proguard-rules.pro` now documents the project policy: rely on Android's
  optimized defaults and library consumer rules unless a specific runtime issue
  proves a narrower custom rule is needed.
- Tink's Error Prone and JSR-305 annotation references are resolved through
  `compileOnly` dependencies in `:app` and `:core:security`, which keeps the R8
  classpath complete without packaging annotation-only jars into the release
  runtime dependency metadata.
- Generated R8 configuration showed consumer rules from WorkManager, Room,
  Security Crypto, ProfileInstaller, Compose/Lifecycle, and Tink. No generated
  `missing_rules.txt` file was present after `:app:assembleRelease`.

Release dependency scope review:

- Release SDK dependency metadata included expected production runtime
  dependencies such as ProfileInstaller, Room, WorkManager, AndroidX Security
  Crypto, and Tink.
- Release SDK dependency metadata did not include `error_prone_annotations`,
  `jsr305`, Paparazzi, Macrobenchmark, UIAutomator, AndroidX test libraries,
  Espresso, or JUnit after the scope change.
- The large dataset generator remains benchmark-only through
  `benchmarkImplementation(project(":core:testing"))`.

Release artifact check from the validation build:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

The APK size was approximately 2.49 MiB (`2,606,447` bytes). Release mapping
outputs were generated under:

```text
app/build/outputs/mapping/release/
```

including `mapping.txt`, `configuration.txt`, `resources.txt`, `seeds.txt`,
and `usage.txt`.

CI validation:

- `.github/workflows/pr-validation.yml` already runs `:app:assembleRelease`
  and `clean build`, so PR validation should catch most release/R8 regressions.
- Connected macrobenchmark and baseline profile generation remain intentionally
  out of default CI because no device/emulator workflow is configured.

Commands for this review:

```powershell
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:assembleBenchmark
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :macrobenchmark:assemble
.\gradlew.bat :core:security:testDebugUnitTest
.\gradlew.bat :core:ai:testDebugUnitTest
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :core:database:testDebugUnitTest
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat clean build
```

Remaining gaps:

- Release signing was not validated because no local release signing properties
  were present in this environment.
- The minified release APK was not smoke-tested on a device or emulator.
- Future optional integrations must continue to be checked for real runtime
  reflection or service-loader requirements before adding any custom R8 rules.

## T-E15-06 Debug LeakCanary And Profiler Pass

This pass adds LeakCanary as a debug-only memory diagnostic tool and documents
the local profiler workflow. LeakCanary is declared in the version catalog as
`com.squareup.leakcanary:leakcanary-android` and is consumed only by `:app`
through `debugImplementation(libs.leakcanary.android)`.

Dependency scope:

- Debug builds include LeakCanary so local development APKs can report retained
  objects and heap-analysis findings.
- Release builds do not include LeakCanary because there is no
  `implementation`, `releaseImplementation`, or shared-module dependency on
  it.
- Benchmark builds remain release-like and are not given a
  `benchmarkImplementation` LeakCanary dependency. This avoids polluting startup,
  frame timing, memory pressure, and Baseline Profile collection.
- No LeakCanary R8 keep rules, `dontwarn` rules, release runtime hooks, or
  production monitoring code were added.

Manual profiler checklist:

1. Attach a development device or emulator. Use synthetic or demo data only;
   do not use personal financial records, real merchant names, real account
   data, or real bank details during profiling.
2. Install and launch the debug app:

```powershell
.\gradlew.bat :app:installDebug
```

3. Exercise startup to dashboard, dashboard refresh, transaction list scrolling,
   transaction detail/editor navigation, search query/results, Settings app-lock
   toggle states, lock/unlock flow on a device with supported authentication,
   and top-level navigation between Dashboard, Transactions, Search, Insights,
   Settings, and Debug.
4. Rotate or background/foreground the app while watching for retained
   `Activity`, `View`, `NavHost`, biometric prompt, repository, or Room objects.
5. Open the LeakCanary notification or launcher entry if a retained-object
   report appears. Treat "retained" as a lead, not proof: inspect the reference
   path, reproduce the flow, and confirm whether the object should have been
   cleared.
6. Use Android Studio Memory Profiler for heap snapshots around the same flows.
   Compare snapshots after forcing GC and after navigating away from the screen
   under test.
7. If a leak is confirmed, fix the ownership boundary rather than suppressing
   LeakCanary. Typical fixes include using `applicationContext` for app-singleton
   services, keeping coroutines in `viewModelScope` or composition-managed
   scopes, cancelling callbacks/listeners from `DisposableEffect`, avoiding
   stored `Activity` or `View` references, and closing short-lived Room
   databases.

Static memory-risk review:

- App graph and `MainActivity`: production construction uses
  `applicationContext` for Room, WorkManager, encrypted preferences, and
  `BiometricManager`. `MainActivity` owns a small activity coroutine scope and
  cancels it in `onDestroy`. The app graph receives an `activityProvider`
  callback for biometric prompts instead of storing a `FragmentActivity`
  instance directly.
- App lock and biometric prompt: `AppLockGate` uses lifecycle-aware state
  collection and composition-managed unlock launches. The Android biometric
  prompt is created inside the suspend authentication call and cancelled if the
  coroutine is cancelled.
- Compose routes: dashboard, budget setup, transaction list/detail/editor,
  adaptive transaction, search, and settings use `collectAsStateWithLifecycle`,
  `LaunchedEffect`, `remember`, `rememberCoroutineScope`, or ViewModel scopes in
  lifecycle-owned places. No undisposed listener or raw view callback was found.
- ViewModels: repository Flow collection and save/delete work run through
  `viewModelScope`, `stateIn(WhileSubscribed(...))`, or one-shot launches. The
  transaction delete undo snapshot is intentionally short-lived ViewModel state.
- Room/repositories: the app database is an app-lifetime Room singleton owned by
  the graph. The benchmark setup activity creates a temporary database for
  seeding and closes it in `finally`.
- WorkManager: the scheduler stores a `WorkManager` instance obtained from
  application context and does not store activities, views, or callbacks.
- AI provider/fallback code: current providers are local/stub implementations
  without Android context references or network/client resources.
- Benchmark data: large benchmark data is benchmark-source-set only through
  `benchmarkImplementation(project(":core:testing"))` and is chunked during
  seeding. It is not available to normal debug or release source sets except
  through the benchmark build type.

No obvious low-risk leak fix was identified during the static review, so this
task does not refactor runtime architecture or change production behavior.

Runtime limitation:

- A LeakCanary runtime pass requires an attached device or emulator and a debug
  install. If no device is attached during validation, only build/dependency
  isolation can be verified and no leak-free runtime claim should be made.

Validation results for this pass:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:assembleBenchmark
.\gradlew.bat :macrobenchmark:assemble
.\gradlew.bat :app:testDebugUnitTest :core:security:testDebugUnitTest :core:data:testDebugUnitTest :core:database:testDebugUnitTest :feature:dashboard:testDebugUnitTest :feature:search:testDebugUnitTest :feature:transaction:testDebugUnitTest
.\gradlew.bat :app:dependencyInsight --configuration debugRuntimeClasspath --dependency leakcanary
.\gradlew.bat :app:dependencyInsight --configuration releaseRuntimeClasspath --dependency leakcanary
.\gradlew.bat :app:dependencyInsight --configuration benchmarkRuntimeClasspath --dependency leakcanary
```

All commands above passed. The first `:app:assembleDebug` attempt timed out
before returning useful output; the same command was rerun with a longer timeout
and passed. Dependency insight showed LeakCanary 2.14 on
`debugRuntimeClasspath`, and no matching LeakCanary dependency on
`releaseRuntimeClasspath` or `benchmarkRuntimeClasspath`.

`adb devices` reported no attached device or emulator, so the debug app was not
installed, LeakCanary UI/notification behavior was not observed, and no runtime
leak result is claimed.

Follow-ups:

- Run the manual profiler checklist on a named physical device and a lower-end
  emulator before recording runtime findings.
- Keep future profiler findings tied to exact app commit, build variant,
  device, Android version, flows exercised, and LeakCanary trace evidence.
