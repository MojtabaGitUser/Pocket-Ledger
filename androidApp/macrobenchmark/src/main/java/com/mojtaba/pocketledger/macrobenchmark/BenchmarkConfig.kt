package com.mojtaba.pocketledger.macrobenchmark

internal object BenchmarkConfig {
    const val PackageName = "com.mojtaba.pocketledger"
    const val SetupActivity = "com.mojtaba.pocketledger.benchmark.BenchmarkSetupActivity"
    const val SeedModeExtra = "com.mojtaba.pocketledger.benchmark.SEED_MODE"
    const val SeedModeDemo = "demo"
    const val SeedModeLarge = "large"
    const val TimeoutMillis = 30_000L
    const val LargeSeedTimeoutMillis = 180_000L
}
