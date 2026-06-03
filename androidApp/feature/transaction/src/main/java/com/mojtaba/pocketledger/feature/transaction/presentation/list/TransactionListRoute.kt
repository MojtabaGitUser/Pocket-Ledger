package com.mojtaba.pocketledger.feature.transaction.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository

@Composable
fun TransactionListRoute(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    onOpenTransaction: (String) -> Unit,
    viewModel: TransactionListViewModel = viewModel(
        factory = TransactionListViewModelFactory(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TransactionListEvent.OpenDetail -> onOpenTransaction(event.transactionId)
            }
        }
    }

    TransactionListScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
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
