package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.feature.transaction.model.TransactionUiFormatters
import com.mojtaba.pocketledger.feature.transaction.navigation.TransactionRoutes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    savedStateHandle: SavedStateHandle,
    initialTransactionId: String? = null,
) : ViewModel() {
    private val transactionId: String? =
        savedStateHandle[TransactionRoutes.TransactionIdArg] ?: initialTransactionId
    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<TransactionDetailUiState> = refreshRequests
        .flatMapLatest { observeUiState() }
        .onStart { emit(TransactionDetailUiState.Loading) }
        .catch { throwable ->
            emit(TransactionDetailUiState.Error(throwable.message ?: "Unable to load transaction."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionDetailUiState.Loading,
        )

    private val _events = MutableSharedFlow<TransactionDetailEvent>()
    val events: SharedFlow<TransactionDetailEvent> = _events.asSharedFlow()

    fun onAction(action: TransactionDetailAction) {
        when (action) {
            TransactionDetailAction.EditClicked -> emitForCurrentId { TransactionDetailEvent.EditTransaction(it) }
            TransactionDetailAction.DeleteClicked -> emitForCurrentId { TransactionDetailEvent.DeleteRequested(it) }
            TransactionDetailAction.RetryClicked -> refreshRequests.update { it + 1 }
        }
    }

    private fun observeUiState() =
        if (transactionId.isNullOrBlank()) {
            flowOf(TransactionDetailUiState.NotFound)
        } else {
            transactionRepository.observeById(transactionId)
                .flatMapLatest { transaction ->
                    if (transaction == null) {
                        flowOf(TransactionDetailUiState.NotFound)
                    } else {
                        combine(
                            transaction.categoryId
                                ?.let(categoryRepository::observeById)
                                ?: flowOf(null),
                            tagRepository.observeTagsForTransaction(transaction.id),
                        ) { category, tags ->
                            TransactionDetailUiState.Content(
                                transaction = TransactionUiFormatters.detail(
                                    transaction = transaction,
                                    category = category,
                                    tags = tags,
                                ),
                            )
                        }
                    }
                }
        }

    private fun emitForCurrentId(factory: (String) -> TransactionDetailEvent) {
        val id = transactionId?.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            _events.emit(factory(id))
        }
    }
}
