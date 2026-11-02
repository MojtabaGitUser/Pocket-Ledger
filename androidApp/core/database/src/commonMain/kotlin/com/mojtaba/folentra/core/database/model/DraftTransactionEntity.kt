package com.mojtaba.folentra.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Drafts never participate in ledger queries. Confirmed rows retain an idempotency marker. */
@Entity(tableName = "transaction_drafts")
data class DraftTransactionEntity(
    @PrimaryKey val id: String,
    val amountInput: String,
    val currencyCode: String,
    val type: String,
    val occurredAt: Long?,
    val categoryId: String?,
    val merchant: String,
    val note: String,
    val source: String,
    val createdAt: Long,
    val updatedAt: Long,
    val confirmedTransactionId: String? = null,
)
