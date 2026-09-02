package com.mojtaba.folentra.benchmark

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mojtaba.folentra.core.data.seed.DemoDataSeeder
import com.mojtaba.folentra.core.data.repository.local.LocalBudgetRepository
import com.mojtaba.folentra.core.data.repository.local.LocalCategoryRepository
import com.mojtaba.folentra.core.data.repository.local.LocalTagRepository
import com.mojtaba.folentra.core.data.repository.local.LocalTransactionRepository
import com.mojtaba.folentra.core.database.createFolentraDatabase
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BenchmarkSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val seedMode = intent.getStringExtra(EXTRA_SEED_MODE) ?: SEED_MODE_DEMO
        setContent {
            var failureMessage by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(seedMode) {
                try {
                    seedBenchmarkData(seedMode)
                    Log.i(LOG_TAG, "Benchmark data setup complete")
                    finish()
                } catch (error: Exception) {
                    Log.e(LOG_TAG, "Failed to seed benchmark data", error)
                    failureMessage = "Benchmark setup failed: ${error.javaClass.simpleName}"
                }
            }

            FolentraTheme(dynamicColor = false) {
                Text(
                    text = failureMessage ?: seedMode.preparingText(),
                    modifier = Modifier.semantics {
                        contentDescription = SETUP_CONTENT_DESCRIPTION
                    },
                )
            }
        }
    }

    private suspend fun seedBenchmarkData(seedMode: String) = withContext(Dispatchers.IO) {
        Log.i(LOG_TAG, "Opening benchmark database for seed mode: $seedMode")
        val database = createFolentraDatabase(applicationContext)
        try {
            Log.i(LOG_TAG, "Clearing benchmark database")
            database.clearAllTables()
            Log.i(LOG_TAG, "Benchmark database cleared")
            val categoryRepository = LocalCategoryRepository(database.categoryDao())
            val transactionRepository = LocalTransactionRepository(database.transactionDao())
            val budgetRepository = LocalBudgetRepository(database.budgetDao())
            val tagRepository = LocalTagRepository(database.tagDao())
            Log.i(LOG_TAG, "Seeding benchmark data")
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
            Log.i(LOG_TAG, "Benchmark data seeded")
        } finally {
            database.close()
            Log.i(LOG_TAG, "Benchmark database closed")
        }
    }

    private fun String.preparingText(): String =
        when (this) {
            SEED_MODE_LARGE -> "Preparing large benchmark data"
            else -> "Preparing benchmark data"
        }

    companion object {
        private const val LOG_TAG = "BenchmarkSetup"
        private const val SETUP_CONTENT_DESCRIPTION = "Benchmark data setup"
        const val EXTRA_SEED_MODE = "com.mojtaba.folentra.benchmark.SEED_MODE"
        const val SEED_MODE_DEMO = "demo"
        const val SEED_MODE_LARGE = "large"
    }
}
