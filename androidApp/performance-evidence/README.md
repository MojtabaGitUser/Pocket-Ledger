# Performance evidence

This directory is the local output root for the recomposition and jank review.
Generated runs are intentionally ignored because Perfetto traces can be large
and benchmark results are only comparable on the same named device and OS
image.

Generate a run from the repository root:

```powershell
.\scripts\collect_android_performance_evidence.ps1 -DeviceSerial emulator-5554
```

Each timestamped run contains:

- `manifest.json`: commit, dirty state, device identity, Android version, and
  executed Gradle tasks.
- `summary.md`: a reviewer-oriented index of the captured evidence.
- `compose-compiler/`: release-source compiler stability, skippability, and module
  metrics reports.
- `macrobenchmark/`: benchmark JSON, instrumentation results, and Compose-aware
  Perfetto traces with `FrameTimingMetric` output.

Open `.perfetto-trace` files in [Perfetto](https://ui.perfetto.dev/) and inspect
Compose composition/recomposition slices alongside frame timelines. Do not
compare numbers from different device models, Android versions, thermal states,
or build commits.
