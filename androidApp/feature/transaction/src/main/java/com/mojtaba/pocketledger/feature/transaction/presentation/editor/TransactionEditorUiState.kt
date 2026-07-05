package com.mojtaba.pocketledger.feature.transaction.presentation.editor

import com.mojtaba.pocketledger.feature.transaction.form.TransactionFormState
import com.mojtaba.pocketledger.feature.transaction.form.TransactionFormValidation
import com.mojtaba.pocketledger.feature.transaction.form.TransactionFormValidationResult

data class TransactionEditorUiState(
    val formState: TransactionFormState = TransactionFormState(),
    val validationResult: TransactionFormValidationResult =
        TransactionFormValidation.validate(TransactionFormState(), currentTimeMillis = Long.MAX_VALUE),
    val categories: List<TransactionCategoryOption> = emptyList(),
    val tags: List<TransactionTagOption> = emptyList(),
    val selectedTagIds: Set<String> = emptySet(),
    val autofillSuggestion: TransactionAutofillSuggestionUiModel? = null,
    val isAutofillLoading: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = validationResult.isSubmitReady && !isSaving && !isLoading
}

data class TransactionCategoryOption(
    val id: String,
    val name: String,
    val type: String,
)

data class TransactionTagOption(
    val id: String,
    val name: String,
)

data class TransactionAutofillSuggestionUiModel(
    val categoryId: String?,
    val categoryName: String?,
    val recurring: Boolean?,
    val amountInput: String?,
    val confidenceLabel: String,
    val reason: String,
)
