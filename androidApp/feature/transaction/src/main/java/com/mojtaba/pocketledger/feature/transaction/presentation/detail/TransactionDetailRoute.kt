package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository

@Composable
fun TransactionDetailRoute(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    transactionId: String?,
    onNavigateBack: () -> Unit,
    onEditTransaction: (String) -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    showTopBar: Boolean = true,
    showBackAction: Boolean = true,
    viewModel: TransactionDetailViewModel = viewModel(
        factory = TransactionDetailViewModelFactory(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            transactionId = transactionId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                is TransactionDetailEvent.EditTransaction -> onEditTransaction(event.transactionId)
                TransactionDetailEvent.NavigateBackAfterDelete -> onNavigateBack()
                TransactionDetailEvent.ShowDeleteUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onAction(TransactionDetailAction.UndoDeleteClicked)
                    } else {
                        onNavigateBack()
                    }
                }
                is TransactionDetailEvent.ShowDeleteFailedSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    TransactionDetailScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        showTopBar = showTopBar,
        showBackAction = showBackAction,
        modifier = modifier,
    )
}

private class TransactionDetailViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val transactionId: String?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        if (!modelClass.isAssignableFrom(TransactionDetailViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return TransactionDetailViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            savedStateHandle = extras.createSavedStateHandle(),
            initialTransactionId = transactionId,
        ) as T
    }
}
