package com.mojtaba.folentra.feature.transaction.presentation.editor

import com.mojtaba.folentra.feature.transaction.form.TransactionType

sealed interface TransactionEditorAction {
    data class AmountChanged(val value: String) : TransactionEditorAction
    data class TypeChanged(val value: TransactionType) : TransactionEditorAction
    data class CategoryChanged(val categoryId: String?) : TransactionEditorAction
    data class TagToggled(val tagId: String) : TransactionEditorAction
    data class MerchantChanged(val value: String) : TransactionEditorAction
    data class NoteChanged(val value: String) : TransactionEditorAction
    data class DateChanged(val value: Long?) : TransactionEditorAction
    data class CurrencyChanged(val value: String) : TransactionEditorAction
    data class RecurringChanged(val value: Boolean) : TransactionEditorAction
    data object SmartAutofillClicked : TransactionEditorAction
    data object SmartAutofillAccepted : TransactionEditorAction
    data object SmartAutofillDismissed : TransactionEditorAction
    data object SaveClicked : TransactionEditorAction
}
