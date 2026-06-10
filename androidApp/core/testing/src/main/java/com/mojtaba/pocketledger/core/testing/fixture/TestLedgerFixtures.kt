package com.mojtaba.pocketledger.core.testing.fixture

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink

fun testLedgerCategory(
    id: String = TestIds.CategoryGroceries,
    name: String = "Groceries",
    type: String = "expense",
    colorHex: String? = "#1565C0",
    iconName: String? = "shopping_cart",
    sortOrder: Int = 10,
    isActive: Boolean = true,
    createdAt: Long = TestClock.CreatedAt,
    updatedAt: Long = TestClock.UpdatedAt,
): LedgerCategory = LedgerCategory(
    id = id,
    name = name,
    type = type,
    colorHex = colorHex,
    iconName = iconName,
    sortOrder = sortOrder,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun testIncomeCategory(
    id: String = TestIds.CategorySalary,
    name: String = "Salary",
): LedgerCategory = testLedgerCategory(
    id = id,
    name = name,
    type = "income",
    colorHex = "#2E7D32",
    iconName = "payments",
    sortOrder = 0,
)

fun testLedgerBudget(
    id: String = TestIds.BudgetGroceries,
    name: String = "Groceries budget",
    amountMinor: Long = 50_000,
    currencyCode: String = "USD",
    periodType: String = "monthly",
    periodStart: Long = TestClock.NovemberPeriodStart,
    periodEnd: Long = TestClock.NovemberPeriodEnd,
    categoryId: String? = TestIds.CategoryGroceries,
    isActive: Boolean = true,
    createdAt: Long = TestClock.CreatedAt,
    updatedAt: Long = TestClock.UpdatedAt,
): LedgerBudget = LedgerBudget(
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

fun testLedgerTag(
    id: String = TestIds.TagEssential,
    name: String = "Essential",
    colorHex: String? = "#2E7D32",
    createdAt: Long = TestClock.CreatedAt,
    updatedAt: Long = TestClock.UpdatedAt,
): LedgerTag = LedgerTag(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun testLedgerTransaction(
    id: String = TestIds.TransactionGroceries,
    amountMinor: Long = -8_632,
    currencyCode: String = "USD",
    type: String = "expense",
    occurredAt: Long = TestClock.November15,
    categoryId: String? = TestIds.CategoryGroceries,
    merchant: String? = "Neighborhood Market",
    note: String? = "Weekly groceries",
    source: String? = "manual",
    isRecurring: Boolean = false,
    createdAt: Long = TestClock.CreatedAt,
    updatedAt: Long = TestClock.UpdatedAt,
): LedgerTransaction = LedgerTransaction(
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

fun testIncomeTransaction(
    id: String = TestIds.TransactionSalary,
    amountMinor: Long = 250_000,
): LedgerTransaction = testLedgerTransaction(
    id = id,
    amountMinor = amountMinor,
    type = "income",
    categoryId = TestIds.CategorySalary,
    merchant = "Pocket Ledger Co.",
    note = "Paycheck",
    isRecurring = true,
)

fun testTransactionTagLink(
    transactionId: String = TestIds.TransactionGroceries,
    tagId: String = TestIds.TagEssential,
): TransactionTagLink = TransactionTagLink(
    transactionId = transactionId,
    tagId = tagId,
)
