package com.mojtaba.folentra.feature.transaction.presentation.editor

import androidx.lifecycle.SavedStateHandle
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.ai.AiInferenceResult
import com.mojtaba.folentra.core.ai.AiResultQuality
import com.mojtaba.folentra.core.ai.SmartAutofillCandidates
import com.mojtaba.folentra.core.ai.SmartAutofillCategory
import com.mojtaba.folentra.core.ai.SmartAutofillHistoryItem
import com.mojtaba.folentra.core.ai.SmartAutofillInput
import com.mojtaba.folentra.core.ai.SmartAutofillRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.folentra.core.data.model.LedgerTransaction
import com.mojtaba.folentra.core.data.model.TransactionTagLink
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.feature.transaction.form.TransactionFormMode
import com.mojtaba.folentra.feature.transaction.form.TransactionFormState
import com.mojtaba.folentra.feature.transaction.form.TransactionFormValidation
import com.mojtaba.folentra.feature.transaction.form.TransactionType
import com.mojtaba.folentra.feature.transaction.form.ValidatedTransactionInput
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionEditorViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val savedStateHandle: SavedStateHandle,
    initialMode: TransactionFormMode = TransactionFormMode.CREATE,
    initialTransactionId: String? = null,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val aiFallbackStrategy: AiFallbackStrategy? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TransactionEditorUiState(
            formState = restoredFormState(initialMode, initialTransactionId),
            selectedTagIds = restoredSelectedTagIds(),
            isLoading = shouldLoadEditTransaction(initialMode),
        ),
    )
    val uiState: StateFlow<TransactionEditorUiState> = _uiState.asStateFlow()

    private val _events = Channel<TransactionEditorEvent>(Channel.BUFFERED)
    val events: Flow<TransactionEditorEvent> = _events.receiveAsFlow()

    init {
        updateValidation()
        observeCategories()
        observeTags()
        loadEditTransactionIfNeeded(initialMode)
    }

    fun onAction(action: TransactionEditorAction) {
        when (action) {
            is TransactionEditorAction.AmountChanged -> updateForm { copy(amountInput = action.value) }
            is TransactionEditorAction.TypeChanged -> updateType(action.value)
            is TransactionEditorAction.CategoryChanged -> updateForm { copy(categoryId = action.categoryId) }
            is TransactionEditorAction.TagToggled -> toggleTag(action.tagId)
            is TransactionEditorAction.MerchantChanged -> updateForm { copy(merchant = action.value) }
            is TransactionEditorAction.NoteChanged -> updateForm { copy(note = action.value) }
            is TransactionEditorAction.DateChanged -> updateForm { copy(occurredAt = action.value) }
            is TransactionEditorAction.CurrencyChanged -> updateForm { copy(currencyCode = action.value) }
            is TransactionEditorAction.RecurringChanged -> updateForm { copy(isRecurring = action.value) }
            TransactionEditorAction.SmartAutofillClicked -> generateSmartAutofillSuggestion()
            TransactionEditorAction.SmartAutofillAccepted -> acceptSmartAutofillSuggestion()
            TransactionEditorAction.SmartAutofillDismissed -> _uiState.update { it.copy(autofillSuggestion = null) }
            TransactionEditorAction.SaveClicked -> save()
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.observeActiveCategories().collect { categories ->
                _uiState.update { state ->
                    state.copy(
                        categories = categories.map {
                            TransactionCategoryOption(
                                id = it.id,
                                name = it.name,
                                type = it.type,
                            )
                        },
                    )
                }
            }
        }
    }

    private fun observeTags() {
        viewModelScope.launch {
            tagRepository.observeTags().collect { tags ->
                _uiState.update { state ->
                    state.copy(
                        tags = tags.map {
                            TransactionTagOption(
                                id = it.id,
                                name = it.name,
                            )
                        },
                    )
                }
            }
        }
    }

    private fun loadEditTransactionIfNeeded(initialMode: TransactionFormMode) {
        if (!shouldLoadEditTransaction(initialMode)) return

        viewModelScope.launch {
            val transactionId = _uiState.value.formState.transactionId
            if (transactionId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(TransactionEditorEvent.ShowSnackbar("Transaction id is required for editing."))
                return@launch
            }

            val transaction = transactionRepository.getById(transactionId)
            if (transaction == null) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(TransactionEditorEvent.ShowSnackbar("Transaction was not found."))
                return@launch
            }

            val selectedTags = tagRepository.observeTagsForTransaction(transactionId)
                .first()
                .map { it.id }
                .toSet()

            val formState = transaction.toFormState()
            persistFormState(formState)
            persistSelectedTagIds(selectedTags)
            savedStateHandle[KEY_EDIT_LOADED] = true

            _uiState.update {
                it.copy(
                    formState = formState,
                    selectedTagIds = selectedTags,
                    isLoading = false,
                )
            }
            updateValidation()
        }
    }

    private fun save() {
        val currentState = _uiState.value
        val validation = TransactionFormValidation.validate(
            state = currentState.formState,
            currentTimeMillis = currentTimeMillis(),
        )
        if (!validation.isValid) {
            _uiState.update { it.copy(validationResult = validation) }
            viewModelScope.launch {
                _events.send(TransactionEditorEvent.ShowSnackbar("Fix validation errors before saving."))
            }
            return
        }

        val input = requireNotNull(validation.validatedInput)
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationResult = validation) }
            runCatching {
                saveValidatedInput(input, currentState.formState.mode, currentState.selectedTagIds)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _events.send(TransactionEditorEvent.SaveCompleted)
            }.onFailure { throwable ->
                _uiState.update { it.copy(isSaving = false) }
                _events.send(
                    TransactionEditorEvent.ShowSnackbar(
                        throwable.message ?: "Unable to save transaction.",
                    ),
                )
            }
        }
    }

    private suspend fun saveValidatedInput(
        input: ValidatedTransactionInput,
        mode: TransactionFormMode,
        selectedTagIds: Set<String>,
    ) {
        val now = currentTimeMillis()
        val transactionId = input.transactionId ?: idGenerator()
        val existing = if (mode == TransactionFormMode.EDIT) {
            transactionRepository.getById(transactionId)
        } else {
            null
        }
        val amountMinor = when (input.type) {
            TransactionType.EXPENSE -> -input.amountMinor
            TransactionType.INCOME -> input.amountMinor
        }

        transactionRepository.upsert(
            LedgerTransaction(
                id = transactionId,
                amountMinor = amountMinor,
                currencyCode = input.currencyCode,
                type = input.type.name.lowercase(Locale.US),
                occurredAt = input.occurredAt,
                categoryId = input.categoryId,
                merchant = input.merchant,
                note = input.note,
                source = existing?.source ?: "manual",
                isRecurring = input.isRecurring,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )

        val existingTagIds = if (mode == TransactionFormMode.EDIT) {
            tagRepository.observeTagsForTransaction(transactionId).first().map { it.id }.toSet()
        } else {
            emptySet()
        }
        existingTagIds.minus(selectedTagIds).forEach { tagId ->
            tagRepository.removeTagFromTransaction(transactionId, tagId)
        }
        selectedTagIds.minus(existingTagIds).forEach { tagId ->
            tagRepository.addTagToTransaction(TransactionTagLink(transactionId, tagId))
        }
    }

    private fun generateSmartAutofillSuggestion() {
        val strategy = aiFallbackStrategy ?: return
        val state = _uiState.value
        val description = state.formState.merchant.ifBlank { state.formState.note }
        if (description.isBlank()) {
            viewModelScope.launch { _events.send(TransactionEditorEvent.ShowSnackbar("Add a merchant or note before requesting a suggestion.")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAutofillLoading = true, autofillSuggestion = null) }
            val history = transactionRepository.observeRecentTransactions(limit = 50).first()
            val request = SmartAutofillRequest(
                partialInput = SmartAutofillInput(
                    description = state.formState.merchant,
                    note = state.formState.note,
                    transactionType = state.formState.transactionType.name.lowercase(Locale.US),
                    categoryId = state.formState.categoryId,
                    amountMinor = state.formState.amountInput.toMinorOrNull(),
                ),
                candidates = SmartAutofillCandidates(
                    categories = state.categories.map { category ->
                        SmartAutofillCategory(category.id, category.name, category.type)
                    },
                ),
                history = history.map { transaction ->
                    SmartAutofillHistoryItem(
                        transactionId = transaction.id,
                        description = transaction.merchant,
                        note = transaction.note,
                        transactionType = transaction.type,
                        categoryId = transaction.categoryId,
                        amountMinor = kotlin.math.abs(transaction.amountMinor),
                        isRecurring = transaction.isRecurring,
                        occurredAtMillis = transaction.occurredAt,
                    )
                },
                occurredAtMillis = state.formState.occurredAt ?: currentTimeMillis(),
            )
            val suggestion = when (val result = strategy.smartAutofill(request)) {
                is AiInferenceResult.Success -> result.value.suggestion?.let { autofill ->
                    TransactionAutofillSuggestionUiModel(
                        categoryId = autofill.categoryId,
                        categoryName = state.categories.firstOrNull { it.id == autofill.categoryId }?.name,
                        recurring = autofill.recurring,
                        amountInput = autofill.amountMinor?.toDisplayAmountInput(),
                        note = autofill.note,
                        confidenceLabel = result.value.confidence.label,
                        reason = autofill.reason,
                    )
                }
                is AiInferenceResult.Unavailable,
                is AiInferenceResult.Failure,
                -> null
            }
            _uiState.update { it.copy(isAutofillLoading = false, autofillSuggestion = suggestion) }
            if (suggestion == null) {
                _events.send(TransactionEditorEvent.ShowSnackbar("No local autofill suggestion found."))
            }
        }
    }

    private fun acceptSmartAutofillSuggestion() {
        val suggestion = _uiState.value.autofillSuggestion ?: return
        updateForm {
            copy(
                categoryId = suggestion.categoryId ?: categoryId,
                amountInput = suggestion.amountInput ?: amountInput,
                isRecurring = suggestion.recurring ?: isRecurring,
                note = suggestion.note ?: note,
            )
        }
        _uiState.update { it.copy(autofillSuggestion = null) }
    }

    private fun updateType(type: TransactionType) {
        updateForm {
            copy(
                transactionType = type,
                categoryId = if (type == TransactionType.INCOME) null else categoryId,
            )
        }
    }

    private fun updateForm(reducer: TransactionFormState.() -> TransactionFormState) {
        val updatedFormState = _uiState.value.formState.reducer()
        persistFormState(updatedFormState)
        _uiState.update { it.copy(formState = updatedFormState, autofillSuggestion = null) }
        updateValidation()
    }

    private fun toggleTag(tagId: String) {
        val updatedTagIds = _uiState.value.selectedTagIds.toMutableSet().apply {
            if (!add(tagId)) remove(tagId)
        }.toSet()
        persistSelectedTagIds(updatedTagIds)
        _uiState.update { it.copy(selectedTagIds = updatedTagIds) }
    }

    private fun updateValidation() {
        _uiState.update {
            it.copy(
                validationResult = TransactionFormValidation.validate(
                    state = it.formState,
                    currentTimeMillis = currentTimeMillis(),
                ),
            )
        }
    }

    private fun shouldLoadEditTransaction(initialMode: TransactionFormMode): Boolean =
        restoredMode(initialMode) == TransactionFormMode.EDIT &&
            savedStateHandle.get<Boolean>(KEY_EDIT_LOADED) != true &&
            savedStateHandle.get<Boolean>(KEY_FORM_RESTORED) != true

    private fun restoredFormState(
        initialMode: TransactionFormMode,
        initialTransactionId: String?,
    ): TransactionFormState {
        val mode = restoredMode(initialMode)
        return TransactionFormState(
            mode = mode,
            transactionId = savedStateHandle[KEY_TRANSACTION_ID] ?: initialTransactionId,
            amountInput = savedStateHandle[KEY_AMOUNT_INPUT] ?: "",
            transactionType = restoredTransactionType(),
            categoryId = savedStateHandle[KEY_CATEGORY_ID],
            occurredAt = savedStateHandle[KEY_OCCURRED_AT] ?: currentTimeMillis(),
            merchant = savedStateHandle[KEY_MERCHANT] ?: "",
            note = savedStateHandle[KEY_NOTE] ?: "",
            currencyCode = savedStateHandle[KEY_CURRENCY_CODE] ?: TransactionFormState.DEFAULT_CURRENCY_CODE,
            isRecurring = savedStateHandle[KEY_IS_RECURRING] ?: false,
        )
    }

    private fun restoredMode(initialMode: TransactionFormMode): TransactionFormMode =
        savedStateHandle.get<String>(KEY_MODE)
            ?.let { runCatching { TransactionFormMode.valueOf(it) }.getOrNull() }
            ?: initialMode

    private fun restoredTransactionType(): TransactionType =
        savedStateHandle.get<String>(KEY_TRANSACTION_TYPE)
            ?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }
            ?: TransactionType.EXPENSE

    private fun restoredSelectedTagIds(): Set<String> =
        savedStateHandle.get<ArrayList<String>>(KEY_SELECTED_TAG_IDS)?.toSet() ?: emptySet()

    private fun persistFormState(state: TransactionFormState) {
        savedStateHandle[KEY_FORM_RESTORED] = true
        savedStateHandle[KEY_MODE] = state.mode.name
        savedStateHandle[KEY_TRANSACTION_ID] = state.transactionId
        savedStateHandle[KEY_AMOUNT_INPUT] = state.amountInput
        savedStateHandle[KEY_TRANSACTION_TYPE] = state.transactionType.name
        savedStateHandle[KEY_CATEGORY_ID] = state.categoryId
        savedStateHandle[KEY_OCCURRED_AT] = state.occurredAt
        savedStateHandle[KEY_MERCHANT] = state.merchant
        savedStateHandle[KEY_NOTE] = state.note
        savedStateHandle[KEY_CURRENCY_CODE] = state.currencyCode
        savedStateHandle[KEY_IS_RECURRING] = state.isRecurring
    }

    private fun persistSelectedTagIds(tagIds: Set<String>) {
        savedStateHandle[KEY_SELECTED_TAG_IDS] = ArrayList(tagIds)
    }

    private fun LedgerTransaction.toFormState(): TransactionFormState {
        val type = if (type.equals("income", ignoreCase = true)) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }
        return TransactionFormState(
            mode = TransactionFormMode.EDIT,
            transactionId = id,
            amountInput = amountMinor.toDisplayAmountInput(),
            transactionType = type,
            categoryId = categoryId,
            occurredAt = occurredAt,
            merchant = merchant.orEmpty(),
            note = note.orEmpty(),
            currencyCode = currencyCode,
            isRecurring = isRecurring,
        )
    }

    private fun String.toMinorOrNull(): Long? =
        runCatching {
            BigDecimal(this.trim()).movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact()
        }.getOrNull()

    private val AiResultQuality.label: String
        get() = name.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }

    private fun Long.toDisplayAmountInput(): String =
        BigDecimal.valueOf(kotlin.math.abs(this))
            .movePointLeft(2)
            .setScale(2, RoundingMode.UNNECESSARY)
            .stripTrailingZeros()
            .toPlainString()

    private companion object {
        const val KEY_FORM_RESTORED = "transaction_editor_form_restored"
        const val KEY_EDIT_LOADED = "transaction_editor_edit_loaded"
        const val KEY_MODE = "transaction_editor_mode"
        const val KEY_TRANSACTION_ID = "transaction_editor_transaction_id"
        const val KEY_AMOUNT_INPUT = "transaction_editor_amount_input"
        const val KEY_TRANSACTION_TYPE = "transaction_editor_transaction_type"
        const val KEY_CATEGORY_ID = "transaction_editor_category_id"
        const val KEY_OCCURRED_AT = "transaction_editor_occurred_at"
        const val KEY_MERCHANT = "transaction_editor_merchant"
        const val KEY_NOTE = "transaction_editor_note"
        const val KEY_CURRENCY_CODE = "transaction_editor_currency_code"
        const val KEY_IS_RECURRING = "transaction_editor_is_recurring"
        const val KEY_SELECTED_TAG_IDS = "transaction_editor_selected_tag_ids"
    }
}
