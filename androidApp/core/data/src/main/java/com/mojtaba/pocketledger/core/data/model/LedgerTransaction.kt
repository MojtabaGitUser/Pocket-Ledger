package com.mojtaba.pocketledger.core.data.model

data class LedgerTransaction(
    val id: String,
    val amountMinor: Long,
    val currencyCode: String,
    val type: String,
    val occurredAt: Long,
    val categoryId: String? = null,
    val merchant: String? = null,
    val note: String? = null,
    val source: String? = null,
    val isRecurring: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
