package com.mojtaba.pocketledger.feature.transaction.form

data class ValidatedTransactionInput(
    val transactionId: String?,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: String?,
    val occurredAt: Long,
    val merchant: String?,
    val note: String?,
    val currencyCode: String,
    val isRecurring: Boolean,
)
