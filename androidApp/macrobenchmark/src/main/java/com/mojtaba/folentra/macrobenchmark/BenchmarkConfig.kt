package com.mojtaba.folentra.macrobenchmark

internal object BenchmarkConfig {
    const val PackageName = "com.mojtaba.folentra"
    const val SetupActivity = "com.mojtaba.folentra.benchmark.BenchmarkSetupActivity"
    const val SeedModeExtra = "com.mojtaba.folentra.benchmark.SEED_MODE"
    const val SeedModeDemo = "demo"
    const val SeedModeLarge = "large"
    const val TimeoutMillis = 30_000L
    const val LargeSeedTimeoutMillis = 180_000L
}
