package com.mojtaba.pocketledger.macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal fun MacrobenchmarkScope.seedDemoData() {
    val context = InstrumentationRegistry.getInstrumentation().context
    val intent = Intent()
        .setClassName(BenchmarkConfig.PackageName, BenchmarkConfig.SetupActivity)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(intent)
    waitForText("Benchmark data ready")
    device.pressBack()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.waitForText(text: String) {
    check(device.wait(Until.hasObject(By.text(text)), BenchmarkConfig.TimeoutMillis)) {
        "Timed out waiting for text: $text"
    }
}

internal fun MacrobenchmarkScope.waitForDescription(description: String) {
    check(device.wait(Until.hasObject(By.desc(description)), BenchmarkConfig.TimeoutMillis)) {
        "Timed out waiting for content description: $description"
    }
}

internal fun MacrobenchmarkScope.findByDescription(description: String): UiObject2 {
    waitForDescription(description)
    return device.findObject(By.desc(description))
}

internal fun MacrobenchmarkScope.findByResource(resourceName: String): UiObject2 {
    val selector = By.res(BenchmarkConfig.PackageName, resourceName)
    check(device.wait(Until.hasObject(selector), BenchmarkConfig.TimeoutMillis)) {
        "Timed out waiting for resource: $resourceName"
    }
    return device.findObject(selector)
}
