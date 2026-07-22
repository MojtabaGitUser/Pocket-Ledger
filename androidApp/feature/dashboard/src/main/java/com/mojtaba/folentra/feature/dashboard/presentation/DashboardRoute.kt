package com.mojtaba.folentra.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.core.designsystem.adaptive.FolentraWindowWidthSizeClass
import com.mojtaba.folentra.feature.dashboard.domain.DashboardSummaryGenerator

@Composable
fun DashboardStateRoute(
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    aiFallbackStrategy: AiFallbackStrategy,
    widthSizeClass: FolentraWindowWidthSizeClass = FolentraWindowWidthSizeClass.Compact,
    onSetBudget: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            aiFallbackStrategy = aiFallbackStrategy,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardRoute(
        modifier = modifier,
        uiState = uiState,
        widthSizeClass = widthSizeClass,
        onAction = { action ->
            viewModel.onAction(action)
            if (action == DashboardAction.SetBudgetClicked) {
                onSetBudget()
            }
        },
    )
}

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    uiState: DashboardUiState = DashboardUiState.Empty,
    widthSizeClass: FolentraWindowWidthSizeClass = FolentraWindowWidthSizeClass.Compact,
    onAction: (DashboardAction) -> Unit = {},
) {
    DashboardScreen(
        uiState = uiState,
        widthSizeClass = widthSizeClass,
        onAction = onAction,
        modifier = modifier,
    )
}

private class DashboardViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val aiFallbackStrategy: AiFallbackStrategy,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return DashboardViewModel(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            summaryGenerator = DashboardSummaryGenerator(aiFallbackStrategy),
        ) as T
    }
}
