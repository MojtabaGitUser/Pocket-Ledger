package com.mojtaba.pocketledger.feature.transaction.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.feature.transaction.model.TransactionUiFormatters
import com.mojtaba.pocketledger.feature.transaction.navigation.TransactionRoutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlinx.coroutines.flow.first
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
    private val deleteUiState = MutableStateFlow(DeleteUiState())
    private var deletedTransactionSnapshot: DeletedTransactionSnapshot? = null

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
            TransactionDetailAction.DeleteClicked -> onDeleteClicked()
            TransactionDetailAction.DeleteConfirmed -> onDeleteConfirmed()
            TransactionDetailAction.DeleteDismissed -> onDeleteDismissed()
            TransactionDetailAction.UndoDeleteClicked -> onUndoDeleteClicked()
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
                            deleteUiState,
                        ) { category, tags, deleteState ->
                            TransactionDetailUiState.Content(
                                transaction = TransactionUiFormatters.detail(
                                    transaction = transaction,
                                    category = category,
                                    tags = tags,
                                ),
                                showDeleteConfirmation = deleteState.showDeleteConfirmation,
                                isDeleting = deleteState.isDeleting,
                            )
                        }
                    }
                }
        }

    private fun onDeleteClicked() {
        if (uiState.value !is TransactionDetailUiState.Content) return
        deleteUiState.update { it.copy(showDeleteConfirmation = true) }
    }

    private fun onDeleteDismissed() {
        if (deleteUiState.value.isDeleting) return
        deleteUiState.update { it.copy(showDeleteConfirmation = false) }
    }

    private fun onDeleteConfirmed() {
        if (deleteUiState.value.isDeleting) return
        val id = transactionId?.takeIf { it.isNotBlank() } ?: return emitDeleteFailed("Transaction not found.")
        deleteUiState.update { it.copy(showDeleteConfirmation = true, isDeleting = true) }

        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getById(id)
                if (transaction == null) {
                    deletedTransactionSnapshot = null
                    deleteUiState.value = DeleteUiState()
                    _events.emit(TransactionDetailEvent.ShowDeleteFailedSnackbar("Transaction not found."))
                    return@launch
                }

                val tagIds = tagRepository.observeTagsForTransaction(id)
                    .first()
                    .map { it.id }
                deletedTransactionSnapshot = DeletedTransactionSnapshot(
                    transaction = transaction,
                    tagIds = tagIds,
                )

                if (transactionRepository.deleteById(id)) {
                    deleteUiState.value = DeleteUiState()
                    _events.emit(TransactionDetailEvent.ShowDeleteUndoSnackbar)
                } else {
                    deletedTransactionSnapshot = null
                    deleteUiState.value = DeleteUiState()
                    _events.emit(TransactionDetailEvent.ShowDeleteFailedSnackbar("Transaction not found."))
                }
            } catch (throwable: Throwable) {
                deletedTransactionSnapshot = null
                deleteUiState.value = DeleteUiState()
                _events.emit(
                    TransactionDetailEvent.ShowDeleteFailedSnackbar(
                        throwable.message ?: "Unable to delete transaction.",
                    ),
                )
            }
        }
    }

    private fun onUndoDeleteClicked() {
        val snapshot = deletedTransactionSnapshot
        if (snapshot == null) {
            emitDeleteFailed("Unable to restore transaction.")
            return
        }

        viewModelScope.launch {
            try {
                transactionRepository.upsert(snapshot.transaction)
                snapshot.tagIds.forEach { tagId ->
                    tagRepository.addTagToTransaction(
                        TransactionTagLink(
                            transactionId = snapshot.transaction.id,
                            tagId = tagId,
                        ),
                    )
                }
                deletedTransactionSnapshot = null
            } catch (throwable: Throwable) {
                _events.emit(
                    TransactionDetailEvent.ShowDeleteFailedSnackbar(
                        throwable.message ?: "Unable to restore transaction.",
                    ),
                )
            }
        }
    }

    private fun emitDeleteFailed(message: String) {
        viewModelScope.launch {
            _events.emit(TransactionDetailEvent.ShowDeleteFailedSnackbar(message))
        }
    }

    private fun emitForCurrentId(factory: (String) -> TransactionDetailEvent) {
        val id = transactionId?.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            _events.emit(factory(id))
        }
    }

    private data class DeleteUiState(
        val showDeleteConfirmation: Boolean = false,
        val isDeleting: Boolean = false,
    )

    private data class DeletedTransactionSnapshot(
        val transaction: LedgerTransaction,
        val tagIds: List<String>,
    )
}
