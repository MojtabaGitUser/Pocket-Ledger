package com.mojtaba.folentra.core.data.mapper

import com.mojtaba.folentra.core.data.model.LedgerBudget
import com.mojtaba.folentra.core.database.model.BudgetEntity

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
