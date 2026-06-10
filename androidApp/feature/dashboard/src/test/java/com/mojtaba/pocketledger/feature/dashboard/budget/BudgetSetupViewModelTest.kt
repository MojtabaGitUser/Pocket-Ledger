package com.mojtaba.pocketledger.feature.dashboard.budget

import androidx.lifecycle.SavedStateHandle
import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.contract.SyncState
import com.mojtaba.pocketledger.core.testing.coroutine.MainDispatcherRule
import com.mojtaba.pocketledger.core.testing.fixture.TestClock
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerBudget
import com.mojtaba.pocketledger.core.testing.fixture.testLedgerCategory
import com.mojtaba.pocketledger.core.testing.repository.FakeBudgetRepository
import com.mojtaba.pocketledger.core.testing.repository.FakeCategoryRepository
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetSetupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialCreateStateUsesDefaultsAndLoadsCategories() = runTest {
        val viewModel = newViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(BudgetSetupMode.CREATE, state.formState.mode)
        assertEquals("USD", state.formState.currencyCode)
        assertEquals(TestClock.NovemberPeriodStart, state.formState.periodStart)
        assertEquals(TestClock.NovemberPeriodEnd, state.formState.periodEnd)
        assertTrue(state.formState.isActive)
        assertEquals(listOf("Dining", "Food"), state.categories.map { it.name })
    }

    @Test
    fun fieldUpdatesChangeStateAndValidation() = runTest {
        val viewModel = newViewModel()

        viewModel.onAction(BudgetSetupAction.NameChanged("Food"))
        viewModel.onAction(BudgetSetupAction.AmountChanged("abc"))
        advanceUntilIdle()

        assertEquals("Food", viewModel.uiState.value.formState.nameInput)
        assertEquals(BudgetAmountError.INVALID_FORMAT, viewModel.uiState.value.validationResult.errors.amount)

        viewModel.onAction(BudgetSetupAction.AmountChanged("50.00"))
        viewModel.onAction(BudgetSetupAction.CurrencyChanged("cad"))
        viewModel.onAction(BudgetSetupAction.CategorySelected("food"))
        viewModel.onAction(BudgetSetupAction.ActiveChanged(false))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.validationResult.isValid)
        assertEquals("cad", state.formState.currencyCode)
        assertEquals("food", state.formState.categoryId)
        assertFalse(state.formState.isActive)
    }

    @Test
    fun invalidSaveShowsErrorsAndDoesNotWrite() = runTest {
        val budgetRepository = FakeBudgetRepository()
        val viewModel = newViewModel(budgetRepository = budgetRepository)
        val events = mutableListOf<BudgetSetupEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(BudgetSetupAction.SaveClicked)
        advanceUntilIdle()

        assertTrue(budgetRepository.snapshot().isEmpty())
        assertFalse(viewModel.uiState.value.validationResult.isValid)
        assertEquals(BudgetSetupEvent.ShowSnackbar("Fix validation errors before saving."), events.single())
        eventJob.cancel()
    }

    @Test
    fun validSaveCallsRepositoryAndEmitsEvent() = runTest {
        val budgetRepository = FakeBudgetRepository()
        val viewModel = newViewModel(
            budgetRepository = budgetRepository,
            idGenerator = { "generated-budget" },
        )
        val events = mutableListOf<BudgetSetupEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(BudgetSetupAction.NameChanged("  Food budget  "))
        viewModel.onAction(BudgetSetupAction.AmountChanged("125.50"))
        viewModel.onAction(BudgetSetupAction.CurrencyChanged(" cad "))
        viewModel.onAction(BudgetSetupAction.CategorySelected("food"))
        viewModel.onAction(BudgetSetupAction.SaveClicked)
        advanceUntilIdle()

        val saved = budgetRepository.getById("generated-budget")
        assertNotNull(saved)
        assertEquals("Food budget", saved?.name)
        assertEquals(12_550L, saved?.amountMinor)
        assertEquals("CAD", saved?.currencyCode)
        assertEquals("monthly", saved?.periodType)
        assertEquals("food", saved?.categoryId)
        assertEquals(CURRENT_TIME, saved?.createdAt)
        assertEquals(CURRENT_TIME, saved?.updatedAt)
        assertEquals(BudgetSetupEvent.SaveCompleted, events.single())
        assertFalse(viewModel.uiState.value.isSaving)
        eventJob.cancel()
    }

    @Test
    fun editModeLoadsBudget() = runTest {
        val budgetRepository = FakeBudgetRepository(
            initialBudgets = listOf(
                testLedgerBudget(
                    id = "budget-1",
                    name = "Groceries",
                    amountMinor = 25_000,
                    currencyCode = "USD",
                    categoryId = "food",
                    isActive = false,
                ),
            ),
        )

        val viewModel = newViewModel(
            budgetRepository = budgetRepository,
            mode = BudgetSetupMode.EDIT,
            budgetId = "budget-1",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(BudgetSetupMode.EDIT, state.formState.mode)
        assertEquals("budget-1", state.formState.budgetId)
        assertEquals("Groceries", state.formState.nameInput)
        assertEquals("250", state.formState.amountInput)
        assertEquals("food", state.formState.categoryId)
        assertFalse(state.formState.isActive)
    }

    @Test
    fun missingEditBudgetStopsLoadingAndEmitsError() = runTest {
        val viewModel = newViewModel(
            mode = BudgetSetupMode.EDIT,
            budgetId = "missing",
        )
        val events = mutableListOf<BudgetSetupEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            BudgetSetupEvent.ShowSnackbar("Budget was not found."),
            events.single(),
        )
        eventJob.cancel()
    }

    @Test
    fun savedStateHandleRestoresDraftState() = runTest {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = newViewModel(savedStateHandle = savedStateHandle)

        firstViewModel.onAction(BudgetSetupAction.NameChanged("Draft budget"))
        firstViewModel.onAction(BudgetSetupAction.AmountChanged("75.25"))
        firstViewModel.onAction(BudgetSetupAction.CurrencyChanged("CAD"))
        firstViewModel.onAction(BudgetSetupAction.CategorySelected("food"))
        firstViewModel.onAction(BudgetSetupAction.ActiveChanged(false))
        advanceUntilIdle()

        val restoredViewModel = newViewModel(savedStateHandle = savedStateHandle)
        advanceUntilIdle()

        val state = restoredViewModel.uiState.value.formState
        assertEquals("Draft budget", state.nameInput)
        assertEquals("75.25", state.amountInput)
        assertEquals("CAD", state.currencyCode)
        assertEquals("food", state.categoryId)
        assertFalse(state.isActive)
    }

    @Test
    fun editSavePreservesCreatedAtAndUpdatesBudget() = runTest {
        val budgetRepository = FakeBudgetRepository(
            initialBudgets = listOf(
                testLedgerBudget(
                    id = "budget-1",
                    name = "Old",
                    amountMinor = 10_000,
                    createdAt = 10L,
                    updatedAt = 20L,
                ),
            ),
        )
        val viewModel = newViewModel(
            budgetRepository = budgetRepository,
            mode = BudgetSetupMode.EDIT,
            budgetId = "budget-1",
        )
        advanceUntilIdle()

        viewModel.onAction(BudgetSetupAction.NameChanged("Updated"))
        viewModel.onAction(BudgetSetupAction.AmountChanged("99.99"))
        viewModel.onAction(BudgetSetupAction.SaveClicked)
        advanceUntilIdle()

        val saved = budgetRepository.getById("budget-1")
        assertEquals("Updated", saved?.name)
        assertEquals(9_999L, saved?.amountMinor)
        assertEquals(10L, saved?.createdAt)
        assertEquals(CURRENT_TIME, saved?.updatedAt)
    }

    @Test
    fun saveFailureEmitsErrorAndResetsSaving() = runTest {
        val budgetRepository = FailingBudgetRepository()
        val viewModel = newViewModel(budgetRepository = budgetRepository)
        val events = mutableListOf<BudgetSetupEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(BudgetSetupAction.NameChanged("Food"))
        viewModel.onAction(BudgetSetupAction.AmountChanged("10.00"))
        viewModel.onAction(BudgetSetupAction.SaveClicked)
        advanceUntilIdle()

        assertEquals(1, budgetRepository.upsertCalls)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(BudgetSetupEvent.ShowSnackbar("Save failed"), events.single())
        eventJob.cancel()
    }

    private fun newViewModel(
        budgetRepository: BudgetRepository = FakeBudgetRepository(),
        categoryRepository: FakeCategoryRepository = defaultCategoryRepository(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        mode: BudgetSetupMode = BudgetSetupMode.CREATE,
        budgetId: String? = null,
        idGenerator: () -> String = { "generated-budget" },
    ): BudgetSetupViewModel = BudgetSetupViewModel(
        budgetRepository = budgetRepository,
        categoryRepository = categoryRepository,
        savedStateHandle = savedStateHandle,
        initialMode = mode,
        initialBudgetId = budgetId,
        currentTimeMillis = { CURRENT_TIME },
        zoneId = ZoneOffset.UTC,
        idGenerator = idGenerator,
    )

    private class FailingBudgetRepository : BudgetRepository {
        var upsertCalls = 0

        override val repositoryName: String = "failing-budget"

        override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

        override suspend fun insert(budget: LedgerBudget) = upsert(budget)

        override suspend fun insertAll(budgets: List<LedgerBudget>) = Unit

        override suspend fun upsert(budget: LedgerBudget) {
            upsertCalls += 1
            error("Save failed")
        }

        override suspend fun upsertAll(budgets: List<LedgerBudget>) = Unit

        override suspend fun update(budget: LedgerBudget) = upsert(budget)

        override suspend fun delete(budget: LedgerBudget) = Unit

        override suspend fun deleteById(id: String): Boolean = false

        override suspend fun getById(id: String): LedgerBudget? = null

        override fun observeById(id: String): Flow<LedgerBudget?> = flowOf(null)

        override fun observeBudgets(): Flow<List<LedgerBudget>> = flowOf(emptyList())

        override fun observeActiveBudgets(): Flow<List<LedgerBudget>> = flowOf(emptyList())

        override fun observeBudgetsByCategory(categoryId: String): Flow<List<LedgerBudget>> = flowOf(emptyList())

        override fun observeBudgetsByPeriodRange(
            startInclusive: Long,
            endInclusive: Long,
        ): Flow<List<LedgerBudget>> = flowOf(emptyList())
    }

    private companion object {
        const val CURRENT_TIME = TestClock.November16

        fun defaultCategoryRepository(): FakeCategoryRepository = FakeCategoryRepository(
            listOf(
                testLedgerCategory(id = "food", name = "Food", type = "expense", sortOrder = 10),
                testLedgerCategory(id = "dining", name = "Dining", type = "expense", sortOrder = 5),
            ),
        )
    }
}
