package com.mojtaba.folentra.core.data.model

data class LedgerBudget(
    val id: String,
    val name: String,
    val amountMinor: Long,
    val currencyCode: String,
    val periodType: String,
    val periodStart: Long,
    val periodEnd: Long,
    val categoryId: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
)
