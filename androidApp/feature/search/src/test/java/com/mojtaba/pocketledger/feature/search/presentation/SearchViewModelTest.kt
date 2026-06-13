package com.mojtaba.pocketledger.feature.search.presentation

import com.mojtaba.pocketledger.core.data.search.SearchAmountRange
import com.mojtaba.pocketledger.core.data.search.SearchDateRange
import com.mojtaba.pocketledger.core.data.search.SearchMode
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType
import com.mojtaba.pocketledger.core.featureflags.DefaultFeatureFlags
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.pocketledger.core.testing.coroutine.MainDispatcherRule
import com.mojtaba.pocketledger.core.testing.featureflags.FakeFeatureFlagProvider
import com.mojtaba.pocketledger.core.testing.fixture.TestClock
import com.mojtaba.pocketledger.core.testing.fixture.testIncomeCategory
import com.mojtaba.pocketledger.core.testing.fixture.testIncomeTransaction
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerCategory
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerTag
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerTransaction
import com.mojtaba.pocketledger.core.testing.fixture.testTransactionTagLink
import com.mojtaba.pocketledger.core.testing.repository.FakeCategoryRepository
import com.mojtaba.pocketledger.core.testing.repository.FakeTagRepository
import com.mojtaba.pocketledger.core.testing.repository.FakeTransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialStateShowsEmptyLedgerWhenNoTransactionsExist() = runTest {
        val viewModel = newViewModel(transactionRepository = FakeTransactionRepository())
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmptyLedger)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(SearchMode.Keyword, viewModel.uiState.value.query.mode)
        assertFalse(viewModel.uiState.value.capabilities.semanticSearchVisible)
        job.cancel()
    }

    @Test
    fun keywordChangeUpdatesSearchQueryAndResults() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(
                listOf(
                    testTransaction(id = "coffee", merchant = "Coffee Shop"),
                    testTransaction(id = "market", merchant = "Neighborhood Market"),
                ),
            ),
        )
        val job = launch { viewModel.uiState.collect {} }

        viewModel.onAction(SearchAction.KeywordChanged(" coffee "))
        advanceUntilIdle()

        assertEquals("coffee", viewModel.uiState.value.query.text)
        assertEquals(SearchMode.Keyword, viewModel.uiState.value.query.mode)
        assertEquals(listOf("coffee"), viewModel.uiState.value.results.map { it.transactionId })
        job.cancel()
    }

    @Test
    fun semanticCapabilityIsVisibleOnlyWhenFeatureFlagEnabled() = runTest {
        val featureFlags = FakeFeatureFlagProvider().apply {
            enable(DefaultFeatureFlags.SemanticSearchEnabled)
        }
        val viewModel = newViewModel(featureFlags = featureFlags)
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.capabilities.semanticSearchVisible)
        assertFalse(viewModel.uiState.value.capabilities.semanticSearchAvailable)
        job.cancel()
    }

    @Test
    fun selectingSemanticModeFallsBackToKeywordAndDoesNotRunSemanticSearch() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(testTransaction()))
        val featureFlags = FakeFeatureFlagProvider().apply {
            enable(DefaultFeatureFlags.SemanticSearchEnabled)
        }
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            featureFlags = featureFlags,
        )
        val job = launch { viewModel.uiState.collect {} }

        viewModel.onAction(SearchAction.SearchModeSelected(SearchMode.Semantic))
        advanceUntilIdle()

        assertEquals(SearchMode.Keyword, viewModel.uiState.value.query.mode)
        assertEquals("Semantic search is not available yet.", viewModel.uiState.value.modeUnavailableMessage)
        assertEquals(SearchMode.Keyword, transactionRepository.lastSearchQuery?.mode)
        assertTrue(transactionRepository.searchCalls > 0)
        job.cancel()
    }

    @Test
    fun typeCategoryTagDateAndAmountFiltersUpdateQuery() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(listOf(testTransaction())),
        )
        val job = launch { viewModel.uiState.collect {} }
        val range = SearchDateRange(TestClock.NovemberPeriodStart, TestClock.NovemberPeriodEnd)
        val amountRange = SearchAmountRange(minMinor = 1_000, maxMinor = 9_999)

        viewModel.onAction(SearchAction.TypeFilterChanged(SearchTransactionType.Expense))
        viewModel.onAction(SearchAction.CategoryToggled("food"))
        viewModel.onAction(SearchAction.TagToggled("work"))
        viewModel.onAction(SearchAction.DateRangeChanged(range))
        viewModel.onAction(SearchAction.AmountRangeChanged(amountRange))
        advanceUntilIdle()

        val filters = viewModel.uiState.value.query.filters
        assertEquals(setOf(SearchTransactionType.Expense), filters.transactionTypes)
        assertEquals(setOf("food"), filters.categoryIds)
        assertEquals(setOf("work"), filters.tagIds)
        assertEquals(range, filters.dateRange)
        assertEquals(amountRange, filters.amountRange)
        job.cancel()
    }

    @Test
    fun clearFiltersResetsKeywordAndFilters() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(listOf(testTransaction())),
        )
        val job = launch { viewModel.uiState.collect {} }

        viewModel.onAction(SearchAction.KeywordChanged("coffee"))
        viewModel.onAction(SearchAction.TypeFilterChanged(SearchTransactionType.Expense))
        viewModel.onAction(SearchAction.CategoryToggled("food"))
        viewModel.onAction(SearchAction.ClearFiltersClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.query.isEmpty)
        assertEquals(SearchMode.Keyword, viewModel.uiState.value.query.mode)
        assertEquals("", viewModel.uiState.value.keywordInput)
        job.cancel()
    }

    @Test
    fun repositoryResultsMapToUiResultsWithCategoryAndTags() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(
                listOf(testTransaction(id = "coffee")),
                listOf(testTransactionTagLink(transactionId = "coffee", tagId = "work")),
            ),
            categoryRepository = FakeCategoryRepository(listOf(testLedgerCategory(id = "food", name = "Food"))),
            tagRepository = FakeTagRepository(
                initialTags = listOf(testLedgerTag(id = "work", name = "Work")),
                initialLinks = listOf(testTransactionTagLink(transactionId = "coffee", tagId = "work")),
            ),
        )
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val result = viewModel.uiState.value.results.single()
        assertEquals("Coffee Shop", result.title)
        assertEquals("Food", result.categoryLabel)
        assertEquals(listOf("Work"), result.tagLabels)
        job.cancel()
    }

    @Test
    fun noResultsStateIsAvailableWhenFiltersMatchNothing() = runTest {
        val viewModel = newViewModel(
            transactionRepository = FakeTransactionRepository(listOf(testTransaction())),
        )
        val job = launch { viewModel.uiState.collect {} }

        viewModel.onAction(SearchAction.KeywordChanged("missing"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasNoResults)
        job.cancel()
    }

    @Test
    fun resultClickEmitsNavigationEffect() = runTest {
        val viewModel = newViewModel()
        val effects = mutableListOf<SearchEffect>()
        val job = launch { viewModel.effects.collect(effects::add) }

        viewModel.onAction(SearchAction.ResultClicked("transaction-1"))
        advanceUntilIdle()

        assertEquals(SearchEffect.OpenTransaction("transaction-1"), effects.single())
        job.cancel()
    }

    @Test
    fun repositoryErrorShowsErrorState() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(testTransaction())).apply {
            throwOnSearch = true
        }
        val viewModel = newViewModel(transactionRepository = transactionRepository)
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertEquals("Search failed", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        job.cancel()
    }

    private fun newViewModel(
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(
            listOf(testTransaction(), testIncomeTransaction()),
        ),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(
            listOf(
                testLedgerCategory(id = "food", name = "Food", type = "expense"),
                testIncomeCategory(id = "salary", name = "Salary"),
            ),
        ),
        tagRepository: FakeTagRepository = FakeTagRepository(
            initialTags = listOf(testLedgerTag(id = "work", name = "Work")),
        ),
        featureFlags: FakeFeatureFlagProvider = FakeFeatureFlagProvider(),
    ): SearchViewModel = SearchViewModel(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        tagRepository = tagRepository,
        featureFlags = FeatureFlagEvaluator(featureFlags),
    )

    private fun testTransaction(
        id: String = "transaction-1",
        merchant: String = "Coffee Shop",
    ) = testLedgerTransaction(
        id = id,
        amountMinor = -4_250,
        categoryId = "food",
        merchant = merchant,
        note = "Team breakfast",
        occurredAt = TestClock.November14,
    )
}
