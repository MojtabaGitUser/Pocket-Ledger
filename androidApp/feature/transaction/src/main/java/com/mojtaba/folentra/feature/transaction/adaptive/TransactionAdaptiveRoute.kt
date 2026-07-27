package com.mojtaba.folentra.feature.transaction.adaptive

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListAction
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListUiState
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListViewModel

@Composable
fun TransactionAdaptiveRoute(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    initialSelectedTransactionId: String?,
    onAddTransaction: () -> Unit,
    onEditTransaction: (String) -> Unit,
    selectionViewModel: TransactionSelectionViewModel = viewModel(
        key = "transaction-selection-${initialSelectedTransactionId.orEmpty()}",
        factory = TransactionSelectionViewModelFactory(initialSelectedTransactionId),
    ),
    listViewModel: TransactionListViewModel = viewModel(
        key = "transaction-adaptive-list",
        factory = TransactionListViewModelFactory(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
        ),
    ),
) {
    val listUiState by listViewModel.uiState.collectAsStateWithLifecycle()
    val selectedTransactionId by selectionViewModel.selectedTransactionId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(listUiState, selectedTransactionId) {
        val visibleTransactionIds = listUiState.visibleTransactionIdsOrNull()
        if (visibleTransactionIds != null) {
            selectionViewModel.clearSelectionIfDeleted(visibleTransactionIds)
        }
    }

    TransactionListDetailScreen(
        listUiState = listUiState,
        selectedTransactionId = selectedTransactionId,
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        tagRepository = tagRepository,
        snackbarHostState = snackbarHostState,
        onListAction = { action ->
            when (action) {
                is TransactionListAction.TransactionClicked -> {
                    selectionViewModel.selectTransaction(action.transactionId)
                }
                TransactionListAction.RetryClicked -> {
                    listViewModel.onAction(action)
                }
            }
        },
        onClearSelection = selectionViewModel::clearSelection,
        onAddTransaction = onAddTransaction,
        onEditTransaction = onEditTransaction,
    )
}

private fun TransactionListUiState.visibleTransactionIdsOrNull(): Set<String>? =
    when (this) {
        is TransactionListUiState.Content ->
            transactions.map { it.id }.toSet()
        TransactionListUiState.Empty,
        is TransactionListUiState.Error,
        TransactionListUiState.Loading,
        -> null
    }

private class TransactionSelectionViewModelFactory(
    private val initialSelectedTransactionId: String?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        if (!modelClass.isAssignableFrom(TransactionSelectionViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return TransactionSelectionViewModel(
            savedStateHandle = extras.createSavedStateHandle(),
            initialSelectedTransactionId = initialSelectedTransactionId,
        ) as T
    }
}

private class TransactionListViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(TransactionListViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return TransactionListViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
        ) as T
    }
}
