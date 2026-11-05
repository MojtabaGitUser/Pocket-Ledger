package com.mojtaba.folentra.feature.transaction.draft

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.folentra.core.data.model.DraftTransaction
import com.mojtaba.folentra.core.data.model.LedgerCategory
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.DraftTransactionRepository
import com.mojtaba.folentra.core.data.receipt.*
import com.mojtaba.folentra.shared.domain.transaction.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

data class DraftTransactionsState(
    val drafts: List<DraftTransaction> = emptyList(),
    val categories: List<LedgerCategory> = emptyList(),
    val selected: DraftTransaction? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val scanning: Boolean = false,
    val message: String? = null,
    val scanSummary: List<String> = emptyList(),
    val validation: TransactionFormValidationResult? = null,
)

class DraftTransactionsViewModel(
    private val repository: DraftTransactionRepository,
    private val categories: CategoryRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
    private val recognizer: ReceiptTextRecognizer? = null,
    private val saved: SavedStateHandle = SavedStateHandle(),
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val writes = Mutex()
    private val restored = restore()
    private val mutable = MutableStateFlow(DraftTransactionsState(
        selected = restored, validation = restored?.validation(now()),
        scanSummary = saved.get<ArrayList<String>>("scanSummary").orEmpty(),
        message = if (saved.get<Boolean>("scanning") == true) "Scanning was interrupted. Choose the image again or continue manually." else null,
    ))
    val state = mutable.asStateFlow()

    init {
        saved["scanning"] = false
        viewModelScope.launch {
            var reconcileRestored = restored != null
            try { repository.observePending().collect { rows ->
                mutable.update { it.copy(drafts = rows, loading = false) }
                if (reconcileRestored) {
                    reconcileRestored = false
                    if (rows.none { it.id == restored?.id } && !state.value.busy) {
                        close("This draft has already been confirmed or removed.")
                    }
                }
            } } catch (e: CancellationException) { throw e }
            catch (_: Exception) { report("Unable to load drafts. Reopen this screen to retry.") }
        }
        viewModelScope.launch {
            try { categories.observeActiveCategories().collect { rows ->
                mutable.update { it.copy(categories = rows) }
            } } catch (e: CancellationException) { throw e }
            catch (_: Exception) { report("Unable to load categories. Reopen this screen to retry.") }
        }
    }

    fun create() = perform {
        val timestamp = now()
        val draft = DraftTransaction(id = newId(), occurredAt = timestamp, createdAt = timestamp, updatedAt = timestamp)
        writes.withLock { repository.save(draft) }
        selectInternal(draft)
    }

    /** Persist a stable draft before leaving for the system camera/picker. */
    fun prepareImage(onReady: () -> Unit) = perform {
        val timestamp = now()
        val draft = state.value.selected ?: DraftTransaction(
            id = newId(), currencyCode = "", occurredAt = null, source = "receipt_ocr",
            createdAt = timestamp, updatedAt = timestamp,
        )
        writes.withLock { repository.save(draft) }
        selectInternal(draft)
        onReady()
    }

    fun scan(uri: String, expectedDraftId: String? = state.value.selected?.id, onFinished: suspend () -> Unit = {}) {
        val original = state.value.selected
        if (state.value.busy || original == null || original.id != expectedDraftId) {
            viewModelScope.launch { withContext(NonCancellable) { onFinished() } }
            return
        }
        perform {
            mutable.update { it.copy(scanning = true) }
            saved["scanning"] = true
            try {
                val text = requireNotNull(recognizer).recognize(uri)
                if (text.lines.none { it.text.isNotBlank() }) {
                    report("No readable text found. Try a clearer photo or enter the receipt manually.")
                    return@perform
                }
                val result = withContext(Dispatchers.Default) {
                    ReceiptParser().parse(text, Instant.ofEpochMilli(now()).atZone(zone).toLocalDate())
                }
                val merged = result.fillMissing(original, zone)
                val summary = listOf(
                    "Merchant: ${result.merchant.confidence.name.lowercase()}",
                    "Amount: ${result.amountMinor.confidence.name.lowercase()}",
                    "Date: ${result.date.confidence.name.lowercase()}",
                    "Currency: ${result.currency.confidence.name.lowercase()}",
                )
                // Checkpoint corrections/result before attempting a potentially failing DB write.
                selectInternal(merged)
                saved["scanSummary"] = ArrayList(summary)
                mutable.update { it.copy(scanSummary = summary) }
                writes.withLock { repository.save(merged) }
                report("Receipt read. Existing values were kept; only blank fields were filled. Check every field against the receipt before confirming.")
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                report("Could not finish reading or saving the receipt. Your current fields are kept. Save the draft, choose another photo, or enter details manually.")
            } finally {
                saved["scanning"] = false
                mutable.update { it.copy(scanning = false) }
                withContext(NonCancellable) { onFinished() }
            }
        }
    }

    fun select(draft: DraftTransaction) {
        if (!state.value.busy) {
            saved["scanSummary"] = arrayListOf<String>()
            selectInternal(draft)
            mutable.update { it.copy(scanSummary = emptyList()) }
        }
    }
    private fun selectInternal(draft: DraftTransaction) {
        checkpoint(draft)
        mutable.update { it.copy(selected = draft, message = null, validation = draft.validation(now())) }
    }
    fun edit(change: DraftTransaction.() -> DraftTransaction) {
        if (state.value.busy) return
        val draft = state.value.selected?.change() ?: return
        selectInternal(draft)
        // Serialize writes in edit order; confirmation/save cannot be overtaken by an old edit.
        viewModelScope.launch {
            try { writes.withLock { repository.save(draft) } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { report("Changes are not saved yet. Keep this screen open and retry Save draft.") }
        }
    }
    fun saveAndClose() {
        val draft = state.value.selected ?: return
        perform { writes.withLock { repository.save(draft) }; close("Draft saved. It has not been added to your ledger.") }
    }
    fun discard() {
        val draft = state.value.selected ?: return
        perform { writes.withLock { repository.discard(draft.id) }; close("Draft discarded.") }
    }
    fun confirm() {
        if (state.value.busy) return
        val draft = state.value.selected ?: return
        if (!draft.validation(now()).isValid || (draft.categoryId != null &&
                state.value.categories.none { it.id == draft.categoryId && it.type == draft.type })) {
            report("Review the amount, currency, date and category before confirming.")
            return
        }
        perform {
            writes.withLock { repository.confirm(draft) }
            close("Transaction added to your ledger.")
        }
    }
    fun report(message: String) { mutable.update { it.copy(message = message, loading = false) } }
    private fun close(message: String) {
        saved.remove<ArrayList<String>>("draft")
        saved.remove<ArrayList<String>>("scanSummary")
        mutable.update { it.copy(selected = null, validation = null, scanSummary = emptyList(), message = message) }
    }
    private fun perform(block: suspend () -> Unit) {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { report("Could not save changes. Your current fields are kept; please try again.") }
            finally { mutable.update { it.copy(busy = false) } }
        }
    }
    private fun checkpoint(d: DraftTransaction) {
        saved["draft"] = arrayListOf(d.id, d.amountInput, d.currencyCode, d.type, d.occurredAt?.toString().orEmpty(),
            d.categoryId.orEmpty(), d.merchant, d.note, d.source, d.createdAt.toString(), d.updatedAt.toString())
    }
    private fun restore(): DraftTransaction? {
        val d = saved.get<ArrayList<String>>("draft") ?: return null
        if (d.size != 11) return null
        return DraftTransaction(d[0], d[1], d[2], d[3], d[4].toLongOrNull(), d[5].ifEmpty { null }, d[6], d[7],
            d[8], d[9].toLongOrNull() ?: return null, d[10].toLongOrNull() ?: return null)
    }
}

internal fun DraftTransaction.validation(now: Long) = TransactionFormValidation.validate(
    TransactionFormState(amountInput = amountInput, currencyCode = currencyCode,
        transactionType = if (type == "income") TransactionType.INCOME else TransactionType.EXPENSE,
        occurredAt = occurredAt, categoryId = categoryId, merchant = merchant, note = note), now)
