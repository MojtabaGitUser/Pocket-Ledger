package com.mojtaba.pocketledger.macrobenchmark

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
            waitForText("Financial overview")

            findByDescription("Transactions navigation destination").click()
            waitForText("LedgerMart Market 0000")
            val transactionList = findByResource("TransactionList")
            repeat(5) {
                transactionList.fling(Direction.DOWN)
                device.waitForIdle()
            }
            repeat(5) {
                transactionList.fling(Direction.UP)
                device.waitForIdle()
            }

            findByDescription("Search navigation destination").click()
            findByDescription("Search transactions by keyword").text = "LedgerMart"
            waitForText("LedgerMart Market 0000")
            val searchResults = findByResource("SearchResultsList")
            repeat(3) {
                searchResults.fling(Direction.DOWN)
                device.waitForIdle()
            }
        }
    }
}
