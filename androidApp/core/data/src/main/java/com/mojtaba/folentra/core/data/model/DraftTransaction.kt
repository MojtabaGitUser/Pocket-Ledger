package com.mojtaba.folentra.core.data.model

/** Incomplete input is allowed until explicit confirmation. No image or OCR text is stored. */
data class DraftTransaction(
    val id: String,
    val amountInput: String = "",
    val currencyCode: String = "USD",
    val type: String = "expense",
    val occurredAt: Long? = null,
    val categoryId: String? = null,
    val merchant: String = "",
    val note: String = "",
    val source: String = "receipt_manual",
    val createdAt: Long,
    val updatedAt: Long,
    val confirmedTransactionId: String? = null,
)
