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
import com.mojtaba.pocketledger.core.data.seed.DemoDataSeeder
import com.mojtaba.pocketledger.core.data.repository.local.LocalBudgetRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalCategoryRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTagRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTransactionRepository
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import com.mojtaba.pocketledger.core.database.createPocketLedgerDatabase
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme

class BenchmarkSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val seedMode = intent.getStringExtra(EXTRA_SEED_MODE) ?: SEED_MODE_DEMO
        applicationContext.deleteDatabase(PocketLedgerDatabase.DATABASE_NAME)
        setContent {
            var ready by remember { mutableStateOf(false) }

            LaunchedEffect(seedMode) {
                seedBenchmarkData(seedMode)
                ready = true
            }

            PocketLedgerTheme(dynamicColor = false) {
                Text(
                    text = if (ready) {
                        seedMode.readyText()
                    } else {
                        seedMode.preparingText()
                    },
                )
            }
        }
    }

    private suspend fun seedBenchmarkData(seedMode: String) {
        val database = createPocketLedgerDatabase(applicationContext)
        try {
            database.clearAllTables()
            val categoryRepository = LocalCategoryRepository(database.categoryDao())
            val transactionRepository = LocalTransactionRepository(database.transactionDao())
            val budgetRepository = LocalBudgetRepository(database.budgetDao())
            val tagRepository = LocalTagRepository(database.tagDao())
            when (seedMode) {
                SEED_MODE_LARGE -> LargeBenchmarkDataSeeder(
                    categoryRepository = categoryRepository,
                    transactionRepository = transactionRepository,
                    budgetRepository = budgetRepository,
                    tagRepository = tagRepository,
                ).seedLargeDataset()
                else -> DemoDataSeeder(
                    categoryRepository = categoryRepository,
                    transactionRepository = transactionRepository,
                    budgetRepository = budgetRepository,
                    tagRepository = tagRepository,
                ).seedDemoData()
            }
        } finally {
            database.close()
        }
    }

    private fun String.readyText(): String =
        when (this) {
            SEED_MODE_LARGE -> LARGE_READY_TEXT
            else -> DEMO_READY_TEXT
        }

    private fun String.preparingText(): String =
        when (this) {
            SEED_MODE_LARGE -> "Preparing large benchmark data"
            else -> "Preparing benchmark data"
        }

    companion object {
        const val EXTRA_SEED_MODE = "com.mojtaba.pocketledger.benchmark.SEED_MODE"
        const val SEED_MODE_DEMO = "demo"
        const val SEED_MODE_LARGE = "large"
        const val DEMO_READY_TEXT = "Benchmark data ready"
        const val LARGE_READY_TEXT = "Large benchmark data ready"
    }
}
