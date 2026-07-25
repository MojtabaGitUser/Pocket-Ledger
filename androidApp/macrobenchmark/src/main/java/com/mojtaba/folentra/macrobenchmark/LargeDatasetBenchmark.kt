package com.mojtaba.folentra.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test

class LargeDatasetBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollAndSearchLargeDataset() {
        benchmarkRule.measureRepeated(
            packageName = BenchmarkConfig.PackageName,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD,
            iterations = 3,
            setupBlock = {
                seedLargeBenchmarkData()
                pressHome()
            },
        ) {
            startActivityAndWait()
            waitForDescription("Dashboard navigation destination")

            findByDescription("Transactions navigation destination").click()
            device.waitForIdle()
            repeat(5) {
                flingVertically(Direction.DOWN)
            }
            repeat(5) {
                flingVertically(Direction.UP)
            }

            findByDescription("Search navigation destination").click()
            enterText("Search transactions by keyword", "LedgerMart")
        }
    }
}
