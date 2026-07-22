package com.mojtaba.folentra.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.feature.dashboard.domain.DashboardSummaryGenerator
import com.mojtaba.folentra.feature.dashboard.domain.DashboardSummaryInput
import com.mojtaba.folentra.feature.dashboard.model.DashboardPeriod
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
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
class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val summaryGenerator: DashboardSummaryGenerator,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val currencyCode: String = DefaultCurrencyCode,
) : ViewModel() {
    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<DashboardUiState> = refreshRequests
        .flatMapLatest { observeSummaryState() }
        .onStart { emit(DashboardUiState.Loading) }
        .catch { throwable ->
            emit(DashboardUiState.Error(throwable.message ?: "Could not read local dashboard summaries."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading,
        )

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.RetryClicked -> refreshRequests.update { it + 1 }
            DashboardAction.SetBudgetClicked -> Unit
        }
    }

    private fun observeSummaryState(): Flow<DashboardUiState> {
        val period = currentPeriod()
        return combine(
            transactionRepository.observeTransactionsByDateRange(period.startMillis, period.endMillis),
            categoryRepository.observeActiveCategories(),
            budgetRepository.observeBudgetsByPeriodRange(period.startMillis, period.endMillis),
        ) { transactions, categories, budgets ->
            DashboardSummaryInput(
                period = period,
                currencyCode = currencyCode,
                transactions = transactions,
                categories = categories,
                budgets = budgets,
                generatedAt = currentTimeMillis(),
            )
        }.transformLatest { input ->
            if (input.transactions.isEmpty() && input.budgets.none { it.isActive }) {
                emit(DashboardUiState.Empty)
            } else {
                emit(DashboardUiState.Content(summaryGenerator.generate(input)))
            }
        }
    }

    private fun currentPeriod(): DashboardPeriod {
        val now = Instant.ofEpochMilli(currentTimeMillis()).atZone(zoneId)
        val month = YearMonth.from(now)
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L
        return DashboardPeriod(
            startMillis = start,
            endMillis = end,
            label = month.format(PeriodFormatter),
        )
    }

    private companion object {
        const val DefaultCurrencyCode = "USD"
        val PeriodFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    }
}
