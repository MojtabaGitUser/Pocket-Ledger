package com.mojtaba.folentra.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
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
            pressHome()
            startActivityAndWait()
            waitForDescription("Dashboard navigation destination")

            findByDescription("Transactions navigation destination").click()
            waitForDescription("Transactions navigation destination")

            findByDescription("Search navigation destination").click()
            waitForDescription("Search transactions by keyword")

            findByDescription("Settings navigation destination").click()
            waitForText("Security")
        }
    }
}
