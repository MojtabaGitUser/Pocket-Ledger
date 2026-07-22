package com.mojtaba.folentra.feature.dashboard.insights

import com.mojtaba.folentra.core.ai.MonthlySummaryResult

sealed interface InsightsUiState {
    data object Loading : InsightsUiState
    data class Empty(val periodLabel: String) : InsightsUiState
    data class Error(val message: String) : InsightsUiState
    data class Content(
        val periodLabel: String,
        val incomeText: String,
        val expenseText: String,
        val netText: String,
        val result: MonthlySummaryResult,
        val isFallback: Boolean,
    ) : InsightsUiState
}

sealed interface InsightsAction {
    data object RetryClicked : InsightsAction
}
