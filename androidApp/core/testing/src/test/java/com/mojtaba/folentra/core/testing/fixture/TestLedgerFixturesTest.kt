package com.mojtaba.folentra.core.testing.fixture

import org.junit.Assert.assertEquals
import org.junit.Test

class TestLedgerFixturesTest {
    @Test
    fun transactionFixtureUsesStableDefaultsAndAllowsOverrides() {
        val transaction = testLedgerTransaction(amountMinor = -12_345)

        assertEquals(TestIds.TransactionGroceries, transaction.id)
        assertEquals(-12_345L, transaction.amountMinor)
        assertEquals("USD", transaction.currencyCode)
        assertEquals("expense", transaction.type)
        assertEquals(TestClock.November15, transaction.occurredAt)
        assertEquals(TestIds.CategoryGroceries, transaction.categoryId)
    }

    @Test
    fun categoryBudgetTagAndLinkFixturesUseStableIds() {
        assertEquals(TestIds.CategoryGroceries, testLedgerCategory().id)
        assertEquals(TestIds.CategorySalary, testIncomeCategory().id)
        assertEquals(TestIds.BudgetGroceries, testLedgerBudget().id)
        assertEquals(TestIds.TagEssential, testLedgerTag().id)

        val link = testTransactionTagLink()

        assertEquals(TestIds.TransactionGroceries, link.transactionId)
        assertEquals(TestIds.TagEssential, link.tagId)
    }
}
