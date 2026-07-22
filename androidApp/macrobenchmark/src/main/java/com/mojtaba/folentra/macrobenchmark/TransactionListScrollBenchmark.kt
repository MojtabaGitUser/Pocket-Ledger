package com.mojtaba.folentra.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test

class TransactionListScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollSeededTransactionList() {
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
            findByDescription("Transactions navigation destination").click()
            waitForText("Neighborhood Market")

            repeat(3) {
                flingVertically(Direction.DOWN)
            }
            repeat(3) {
                flingVertically(Direction.UP)
            }
        }
    }
}
