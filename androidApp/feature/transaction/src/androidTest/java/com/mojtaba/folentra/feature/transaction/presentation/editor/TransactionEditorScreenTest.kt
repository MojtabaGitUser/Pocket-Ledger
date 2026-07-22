package com.mojtaba.folentra.feature.transaction.presentation.editor

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme
import com.mojtaba.folentra.feature.transaction.form.TransactionFormMode
import com.mojtaba.folentra.feature.transaction.form.TransactionFormState
import com.mojtaba.folentra.feature.transaction.form.TransactionFormValidation
import com.mojtaba.folentra.feature.transaction.form.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TransactionEditorScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun amountEntryUpdatesState() {
        val harness = setEditorContent()

        composeRule.onNodeWithContentDescription("Transaction amount").performTextInput("12.34")

        assertEquals("12.34", harness.state.formState.amountInput)
    }

    @Test
    fun typeSwitchingUpdatesStateAndShowsIncomeCategories() {
        val harness = setEditorContent()

        composeRule.onNodeWithContentDescription("Transaction type Expense")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
        composeRule.onNodeWithText("Income").performClick()

        assertEquals(TransactionType.INCOME, harness.state.formState.transactionType)
        composeRule.onNodeWithText("Salary").assertExists()
        composeRule.onNodeWithContentDescription("Transaction type Income")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
    }

    @Test
    fun validationErrorsAreShown() {
        setEditorContent(
            initialFormState = TransactionFormState(
                amountInput = "abc",
                categoryId = null,
                occurredAt = CURRENT_TIME,
            ),
        )

        composeRule.onNodeWithText("Enter a valid amount").assertExists()
        composeRule.onNodeWithText("Expense requires a category").assertExists()
    }

    @Test
    fun saveButtonDisabledUntilFormIsValid() {
        val harness = setEditorContent()

        composeRule.onNodeWithText("Save").assertIsNotEnabled()

        composeRule.onNodeWithContentDescription("Transaction amount").performTextInput("25.00")
        composeRule.onNodeWithText("Food").performClick()

        composeRule.onNodeWithText("Save").assertIsEnabled()
        assertTrue(harness.state.canSave)
    }

    @Test
    fun createFlowEmitsSaveAction() {
        val harness = setEditorContent()

        composeRule.onNodeWithContentDescription("Transaction amount").performTextInput("25.00")
        composeRule.onNodeWithText("Food").performClick()
        composeRule.onNodeWithText("Work").performClick()
        composeRule.onNodeWithText("Save").performClick()

        assertTrue(harness.saveClicked)
        assertEquals(setOf("work"), harness.state.selectedTagIds)
    }

    @Test
    fun merchantNoteAndRecurringInputsUpdateState() {
        val harness = setEditorContent()

        composeRule.onNodeWithContentDescription("Merchant").performTextInput("Coffee Shop")
        composeRule.onNodeWithContentDescription("Note").performTextInput("Team breakfast")
        composeRule.onNodeWithContentDescription("Recurring transaction")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Off"))
        composeRule.onNodeWithContentDescription("Recurring transaction").performClick()

        assertEquals("Coffee Shop", harness.state.formState.merchant)
        assertEquals("Team breakfast", harness.state.formState.note)
        assertTrue(harness.state.formState.isRecurring)
        composeRule.onNodeWithContentDescription("Recurring transaction")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "On"))
    }

    @Test
    fun categoryAndTagChipsExposeSelectedStateDescriptions() {
        val harness = setEditorContent()

        composeRule.onNodeWithContentDescription("Category Food")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Not selected"))
            .performClick()
        composeRule.onNodeWithContentDescription("Tag Work")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Not selected"))
            .performClick()

        assertEquals("food", harness.state.formState.categoryId)
        assertEquals(setOf("work"), harness.state.selectedTagIds)
        composeRule.onNodeWithContentDescription("Category Food")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
        composeRule.onNodeWithContentDescription("Tag Work")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected"))
    }

    @Test
    fun dateSelectorDisplaysSelectedDate() {
        setEditorContent(
            initialFormState = TransactionFormState(
                amountInput = "12.00",
                categoryId = "food",
                occurredAt = CURRENT_TIME,
            ),
        )

        composeRule.onNodeWithText("Nov 14, 2023").assertExists()
    }

    @Test
    fun editFlowShowsPrefilledValues() {
        setEditorContent(
            initialFormState = TransactionFormState(
                mode = TransactionFormMode.EDIT,
                transactionId = "transaction-1",
                amountInput = "10.00",
                categoryId = "food",
                occurredAt = CURRENT_TIME,
                merchant = "Cafe",
                note = "Lunch",
            ),
        )

        composeRule.onNodeWithText("Edit Transaction").assertExists()
        composeRule.onNodeWithText("10.00").assertExists()
        composeRule.onNodeWithText("Cafe").assertExists()
        composeRule.onNodeWithText("Lunch").assertExists()
    }

    @Test
    fun screenDoesNotCrashWithEmptyCategoriesAndTags() {
        setEditorContent(
            categories = emptyList(),
            tags = emptyList(),
        )

        composeRule.onNodeWithText("Create Transaction").assertExists()
        composeRule.onNodeWithContentDescription("Transaction amount").assertExists()
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    private fun setEditorContent(
        initialFormState: TransactionFormState = TransactionFormState(occurredAt = CURRENT_TIME),
        categories: List<TransactionCategoryOption> = listOf(
            TransactionCategoryOption("food", "Food", "expense"),
            TransactionCategoryOption("salary", "Salary", "income"),
        ),
        tags: List<TransactionTagOption> = listOf(
            TransactionTagOption("work", "Work"),
            TransactionTagOption("weekend", "Weekend"),
        ),
    ): ScreenHarness {
        val harness = ScreenHarness(initialFormState, categories, tags)
        composeRule.setContent {
            FolentraTheme(dynamicColor = false) {
                TransactionEditorScreen(
                    uiState = harness.state,
                    snackbarHostState = SnackbarHostState(),
                    onAction = harness::onAction,
                    onNavigateBack = {},
                )
            }
        }
        return harness
    }

    private class ScreenHarness(
        initialFormState: TransactionFormState,
        categories: List<TransactionCategoryOption>,
        tags: List<TransactionTagOption>,
    ) {
        var saveClicked = false
        var state by mutableStateOf(
            TransactionEditorUiState(
                formState = initialFormState,
                validationResult = TransactionFormValidation.validate(initialFormState, CURRENT_TIME),
                categories = categories,
                tags = tags,
            ),
        )

        fun onAction(action: TransactionEditorAction) {
            when (action) {
                is TransactionEditorAction.AmountChanged -> updateForm { copy(amountInput = action.value) }
                is TransactionEditorAction.TypeChanged -> updateForm {
                    copy(
                        transactionType = action.value,
                        categoryId = if (action.value == TransactionType.INCOME) null else categoryId,
                    )
                }
                is TransactionEditorAction.CategoryChanged -> updateForm { copy(categoryId = action.categoryId) }
                is TransactionEditorAction.TagToggled -> state = state.copy(
                    selectedTagIds = state.selectedTagIds.toMutableSet().apply {
                        if (!add(action.tagId)) remove(action.tagId)
                    },
                )
                is TransactionEditorAction.MerchantChanged -> updateForm { copy(merchant = action.value) }
                is TransactionEditorAction.NoteChanged -> updateForm { copy(note = action.value) }
                is TransactionEditorAction.DateChanged -> updateForm { copy(occurredAt = action.value) }
                is TransactionEditorAction.CurrencyChanged -> updateForm { copy(currencyCode = action.value) }
                is TransactionEditorAction.RecurringChanged -> updateForm { copy(isRecurring = action.value) }
                TransactionEditorAction.SaveClicked -> saveClicked = true
            }
        }

        private fun updateForm(reducer: TransactionFormState.() -> TransactionFormState) {
            val formState = state.formState.reducer()
            state = state.copy(
                formState = formState,
                validationResult = TransactionFormValidation.validate(formState, CURRENT_TIME),
            )
        }
    }

    private companion object {
        const val CURRENT_TIME = 1_700_000_000_000L
    }
}
