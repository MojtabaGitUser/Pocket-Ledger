package com.mojtaba.pocketledger.core.database

import com.mojtaba.pocketledger.core.database.model.BudgetEntity
import com.mojtaba.pocketledger.core.database.model.CategoryEntity
import com.mojtaba.pocketledger.core.database.model.TransactionEntity

internal fun testCategory(
    id: String = "category-food",
    name: String = "Food",
    type: String = "expense",
    isActive: Boolean = true,
): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    type = type,
    colorHex = "#2E7D32",
    iconName = "restaurant",
    sortOrder = 10,
    isActive = isActive,
    createdAt = 1_700_000_000_000,
    updatedAt = 1_700_000_000_000,
)

internal fun testTransaction(
    id: String = "transaction-1",
    amountMinor: Long = -1_250,
    occurredAt: Long = 1_700_000_100_000,
    categoryId: String? = "category-food",
    merchant: String? = "Coffee Shop",
    note: String? = "Latte",
    source: String? = "manual",
    updatedAt: Long = occurredAt,
): TransactionEntity = TransactionEntity(
    id = id,
    amountMinor = amountMinor,
    currencyCode = "USD",
    type = "expense",
    occurredAt = occurredAt,
    categoryId = categoryId,
    merchant = merchant,
    note = note,
    source = source,
    isRecurring = false,
    createdAt = 1_700_000_100_000,
    updatedAt = updatedAt,
)

internal fun testBudget(
    id: String = "budget-food",
    categoryId: String? = "category-food",
    isActive: Boolean = true,
): BudgetEntity = BudgetEntity(
    id = id,
    name = "Food budget",
    amountMinor = 50_000,
    currencyCode = "USD",
    periodType = "monthly",
    periodStart = 1_700_000_000_000,
    periodEnd = 1_702_678_399_999,
    categoryId = categoryId,
    isActive = isActive,
    createdAt = 1_700_000_000_000,
    updatedAt = 1_700_000_000_000,
)
