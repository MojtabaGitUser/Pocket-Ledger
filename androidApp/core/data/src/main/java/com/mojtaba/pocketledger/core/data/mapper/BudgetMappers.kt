package com.mojtaba.pocketledger.core.data.mapper

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.database.model.BudgetEntity

internal fun BudgetEntity.asExternalModel(): LedgerBudget = LedgerBudget(
    id = id,
    name = name,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    periodType = periodType,
    periodStart = periodStart,
    periodEnd = periodEnd,
    categoryId = categoryId,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun LedgerBudget.asEntity(): BudgetEntity = BudgetEntity(
    id = id,
    name = name,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    periodType = periodType,
    periodStart = periodStart,
    periodEnd = periodEnd,
    categoryId = categoryId,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
