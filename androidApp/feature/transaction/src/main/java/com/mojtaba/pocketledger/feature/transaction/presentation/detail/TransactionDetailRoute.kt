package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    onDeleteRequested: (String) -> Unit,
    viewModel: TransactionDetailViewModel = viewModel(
        factory = TransactionDetailViewModelFactory(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            transactionId = transactionId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TransactionDetailEvent.EditTransaction -> onEditTransaction(event.transactionId)
                is TransactionDetailEvent.DeleteRequested -> onDeleteRequested(event.transactionId)
            }
        }
    }

    TransactionDetailScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
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
