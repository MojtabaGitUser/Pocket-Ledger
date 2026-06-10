package com.mojtaba.pocketledger.benchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mojtaba.pocketledger.PocketLedgerAppGraph
import com.mojtaba.pocketledger.core.data.seed.DemoDataSeeder
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme

class BenchmarkSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appGraph = PocketLedgerAppGraph.create(applicationContext)
        setContent {
            var ready by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                DemoDataSeeder(
                    categoryRepository = appGraph.categoryRepository,
                    transactionRepository = appGraph.transactionRepository,
                    budgetRepository = appGraph.budgetRepository,
                    tagRepository = appGraph.tagRepository,
                ).seedDemoData()
                ready = true
            }

            PocketLedgerTheme(dynamicColor = false) {
                Text(
                    text = if (ready) {
                        "Benchmark data ready"
                    } else {
                        "Preparing benchmark data"
                    },
                )
            }
        }
    }
}
