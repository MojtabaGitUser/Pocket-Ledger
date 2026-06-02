package com.mojtaba.pocketledger.feature.transaction.form

/**
 * UI-friendly, Compose-independent state for creating or editing a transaction.
 *
 * `USD` is the default because existing transaction fixtures use USD and no user currency
 * preference exists yet. Expense transactions require a category; income categories are optional
 * until product requirements define income categorization.
 */
data class TransactionFormState(
    val mode: TransactionFormMode = TransactionFormMode.CREATE,
    val transactionId: String? = null,
    val amountInput: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val categoryId: String? = null,
    val occurredAt: Long? = null,
    val merchant: String = "",
    val note: String = "",
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
    val isRecurring: Boolean = false,
) {
    companion object {
        const val DEFAULT_CURRENCY_CODE = "USD"
    }
}
