package com.mojtaba.pocketledger.macrobenchmark

import android.content.Intent
import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal fun MacrobenchmarkScope.seedDemoData() {
    seedBenchmarkData(
        seedMode = BenchmarkConfig.SeedModeDemo,
        timeoutMillis = BenchmarkConfig.TimeoutMillis,
    )
}

internal fun MacrobenchmarkScope.seedLargeBenchmarkData() {
    seedBenchmarkData(
        seedMode = BenchmarkConfig.SeedModeLarge,
        timeoutMillis = BenchmarkConfig.LargeSeedTimeoutMillis,
    )
}

private fun MacrobenchmarkScope.seedBenchmarkData(
    seedMode: String,
    timeoutMillis: Long,
) {
    val context = InstrumentationRegistry.getInstrumentation().context
    val intent = Intent()
        .setClassName(BenchmarkConfig.PackageName, BenchmarkConfig.SetupActivity)
        .putExtra(BenchmarkConfig.SeedModeExtra, seedMode)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(intent)
    device.waitForIdle()
    check(device.wait(Until.gone(By.desc(SETUP_CONTENT_DESCRIPTION)), timeoutMillis)) {
        "Benchmark setup did not complete within $timeoutMillis ms"
    }
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.waitForText(
    text: String,
    timeoutMillis: Long = BenchmarkConfig.TimeoutMillis,
) {
    check(device.wait(Until.hasObject(By.text(text)), timeoutMillis)) {
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

internal fun MacrobenchmarkScope.enterText(description: String, text: String) {
    require(text.matches(Regex("[A-Za-z0-9._-]+"))) {
        "Benchmark input must contain only simple text characters"
    }
    val input = findByDescription(description)
    input.click()
    SystemClock.sleep(INPUT_SETTLE_MILLIS)
    input.text = text
    SystemClock.sleep(INPUT_SETTLE_MILLIS)
    device.pressBack()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.flingVertically(direction: Direction) {
    val centerX = device.displayWidth / 2
    val upperY = device.displayHeight / 4
    val lowerY = device.displayHeight * 3 / 4
    val (startY, endY) = when (direction) {
        Direction.DOWN -> upperY to lowerY
        Direction.UP -> lowerY to upperY
        else -> error("Only vertical flings are supported")
    }
    device.swipe(centerX, startY, centerX, endY, FLING_STEPS)
    device.waitForIdle()
}

private const val SETUP_CONTENT_DESCRIPTION = "Benchmark data setup"
private const val INPUT_SETTLE_MILLIS = 500L
private const val FLING_STEPS = 10
