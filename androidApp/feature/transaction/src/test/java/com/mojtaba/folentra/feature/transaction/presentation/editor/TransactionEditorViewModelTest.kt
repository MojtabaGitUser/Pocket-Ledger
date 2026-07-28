package com.mojtaba.folentra.feature.transaction.presentation.editor

import androidx.lifecycle.SavedStateHandle
import com.mojtaba.folentra.core.ai.AiFallbackStrategy
import com.mojtaba.folentra.core.ai.AiProviderSelector
import com.mojtaba.folentra.core.ai.NoOpAiProvider
import com.mojtaba.folentra.core.ai.RuleBasedAiProvider
import com.mojtaba.folentra.core.featureflags.DefaultFeatureFlags
import com.mojtaba.folentra.core.featureflags.FeatureFlagEvaluator
import com.mojtaba.folentra.core.featureflags.FeatureFlagValue
import com.mojtaba.folentra.core.featureflags.LocalFeatureFlagProvider
import com.mojtaba.folentra.core.testing.coroutine.MainDispatcherRule
import com.mojtaba.folentra.core.testing.fixture.TestClock
import com.mojtaba.folentra.core.testing.fixture.testLedgerCategory
import com.mojtaba.folentra.core.testing.fixture.testLedgerTag
import com.mojtaba.folentra.core.testing.fixture.testLedgerTransaction
import com.mojtaba.folentra.core.testing.fixture.testTransactionTagLink
import com.mojtaba.folentra.core.testing.repository.FakeCategoryRepository
import com.mojtaba.folentra.core.testing.repository.FakeTagRepository
import com.mojtaba.folentra.core.testing.repository.FakeTransactionRepository
import com.mojtaba.folentra.feature.transaction.form.AmountError
import com.mojtaba.folentra.feature.transaction.form.TransactionFormMode
import com.mojtaba.folentra.feature.transaction.form.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class TransactionEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialCreateStateUsesDefaultsAndLoadsOptions() = runTest {
        val viewModel = newViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(TransactionFormMode.CREATE, state.formState.mode)
        assertEquals(TransactionType.EXPENSE, state.formState.transactionType)
        assertEquals(CURRENT_TIME, state.formState.occurredAt)
        assertEquals(listOf("Food", "Salary"), state.categories.map { it.name })
        assertEquals(listOf("Weekend", "Work"), state.tags.map { it.name })
    }

    @Test
    fun changingFieldsUpdatesStateAndValidation() = runTest {
        val viewModel = newViewModel()

        viewModel.onAction(TransactionEditorAction.AmountChanged("abc"))
        advanceUntilIdle()

        assertEquals("abc", viewModel.uiState.value.formState.amountInput)
        assertEquals(AmountError.INVALID_FORMAT, viewModel.uiState.value.validationResult.errors.amount)

        viewModel.onAction(TransactionEditorAction.AmountChanged("12.34"))
        viewModel.onAction(TransactionEditorAction.CategoryChanged("food"))
        viewModel.onAction(TransactionEditorAction.DateChanged(TestClock.November15))
        viewModel.onAction(TransactionEditorAction.MerchantChanged(" Cafe "))
        viewModel.onAction(TransactionEditorAction.NoteChanged(" Lunch "))
        viewModel.onAction(TransactionEditorAction.RecurringChanged(true))
        viewModel.onAction(TransactionEditorAction.TagToggled("work"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.validationResult.isValid)
        assertEquals("food", state.formState.categoryId)
        assertEquals(TestClock.November15, state.formState.occurredAt)
        assertEquals(" Cafe ", state.formState.merchant)
        assertEquals(" Lunch ", state.formState.note)
        assertTrue(state.formState.isRecurring)
        assertEquals(setOf("work"), state.selectedTagIds)
    }

    @Test
    fun switchingToIncomeClearsExpenseCategory() = runTest {
        val viewModel = newViewModel()

        viewModel.onAction(TransactionEditorAction.CategoryChanged("food"))
        viewModel.onAction(TransactionEditorAction.TypeChanged(TransactionType.INCOME))
        advanceUntilIdle()

        assertEquals(TransactionType.INCOME, viewModel.uiState.value.formState.transactionType)
        assertEquals(null, viewModel.uiState.value.formState.categoryId)
    }

    @Test
    fun saveValidCreateWritesNormalizedTransactionTagsAndEvent() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val tagRepository = defaultTagRepository()
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            tagRepository = tagRepository,
            idGenerator = { "generated-id" },
        )
        val events = mutableListOf<TransactionEditorEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(TransactionEditorAction.AmountChanged("10.50"))
        viewModel.onAction(TransactionEditorAction.CategoryChanged("food"))
        viewModel.onAction(TransactionEditorAction.MerchantChanged("  Coffee Shop  "))
        viewModel.onAction(TransactionEditorAction.NoteChanged("  Team breakfast  "))
        viewModel.onAction(TransactionEditorAction.TagToggled("work"))
        viewModel.onAction(TransactionEditorAction.SaveClicked)
        advanceUntilIdle()

        val saved = transactionRepository.getById("generated-id")
        assertNotNull(saved)
        assertEquals(-1_050L, saved?.amountMinor)
        assertEquals("expense", saved?.type)
        assertEquals("food", saved?.categoryId)
        assertEquals("Coffee Shop", saved?.merchant)
        assertEquals("Team breakfast", saved?.note)
        assertEquals("manual", saved?.source)
        assertEquals(CURRENT_TIME, saved?.createdAt)
        assertEquals(CURRENT_TIME, saved?.updatedAt)
        assertEquals(setOf("work"), tagRepository.tagIdsForTransaction("generated-id"))
        assertEquals(TransactionEditorEvent.SaveCompleted, events.single())
        assertFalse(viewModel.uiState.value.isSaving)
        eventJob.cancel()
    }

    @Test
    fun saveValidIncomeWritesPositiveAmountAndNoCategory() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            idGenerator = { "income-id" },
        )

        viewModel.onAction(TransactionEditorAction.TypeChanged(TransactionType.INCOME))
        viewModel.onAction(TransactionEditorAction.AmountChanged("2500.00"))
        viewModel.onAction(TransactionEditorAction.SaveClicked)
        advanceUntilIdle()

        val saved = transactionRepository.getById("income-id")
        assertEquals(250_000L, saved?.amountMinor)
        assertEquals("income", saved?.type)
        assertEquals(null, saved?.categoryId)
    }

    @Test
    fun saveInvalidFormDoesNotWriteTransactionAndEmitsError() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val viewModel = newViewModel(transactionRepository = transactionRepository)
        val events = mutableListOf<TransactionEditorEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(TransactionEditorAction.AmountChanged(""))
        viewModel.onAction(TransactionEditorAction.SaveClicked)
        advanceUntilIdle()

        assertTrue(transactionRepository.snapshot().isEmpty())
        assertFalse(viewModel.uiState.value.validationResult.isValid)
        assertEquals(
            TransactionEditorEvent.ShowSnackbar("Fix validation errors before saving."),
            events.single(),
        )
        eventJob.cancel()
    }

    @Test
    fun saveFailureResetsSavingAndEmitsRepositoryError() = runTest {
        val transactionRepository = FakeTransactionRepository().apply {
            throwOnUpsert = true
        }
        val viewModel = newViewModel(transactionRepository = transactionRepository)
        val events = mutableListOf<TransactionEditorEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        viewModel.onAction(TransactionEditorAction.AmountChanged("10.00"))
        viewModel.onAction(TransactionEditorAction.CategoryChanged("food"))
        viewModel.onAction(TransactionEditorAction.SaveClicked)
        advanceUntilIdle()

        assertEquals(1, transactionRepository.upsertCalls)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(TransactionEditorEvent.ShowSnackbar("Save failed"), events.single())
        eventJob.cancel()
    }

    @Test
    fun editModeLoadsExistingTransactionAndTags() = runTest {
        val transactionRepository = FakeTransactionRepository(
            initialTransactions = listOf(
                testLedgerTransaction(
                    id = "transaction-1",
                    amountMinor = -1_250,
                    categoryId = "food",
                    merchant = "Cafe",
                    note = "Lunch",
                    occurredAt = TestClock.November14,
                    createdAt = 1L,
                    updatedAt = 2L,
                ),
            ),
        )
        val tagRepository = defaultTagRepository(
            initialLinks = listOf(testTransactionTagLink("transaction-1", "work")),
        )
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            tagRepository = tagRepository,
            mode = TransactionFormMode.EDIT,
            transactionId = "transaction-1",
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(TransactionFormMode.EDIT, state.formState.mode)
        assertEquals("transaction-1", state.formState.transactionId)
        assertEquals("12.5", state.formState.amountInput)
        assertEquals(TransactionType.EXPENSE, state.formState.transactionType)
        assertEquals("food", state.formState.categoryId)
        assertEquals(TestClock.November14, state.formState.occurredAt)
        assertEquals("Cafe", state.formState.merchant)
        assertEquals("Lunch", state.formState.note)
        assertEquals(setOf("work"), state.selectedTagIds)
    }

    @Test
    fun editSavePreservesCreatedAtUpdatesTransactionAndReconcilesTags() = runTest {
        val transactionRepository = FakeTransactionRepository(
            initialTransactions = listOf(
                testLedgerTransaction(
                    id = "transaction-1",
                    amountMinor = -1_250,
                    categoryId = "food",
                    merchant = "Cafe",
                    note = "Lunch",
                    createdAt = 10L,
                    updatedAt = 20L,
                ),
            ),
        )
        val tagRepository = defaultTagRepository(
            initialLinks = listOf(testTransactionTagLink("transaction-1", "work")),
        )
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            tagRepository = tagRepository,
            mode = TransactionFormMode.EDIT,
            transactionId = "transaction-1",
        )
        val events = mutableListOf<TransactionEditorEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }
        advanceUntilIdle()

        viewModel.onAction(TransactionEditorAction.AmountChanged("22.00"))
        viewModel.onAction(TransactionEditorAction.MerchantChanged("Market"))
        viewModel.onAction(TransactionEditorAction.NoteChanged("Groceries"))
        viewModel.onAction(TransactionEditorAction.TagToggled("work"))
        viewModel.onAction(TransactionEditorAction.TagToggled("weekend"))
        viewModel.onAction(TransactionEditorAction.SaveClicked)
        advanceUntilIdle()

        val saved = transactionRepository.getById("transaction-1")
        assertEquals(-2_200L, saved?.amountMinor)
        assertEquals("Market", saved?.merchant)
        assertEquals("Groceries", saved?.note)
        assertEquals(10L, saved?.createdAt)
        assertEquals(CURRENT_TIME, saved?.updatedAt)
        assertEquals(setOf("weekend"), tagRepository.tagIdsForTransaction("transaction-1"))
        assertEquals(TransactionEditorEvent.SaveCompleted, events.single())
        eventJob.cancel()
    }

    @Test
    fun missingEditTransactionStopsLoadingAndEmitsError() = runTest {
        val viewModel = newViewModel(
            mode = TransactionFormMode.EDIT,
            transactionId = "missing",
        )
        val events = mutableListOf<TransactionEditorEvent>()
        val eventJob = launch { viewModel.events.collect(events::add) }

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            TransactionEditorEvent.ShowSnackbar("Transaction was not found."),
            events.single(),
        )
        eventJob.cancel()
    }

    @Test
    fun savedStateHandleRestoresDraftStateAndTags() = runTest {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = newViewModel(savedStateHandle = savedStateHandle)

        firstViewModel.onAction(TransactionEditorAction.AmountChanged("45.67"))
        firstViewModel.onAction(TransactionEditorAction.CategoryChanged("food"))
        firstViewModel.onAction(TransactionEditorAction.MerchantChanged("Bakery"))
        firstViewModel.onAction(TransactionEditorAction.NoteChanged("Birthday cake"))
        firstViewModel.onAction(TransactionEditorAction.TagToggled("weekend"))
        advanceUntilIdle()

        val restoredViewModel = newViewModel(savedStateHandle = savedStateHandle)
        advanceUntilIdle()

        val state = restoredViewModel.uiState.value
        assertEquals("45.67", state.formState.amountInput)
        assertEquals("food", state.formState.categoryId)
        assertEquals("Bakery", state.formState.merchant)
        assertEquals("Birthday cake", state.formState.note)
        assertEquals(setOf("weekend"), state.selectedTagIds)
    }

    @Test
    fun smartAutofillUsesLocalFallbackAndAppliesSuggestion() = runTest {
        val transactionRepository = FakeTransactionRepository(
            listOf(
                testLedgerTransaction(
                    id = "history-1",
                    merchant = "Coffee House",
                    categoryId = "food",
                    amountMinor = -425,
                    isRecurring = true,
                ),
                testLedgerTransaction(
                    id = "history-2",
                    merchant = "Coffee House",
                    categoryId = "food",
                    amountMinor = -425,
                    isRecurring = true,
                ),
            ),
        )
        val viewModel = newViewModel(
            transactionRepository = transactionRepository,
            aiFallbackStrategy = localAiStrategy(),
        )
        advanceUntilIdle()

        viewModel.onAction(TransactionEditorAction.MerchantChanged("Coffee House"))
        viewModel.onAction(TransactionEditorAction.SmartAutofillClicked)
        advanceUntilIdle()

        val suggestion = viewModel.uiState.value.autofillSuggestion
        assertEquals("food", suggestion?.categoryId)
        assertEquals("4.25", suggestion?.amountInput)
        assertEquals(true, suggestion?.recurring)

        viewModel.onAction(TransactionEditorAction.SmartAutofillAccepted)
        assertEquals("food", viewModel.uiState.value.formState.categoryId)
        assertEquals("4.25", viewModel.uiState.value.formState.amountInput)
        assertTrue(viewModel.uiState.value.formState.isRecurring)
    }

    private fun newViewModel(
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
        categoryRepository: FakeCategoryRepository = defaultCategoryRepository(),
        tagRepository: FakeTagRepository = defaultTagRepository(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        mode: TransactionFormMode = TransactionFormMode.CREATE,
        transactionId: String? = null,
        idGenerator: () -> String = { "generated-id" },
        aiFallbackStrategy: AiFallbackStrategy? = null,
    ): TransactionEditorViewModel = TransactionEditorViewModel(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        tagRepository = tagRepository,
        savedStateHandle = savedStateHandle,
        initialMode = mode,
        initialTransactionId = transactionId,
        currentTimeMillis = { CURRENT_TIME },
        idGenerator = idGenerator,
        aiFallbackStrategy = aiFallbackStrategy,
    )

    private companion object {
        const val CURRENT_TIME = TestClock.November16

        fun defaultCategoryRepository(): FakeCategoryRepository = FakeCategoryRepository(
            listOf(
                testLedgerCategory(id = "food", name = "Food", type = "expense"),
                testLedgerCategory(id = "salary", name = "Salary", type = "income"),
            ),
        )

        fun defaultTagRepository(
            initialLinks: List<com.mojtaba.folentra.core.data.model.TransactionTagLink> = emptyList(),
        ): FakeTagRepository = FakeTagRepository(
            initialTags = listOf(
                testLedgerTag(id = "work", name = "Work"),
                testLedgerTag(id = "weekend", name = "Weekend"),
            ),
            initialLinks = initialLinks,
        )

        fun localAiStrategy(): AiFallbackStrategy {
            val evaluator = FeatureFlagEvaluator(
                LocalFeatureFlagProvider(
                    mapOf(
                        DefaultFeatureFlags.SmartAutofillEnabled.key to
                            FeatureFlagValue.BooleanValue(true),
                    ),
                ),
            )
            val selector = AiProviderSelector(
                providers = listOf(RuleBasedAiProvider, NoOpAiProvider),
                featureFlags = evaluator,
            )
            return AiFallbackStrategy(selector)
        }
    }
}
