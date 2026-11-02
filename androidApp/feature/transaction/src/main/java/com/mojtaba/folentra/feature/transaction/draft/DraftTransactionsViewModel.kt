package com.mojtaba.folentra.feature.transaction.draft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.folentra.core.data.model.DraftTransaction
import com.mojtaba.folentra.core.data.model.LedgerCategory
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.DraftTransactionRepository
import com.mojtaba.folentra.shared.domain.transaction.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class DraftTransactionsState(
    val drafts: List<DraftTransaction> = emptyList(),
    val categories: List<LedgerCategory> = emptyList(),
    val selected: DraftTransaction? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val message: String? = null,
    val validation: TransactionFormValidationResult? = null,
)

class DraftTransactionsViewModel(
    private val repository: DraftTransactionRepository,
    private val categories: CategoryRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) : ViewModel() {
    private val mutable = MutableStateFlow(DraftTransactionsState())
    val state = mutable.asStateFlow()

    init {
        viewModelScope.launch {
            try { repository.observePending().collect { rows ->
                mutable.update { it.copy(drafts = rows, loading = false) }
            } } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.update { it.copy(loading = false, message = "Unable to load drafts. Reopen this screen to retry.") } }
        }
        viewModelScope.launch {
            try { categories.observeActiveCategories().collect { rows ->
                mutable.update { it.copy(categories = rows) }
            } } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.update { it.copy(message = "Unable to load categories. Reopen this screen to retry.") } }
        }
    }

    fun create() = perform {
        val timestamp = now()
        val draft = DraftTransaction(id = newId(), occurredAt = timestamp, createdAt = timestamp, updatedAt = timestamp)
        repository.save(draft)
        mutable.update { it.copy(selected = draft, validation = draft.validation(now())) }
    }

    fun select(draft: DraftTransaction) {
        if (!state.value.busy) mutable.update { it.copy(selected = draft, message = null, validation = draft.validation(now())) }
    }
    fun edit(change: DraftTransaction.() -> DraftTransaction) {
        if (!state.value.busy) mutable.update {
            val draft = it.selected?.change()
            it.copy(selected = draft, message = null, validation = draft?.validation(now()))
        }
    }
    fun saveAndClose() {
        val draft = state.value.selected ?: return
        perform { repository.save(draft); mutable.update { it.copy(selected = null, message = "Draft saved. It has not been added to your ledger.") } }
    }
    fun discard() {
        val draft = state.value.selected ?: return
        perform { repository.discard(draft.id); mutable.update { it.copy(selected = null, message = "Draft discarded.") } }
    }
    fun confirm() {
        val draft = state.value.selected ?: return
        if (!draft.validation(now()).isValid || (draft.categoryId != null &&
                state.value.categories.none { it.id == draft.categoryId && it.type == draft.type })) {
            mutable.update { it.copy(message = "Review the amount, currency, date and category before confirming.") }
            return
        }
        perform {
            repository.confirm(draft)
            mutable.update { it.copy(selected = null, message = "Transaction added to your ledger.") }
        }
    }
    private fun perform(block: suspend () -> Unit) {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.update { it.copy(message = "Could not save changes. Your draft is still available; please try again.") } }
            finally { mutable.update { it.copy(busy = false) } }
        }
    }
}

internal fun DraftTransaction.validation(now: Long) = TransactionFormValidation.validate(
    TransactionFormState(amountInput = amountInput, currencyCode = currencyCode,
        transactionType = if (type == "income") TransactionType.INCOME else TransactionType.EXPENSE,
        occurredAt = occurredAt, categoryId = categoryId, merchant = merchant, note = note), now)
