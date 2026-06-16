package com.mojtaba.pocketledger.core.testing.repository

import com.mojtaba.pocketledger.core.data.search.SearchAmountRange
import com.mojtaba.pocketledger.core.data.search.SearchDateRange
import com.mojtaba.pocketledger.core.data.search.SearchFilters
import com.mojtaba.pocketledger.core.data.search.SearchQuery
import com.mojtaba.pocketledger.core.data.search.SearchRecurringFilter
import com.mojtaba.pocketledger.core.data.search.SearchSort
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType
import com.mojtaba.pocketledger.core.testing.fixture.TestClock
import com.mojtaba.pocketledger.core.testing.fixture.TestIds
import com.mojtaba.pocketledger.core.testing.fixture.testIncomeTransaction
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerBudget
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerCategory
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerTag
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerTransaction
import com.mojtaba.pocketledger.core.testing.fixture.testTransactionTagLink
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeRepositoryTest {
    @Test
    fun transactionRepositoryEmitsUpsertAndDeleteUpdates() = runTest {
        val repository = FakeTransactionRepository()

        assertEquals(emptyList<String>(), repository.observeRecentTransactions(limit = 10).first().map { it.id })

        repository.upsert(testLedgerTransaction())
        repository.upsert(testIncomeTransaction(amountMinor = 10_000))

        assertEquals(
            listOf(TestIds.TransactionGroceries, TestIds.TransactionSalary),
            repository.observeRecentTransactions(limit = 10).first().map { it.id },
        )
        assertTrue(repository.deleteById(TestIds.TransactionGroceries))
        assertFalse(repository.deleteById(TestIds.TransactionGroceries))
        assertEquals(2, repository.deleteByIdCalls)
        assertEquals(listOf(TestIds.TransactionSalary), repository.observeRecentTransactions(limit = 10).first().map { it.id })
    }

    @Test
    fun transactionRepositoryFiltersByDateCategoryTypeAndTag() = runTest {
        val repository = FakeTransactionRepository(
            initialTransactions = listOf(
                testLedgerTransaction(
                    id = "older",
                    occurredAt = TestClock.November14,
                    categoryId = TestIds.CategoryGroceries,
                ),
                testLedgerTransaction(
                    id = "newer",
                    occurredAt = TestClock.November16,
                    categoryId = TestIds.CategoryDining,
                ),
            ),
            initialTagLinks = listOf(testTransactionTagLink(transactionId = "newer", tagId = TestIds.TagWeekend)),
        )

        assertEquals(listOf("newer"), repository.observeTransactionsByDateRange(TestClock.November16, TestClock.November16).first().map { it.id })
        assertEquals(listOf("older"), repository.observeTransactionsByCategory(TestIds.CategoryGroceries).first().map { it.id })
        assertEquals(listOf("newer", "older"), repository.observeTransactionsByType("expense").first().map { it.id })
        assertEquals(listOf("newer"), repository.observeTransactionsByTag(TestIds.TagWeekend).first().map { it.id })
    }

    @Test
    fun transactionRepositorySearchesKeywordPrefixAndSorts() = runTest {
        val repository = FakeTransactionRepository(
            initialTransactions = listOf(
                testLedgerTransaction(
                    id = "small",
                    amountMinor = -100,
                    merchant = "Coffee Shop",
                ),
                testLedgerTransaction(
                    id = "large",
                    amountMinor = -900,
                    merchant = "Coffee Cart",
                    occurredAt = TestClock.November16,
                ),
                testLedgerTransaction(
                    id = "other",
                    amountMinor = -500,
                    merchant = "Grocery Store",
                ),
            ),
        )

        val observedIds = repository
            .searchTransactions(
                SearchQuery(
                    text = "  coffee ",
                    sort = SearchSort.AmountAscending,
                ),
            )
            .first()
            .map { it.id }

        assertEquals(listOf("large", "small"), observedIds)
    }

    @Test
    fun transactionRepositorySearchReturnsEmptyForInvalidQuery() = runTest {
        val repository = FakeTransactionRepository(
            initialTransactions = listOf(testLedgerTransaction(merchant = "Coffee Shop")),
        )

        val observedIds = repository
            .searchTransactions(SearchQuery(text = "a".repeat(SearchQuery.MAX_TEXT_LENGTH + 1)))
            .first()
            .map { it.id }

        assertEquals(emptyList<String>(), observedIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun transactionRepositoryTagObserverEmitsWhenTagLinksChange() = runTest {
        val repository = FakeTransactionRepository(
            initialTransactions = listOf(
                testLedgerTransaction(id = "untagged"),
                testLedgerTransaction(id = "tagged", occurredAt = TestClock.November16),
            ),
        )
        val observedIds = mutableListOf<List<String>>()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository
                .observeTransactionsByTag(TestIds.TagEssential)
                .map { transactions -> transactions.map { it.id } }
                .take(2)
                .toList(observedIds)
        }

        repository.setTagLinks(
            listOf(testTransactionTagLink(transactionId = "tagged", tagId = TestIds.TagEssential)),
        )
        advanceUntilIdle()

        assertEquals(listOf(emptyList<String>(), listOf("tagged")), observedIds)
        collectJob.cancel()
    }

    @Test
    fun transactionRepositorySearchAppliesCombinedFilters() = runTest {
        val repository = FakeTransactionRepository(
            initialTransactions = listOf(
                testLedgerTransaction(
                    id = "matching",
                    amountMinor = -2_500,
                    currencyCode = "usd",
                    type = "expense",
                    occurredAt = TestClock.November15,
                    categoryId = TestIds.CategoryGroceries,
                    merchant = "Coffee Shop",
                    isRecurring = true,
                ),
                testLedgerTransaction(
                    id = "wrong-tag",
                    amountMinor = -2_500,
                    merchant = "Coffee Shop",
                    isRecurring = true,
                ),
                testLedgerTransaction(
                    id = "wrong-amount",
                    amountMinor = -9_500,
                    merchant = "Coffee Shop",
                    isRecurring = true,
                ),
                testIncomeTransaction(
                    id = "wrong-type",
                    amountMinor = 2_500,
                ),
            ),
            initialTagLinks = listOf(
                testTransactionTagLink(transactionId = "matching", tagId = TestIds.TagEssential),
                testTransactionTagLink(transactionId = "wrong-amount", tagId = TestIds.TagEssential),
            ),
        )

        val observedIds = repository
            .searchTransactions(
                SearchQuery(
                    text = " coffee  ",
                    filters = SearchFilters(
                        transactionTypes = setOf(SearchTransactionType.Expense),
                        categoryIds = setOf(TestIds.CategoryGroceries),
                        tagIds = setOf(TestIds.TagEssential),
                        dateRange = SearchDateRange(
                            startMillis = TestClock.November14,
                            endMillis = TestClock.November16,
                        ),
                        amountRange = SearchAmountRange(minMinor = 2_000, maxMinor = 3_000),
                        currencyCode = " USD ",
                        recurring = SearchRecurringFilter.RecurringOnly,
                    ),
                ),
            )
            .first()
            .map { it.id }

        assertEquals(listOf("matching"), observedIds)
    }

    @Test
    fun categoryRepositoryFiltersActiveCategoriesByType() = runTest {
        val repository = FakeCategoryRepository(
            listOf(
                testLedgerCategory(id = "active", type = "expense", isActive = true),
                testLedgerCategory(id = "inactive", type = "expense", isActive = false),
                testLedgerCategory(id = "income", type = "income", isActive = true),
            ),
        )

        assertEquals(listOf("active"), repository.observeActiveCategoriesByType("expense").first().map { it.id })
    }

    @Test
    fun budgetRepositoryFiltersActiveCategoryAndPeriod() = runTest {
        val repository = FakeBudgetRepository(
            listOf(
                testLedgerBudget(id = "matching", categoryId = TestIds.CategoryGroceries),
                testLedgerBudget(id = "inactive", categoryId = TestIds.CategoryGroceries, isActive = false),
                testLedgerBudget(
                    id = "outside",
                    categoryId = TestIds.CategoryDining,
                    periodStart = 2_000_000_000_000L,
                    periodEnd = 2_000_100_000_000L,
                    isActive = false,
                ),
            ),
        )

        assertEquals(listOf("matching"), repository.observeActiveBudgets().first().map { it.id })
        assertEquals(listOf("matching", "inactive"), repository.observeBudgetsByCategory(TestIds.CategoryGroceries).first().map { it.id })
        assertEquals(listOf("matching", "inactive"), repository.observeBudgetsByPeriodRange(TestClock.November14, TestClock.November16).first().map { it.id })
    }

    @Test
    fun tagRepositoryDoesNotDuplicateLinks() = runTest {
        val repository = FakeTagRepository(
            initialTags = listOf(testLedgerTag(id = TestIds.TagEssential, name = "Essential")),
        )

        repository.addTagToTransaction(testTransactionTagLink())
        repository.addTagToTransaction(testTransactionTagLink())

        assertEquals(setOf(TestIds.TagEssential), repository.tagIdsForTransaction(TestIds.TransactionGroceries))
        assertEquals(listOf(TestIds.TagEssential), repository.observeTagsForTransaction(TestIds.TransactionGroceries).first().map { it.id })
        assertTrue(repository.removeTagFromTransaction(TestIds.TransactionGroceries, TestIds.TagEssential))
        assertFalse(repository.removeTagFromTransaction(TestIds.TransactionGroceries, TestIds.TagEssential))
    }
}
