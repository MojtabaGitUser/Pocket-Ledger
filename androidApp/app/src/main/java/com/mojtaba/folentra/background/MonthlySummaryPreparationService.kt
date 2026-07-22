package com.mojtaba.folentra.background

import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.background.tasks.MonthlySummaryPreparationInput
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.feature.dashboard.domain.DashboardSummaryGenerator
import com.mojtaba.folentra.feature.dashboard.domain.DashboardSummaryInput
import com.mojtaba.folentra.feature.dashboard.model.DashboardPeriod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.io.IOException

class MonthlySummaryPreparationService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    aiFallbackStrategy: AiFallbackStrategy,
) {
    private val generator = DashboardSummaryGenerator(aiFallbackStrategy)

    suspend fun prepare(input: MonthlySummaryPreparationInput): MonthlySummaryPreparationResult {
        val normalized = input.normalized()
        return try {
            generator.generate(
                DashboardSummaryInput(
                    period = DashboardPeriod(
                        startMillis = normalized.periodStartMillis,
                        endMillis = normalized.periodEndMillis,
                        label = normalized.periodLabel,
                    ),
                    currencyCode = normalized.currencyCode,
                    transactions = transactionRepository.observeTransactionsByDateRange(
                        normalized.periodStartMillis,
                        normalized.periodEndMillis,
                    ).first(),
                    categories = categoryRepository.observeActiveCategories().first(),
                    budgets = budgetRepository.observeBudgetsByPeriodRange(
                        normalized.periodStartMillis,
                        normalized.periodEndMillis,
                    ).first(),
                    generatedAt = normalized.generatedAtMillis,
                ),
            )
            MonthlySummaryPreparationResult.Prepared
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IllegalArgumentException) {
            MonthlySummaryPreparationResult.PermanentFailure
        } catch (exception: IllegalStateException) {
            MonthlySummaryPreparationResult.PermanentFailure
        } catch (exception: IOException) {
            MonthlySummaryPreparationResult.RetryableFailure
        }
    }
}

enum class MonthlySummaryPreparationResult {
    Prepared,
    RetryableFailure,
    PermanentFailure,
}
