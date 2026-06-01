package com.mojtaba.pocketledger.core.data.mapper

import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.database.model.TransactionEntity

internal fun TransactionEntity.asExternalModel(): LedgerTransaction = LedgerTransaction(
    id = id,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    type = type,
    occurredAt = occurredAt,
    categoryId = categoryId,
    merchant = merchant,
    note = note,
    source = source,
    isRecurring = isRecurring,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun LedgerTransaction.asEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    type = type,
    occurredAt = occurredAt,
    categoryId = categoryId,
    merchant = merchant,
    note = note,
    source = source,
    isRecurring = isRecurring,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
