package com.mojtaba.pocketledger.macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

internal fun MacrobenchmarkScope.seedDemoData() {
    val context = InstrumentationRegistry.getInstrumentation().context
    val intent = Intent()
        .setClassName(BenchmarkConfig.PackageName, BenchmarkConfig.SetupActivity)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(intent)
    device.wait(
        Until.hasObject(By.text("Benchmark data ready")),
        BenchmarkConfig.TimeoutMillis,
    )
}
