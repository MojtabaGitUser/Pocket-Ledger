package com.mojtaba.pocketledger.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mojtaba.pocketledger.core.ai.AiFallbackStrategy
import com.mojtaba.pocketledger.core.ai.AiProviderSelector
import com.mojtaba.pocketledger.core.ai.GeminiNanoAiProvider
import com.mojtaba.pocketledger.core.ai.MlKitAiProvider
import com.mojtaba.pocketledger.core.ai.NoOpAiProvider
import com.mojtaba.pocketledger.core.ai.RuleBasedAiProvider
import com.mojtaba.pocketledger.core.background.tasks.MonthlySummaryPreparationInput
import com.mojtaba.pocketledger.core.database.createPocketLedgerDatabase
import com.mojtaba.pocketledger.core.data.repository.local.LocalBudgetRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalCategoryRepository
import com.mojtaba.pocketledger.core.data.repository.local.LocalTransactionRepository
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.pocketledger.core.featureflags.LocalFeatureFlagProvider
import kotlinx.coroutines.CancellationException
import java.io.IOException

class MonthlySummaryPreparationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val input = WorkManagerTaskMapper.monthlySummaryInput(inputData)
            ?: return Result.failure()
        val service = serviceFactory(applicationContext)
        return try {
            when (service.prepare(input)) {
                MonthlySummaryPreparationResult.Prepared -> Result.success()
                MonthlySummaryPreparationResult.RetryableFailure -> Result.retry()
                MonthlySummaryPreparationResult.PermanentFailure -> Result.failure()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IllegalArgumentException) {
            Result.failure()
        } catch (exception: IllegalStateException) {
            Result.failure()
        } catch (exception: IOException) {
            Result.retry()
        }
    }

    companion object {
        internal var serviceFactory: (Context) -> MonthlySummaryPreparationService = { context ->
            val database = createPocketLedgerDatabase(context)
            val featureFlags = FeatureFlagEvaluator(LocalFeatureFlagProvider())
            val selector = AiProviderSelector(
                providers = listOf(
                    GeminiNanoAiProvider(),
                    MlKitAiProvider(),
                    RuleBasedAiProvider,
                    NoOpAiProvider,
                ),
                featureFlags = featureFlags,
            )
            MonthlySummaryPreparationService(
                transactionRepository = LocalTransactionRepository(database.transactionDao()),
                categoryRepository = LocalCategoryRepository(database.categoryDao()),
                budgetRepository = LocalBudgetRepository(database.budgetDao()),
                aiFallbackStrategy = AiFallbackStrategy(selector),
            )
        }
    }
}
