package com.mojtaba.folentra.feature.dashboard.budget

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.CategoryRepository

@Composable
fun BudgetSetupRoute(
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    mode: BudgetSetupMode,
    budgetId: String?,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: BudgetSetupViewModel = viewModel(
        factory = BudgetSetupViewModelFactory(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            mode = mode,
            budgetId = budgetId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                BudgetSetupEvent.SaveCompleted -> onSaved()
                is BudgetSetupEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    BudgetSetupScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

private class BudgetSetupViewModelFactory(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val mode: BudgetSetupMode,
    private val budgetId: String?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        if (!modelClass.isAssignableFrom(BudgetSetupViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return BudgetSetupViewModel(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            savedStateHandle = extras.createSavedStateHandle(),
            initialMode = mode,
            initialBudgetId = budgetId,
        ) as T
    }
}
