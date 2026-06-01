package com.mojtaba.pocketledger.core.data.repository.local

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction

internal fun testCategory(
    id: String = "category-food",
    name: String = "Food",
    type: String = "expense",
    isActive: Boolean = true,
): LedgerCategory = LedgerCategory(
    id = id,
    name = name,
    type = type,
    colorHex = "#2E7D32",
    iconName = "restaurant",
    sortOrder = 10,
    isActive = isActive,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_CREATED_AT,
)

internal fun testTransaction(
    id: String = "transaction-1",
    amountMinor: Long = -1_250,
    occurredAt: Long = TEST_OCCURRED_AT,
    categoryId: String? = "category-food",
): LedgerTransaction = LedgerTransaction(
    id = id,
    amountMinor = amountMinor,
    currencyCode = "USD",
    type = "expense",
    occurredAt = occurredAt,
    categoryId = categoryId,
    merchant = "Coffee Shop",
    note = "Latte",
    source = "manual",
    isRecurring = false,
    createdAt = occurredAt,
    updatedAt = occurredAt,
)

internal fun testBudget(
    id: String = "budget-food",
    categoryId: String? = "category-food",
    isActive: Boolean = true,
): LedgerBudget = LedgerBudget(
    id = id,
    name = "Food budget",
    amountMinor = 50_000,
    currencyCode = "USD",
    periodType = "monthly",
    periodStart = TEST_PERIOD_START,
    periodEnd = TEST_PERIOD_END,
    categoryId = categoryId,
    isActive = isActive,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_CREATED_AT,
)

internal fun testTag(
    id: String = "tag-weekend",
    name: String = "Weekend",
): LedgerTag = LedgerTag(
    id = id,
    name = name,
    colorHex = "#1565C0",
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_CREATED_AT,
)

internal const val TEST_CREATED_AT = 1_700_000_000_000L
internal const val TEST_OCCURRED_AT = 1_700_000_100_000L
internal const val TEST_PERIOD_START = 1_700_000_000_000L
internal const val TEST_PERIOD_END = 1_702_678_399_999L
