package com.mojtaba.pocketledger.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

class SearchBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun searchSeededTransactions() {
        benchmarkRule.measureRepeated(
            packageName = BenchmarkConfig.PackageName,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.COLD,
            iterations = 5,
            setupBlock = {
                seedDemoData()
                pressHome()
            },
        ) {
            startActivityAndWait()
            device.findObject(By.desc("Search navigation destination")).click()
            device.wait(
                Until.hasObject(By.desc("Search transactions by keyword")),
                BenchmarkConfig.TimeoutMillis,
            )

            device.findObject(By.desc("Search transactions by keyword")).text = "Bluebird"
            device.wait(
                Until.hasObject(By.text("Bluebird Coffee")),
                BenchmarkConfig.TimeoutMillis,
            )
        }
    }
}
