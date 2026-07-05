package com.mojtaba.pocketledger.feature.dashboard.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.pocketledger.core.ai.AiBudgetComparison
import com.mojtaba.pocketledger.core.ai.AiCategorySummary
import com.mojtaba.pocketledger.core.ai.AiFallbackStrategy
import com.mojtaba.pocketledger.core.ai.AiInferenceResult
import com.mojtaba.pocketledger.core.ai.AiRecurringHint
import com.mojtaba.pocketledger.core.ai.MonthlySummaryRequest
import com.mojtaba.pocketledger.core.ai.RuleBasedAiProvider
import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val aiFallbackStrategy: AiFallbackStrategy,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val currencyCode: String = DefaultCurrencyCode,
) : ViewModel() {
    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<InsightsUiState> = refreshRequests
        .flatMapLatest { observeInsights() }
        .onStart { emit(InsightsUiState.Loading) }
        .catch { throwable -> emit(InsightsUiState.Error(throwable.message ?: "Could not generate private insights.")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InsightsUiState.Loading,
        )

    fun onAction(action: InsightsAction) {
        when (action) {
            InsightsAction.RetryClicked -> refreshRequests.update { it + 1 }
        }
    }

    private fun observeInsights(): Flow<InsightsUiState> {
        val period = currentPeriod()
        return combine(
            transactionRepository.observeTransactionsByDateRange(period.startMillis, period.endMillis),
            categoryRepository.observeActiveCategories(),
            budgetRepository.observeBudgetsByPeriodRange(period.startMillis, period.endMillis),
        ) { transactions, categories, budgets ->
            AggregateInput(period, transactions, categories, budgets)
        }.transformLatest { input ->
            val request = input.toMonthlySummaryRequest(currencyCode.normalizedCurrency())
            if (request.transactionCount == 0 && input.budgets.none { it.isActive }) {
                emit(InsightsUiState.Empty(input.period.label))
            } else {
                val result = when (val generated = aiFallbackStrategy.generateMonthlySummary(request)) {
                    is AiInferenceResult.Success -> generated.value.copy(
                        providerType = generated.providerType,
                        fallbackReason = generated.fallbackReason,
                    )
                    is AiInferenceResult.Unavailable,
                    is AiInferenceResult.Failure,
                    -> (RuleBasedAiProvider.generateMonthlySummary(request) as AiInferenceResult.Success).value
                }
                val expense = abs(request.totalExpenseMinor)
                val net = request.totalIncomeMinor - expense
                emit(
                    InsightsUiState.Content(
                        periodLabel = input.period.label,
                        incomeText = request.totalIncomeMinor.toMoney(request.currencyCode),
                        expenseText = expense.toMoney(request.currencyCode),
                        netText = net.toMoney(request.currencyCode),
                        result = result,
                        isFallback = result.fallbackReason != null || result.providerType == RuleBasedAiProvider.type,
                    ),
                )
            }
        }
    }

    private fun AggregateInput.toMonthlySummaryRequest(currency: String): MonthlySummaryRequest {
        val normalized = transactions.filter { it.currencyCode.normalizedCurrency() == currency }
        val income = normalized.filter { it.type.equals("income", ignoreCase = true) }.sumOf { abs(it.amountMinor) }
        val expenseTransactions = normalized.filter { it.type.equals("expense", ignoreCase = true) }
        val expense = expenseTransactions.sumOf { abs(it.amountMinor) }
        val categoriesById = categories.associateBy { it.id }
        val categorySummaries = expenseTransactions
            .groupBy { it.categoryId }
            .map { (categoryId, items) ->
                AiCategorySummary(
                    categoryId = categoryId,
                    displayName = categoriesById[categoryId]?.name,
                    totalExpenseMinor = items.sumOf { abs(it.amountMinor) },
                    transactionCount = items.size,
                )
            }
        val recurringHints = expenseTransactions
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, items) ->
                if (items.size >= 3) {
                    AiRecurringHint(categoriesById[categoryId]?.name ?: "Uncategorized", items.size)
                } else {
                    null
                }
            }
            .sortedByDescending { it.transactionCount }
        val budgetComparisons = budgets.filter { it.isActive && it.currencyCode.normalizedCurrency() == currency }
            .map { budget ->
                val spent = expenseTransactions
                    .filter { budget.categoryId == null || it.categoryId == budget.categoryId }
                    .sumOf { abs(it.amountMinor) }
                AiBudgetComparison(budget.id, budget.name.ifBlank { "Budget" }, spent, budget.amountMinor)
            }
        return MonthlySummaryRequest(
            periodLabel = period.label,
            startMillis = period.startMillis,
            endMillis = period.endMillis,
            currencyCode = currency,
            totalIncomeMinor = income,
            totalExpenseMinor = expense,
            transactionCount = normalized.size,
            categorySummaries = categorySummaries,
            recurringHints = recurringHints,
            budgetComparisons = budgetComparisons,
        )
    }

    private fun currentPeriod(): InsightsPeriod {
        val now = Instant.ofEpochMilli(currentTimeMillis()).atZone(zoneId)
        val month = YearMonth.from(now)
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L
        return InsightsPeriod(start, end, month.format(PeriodFormatter))
    }

    private data class AggregateInput(
        val period: InsightsPeriod,
        val transactions: List<LedgerTransaction>,
        val categories: List<LedgerCategory>,
        val budgets: List<LedgerBudget>,
    )

    private data class InsightsPeriod(
        val startMillis: Long,
        val endMillis: Long,
        val label: String,
    )

    private companion object {
        const val DefaultCurrencyCode = "USD"
        val PeriodFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    }
}

private fun String.normalizedCurrency(): String = trim().uppercase(Locale.US)

private fun Long.toMoney(currencyCode: String): String =
    String.format(Locale.US, "%s %.2f", currencyCode, this / 100.0)
