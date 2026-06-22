package com.mojtaba.pocketledger.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        baselineProfileRule.collect(
            packageName = BenchmarkConfig.PackageName,
        ) {
            seedDemoData()
            pressHome()

            startActivityAndWait()
            waitForText("Financial overview")

            findByDescription("Transactions navigation destination").click()
            waitForText("Neighborhood Market")
            val transactionList = findByResource("TransactionList")
            repeat(2) {
                transactionList.fling(Direction.DOWN)
                device.waitForIdle()
            }
            repeat(2) {
                transactionList.fling(Direction.UP)
                device.waitForIdle()
            }

            findByDescription("Search navigation destination").click()
            findByDescription("Search transactions by keyword").text = "Bluebird"
            waitForText("Bluebird Coffee")
            val searchResults = findByResource("SearchResultsList")
            searchResults.fling(Direction.DOWN)
            device.waitForIdle()

            findByDescription("Settings navigation destination").click()
            waitForText("Security")
        }
    }
}
