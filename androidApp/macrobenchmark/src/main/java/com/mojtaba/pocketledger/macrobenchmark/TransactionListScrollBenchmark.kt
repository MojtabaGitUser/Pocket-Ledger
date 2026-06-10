package com.mojtaba.pocketledger.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
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
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.COLD,
            iterations = 5,
            setupBlock = {
                seedDemoData()
                pressHome()
            },
        ) {
            startActivityAndWait()
            device.findObject(By.desc("Transactions navigation destination")).click()
            device.wait(
                Until.hasObject(By.text("Neighborhood Market")),
                BenchmarkConfig.TimeoutMillis,
            )

            val transactionList = device.findObject(
                By.res(BenchmarkConfig.PackageName, "TransactionList"),
            )
            repeat(3) {
                transactionList.fling(Direction.DOWN)
                device.waitForIdle()
            }
            repeat(3) {
                transactionList.fling(Direction.UP)
                device.waitForIdle()
            }
        }
    }
}
