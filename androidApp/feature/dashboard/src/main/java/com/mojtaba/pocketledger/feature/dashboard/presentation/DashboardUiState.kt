package com.mojtaba.pocketledger.feature.dashboard.presentation

import androidx.compose.runtime.Immutable
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardSummary

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState

    @Immutable
    data class Content(
        val summary: DashboardSummary,
    ) : DashboardUiState

    @Immutable
    data class Error(
        val message: String,
    ) : DashboardUiState
}
