package com.mojtaba.pocketledger.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
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
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD,
            iterations = 5,
            setupBlock = {
                seedDemoData()
                pressHome()
            },
        ) {
            startActivityAndWait()
            findByDescription("Search navigation destination").click()

            enterText("Search transactions by keyword", "Bluebird")
        }
    }
}
