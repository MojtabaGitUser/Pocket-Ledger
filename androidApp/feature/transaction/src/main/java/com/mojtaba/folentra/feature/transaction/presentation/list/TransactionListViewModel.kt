package com.mojtaba.folentra.feature.transaction.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.folentra.core.data.model.LedgerTag
import com.mojtaba.folentra.core.data.model.LedgerTransaction
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.feature.transaction.model.TransactionUiFormatters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {
    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<TransactionListUiState> = refreshRequests
        .flatMapLatest { observeUiState() }
        .onStart { emit(TransactionListUiState.Loading) }
        .catch { throwable ->
            emit(TransactionListUiState.Error(throwable.message ?: "Unable to load transactions."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionListUiState.Loading,
        )

    private val _events = MutableSharedFlow<TransactionListEvent>()
    val events: SharedFlow<TransactionListEvent> = _events.asSharedFlow()

    fun onAction(action: TransactionListAction) {
        when (action) {
            is TransactionListAction.TransactionClicked -> {
                viewModelScope.launch {
                    _events.emit(TransactionListEvent.OpenDetail(action.transactionId))
                }
            }
            TransactionListAction.RetryClicked -> refreshRequests.update { it + 1 }
        }
    }

    private fun observeUiState(): Flow<TransactionListUiState> {
        val transactionsWithTags = transactionRepository.observeRecentTransactions(RECENT_TRANSACTION_LIMIT)
            .flatMapLatest { transactions ->
                if (transactions.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        transactions.map { transaction ->
                            tagRepository.observeTagsForTransaction(transaction.id).map { tags ->
                                transaction to tags
                            }
                        },
                    ) { it.toList() }
                }
            }

        return combine(
            transactionsWithTags,
            categoryRepository.observeActiveCategories(),
        ) { transactionTagPairs, categories ->
            if (transactionTagPairs.isEmpty()) {
                TransactionListUiState.Empty
            } else {
                val categoriesById = categories.associateBy { it.id }
                TransactionListUiState.Content(
                    transactions = transactionTagPairs.map { (transaction, tags) ->
                        transaction.toListItem(categoriesById[transaction.categoryId], tags)
                    },
                )
            }
        }
    }

    private fun LedgerTransaction.toListItem(
        category: com.mojtaba.folentra.core.data.model.LedgerCategory?,
        tags: List<LedgerTag>,
    ) = TransactionUiFormatters.listItem(
        transaction = this,
        category = category,
        tags = tags,
    )

    private companion object {
        const val RECENT_TRANSACTION_LIMIT = 200
    }
}
