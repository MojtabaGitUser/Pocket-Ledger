package com.mojtaba.pocketledger.feature.dashboard.budget

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BudgetSetupScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fieldsRender() {
        setBudgetContent()

        composeRule.onNodeWithText("Set budget").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Budget name").assertExists()
        composeRule.onNodeWithContentDescription("Budget amount").assertExists()
        composeRule.onNodeWithContentDescription("Currency code").assertExists()
        composeRule.onNodeWithContentDescription("Budget period").assertExists()
        composeRule.onNodeWithContentDescription("Active budget").assertExists()
    }

    @Test
    fun validationErrorsAreShown() {
        setBudgetContent(
            initialFormState = BudgetSetupState(
                nameInput = "",
                amountInput = "abc",
                periodStart = PERIOD_START,
                periodEnd = PERIOD_END,
            ),
        )

        composeRule.onNodeWithText("Budget name is required").assertExists()
        composeRule.onNodeWithText("Enter a valid amount").assertExists()
    }

    @Test
    fun saveButtonDisabledUntilValidAndThenEmitsSaveAction() {
        val harness = setBudgetContent()

        composeRule.onNodeWithText("Save").assertIsNotEnabled()

        composeRule.onNodeWithContentDescription("Budget name").performTextInput("Food")
        composeRule.onNodeWithContentDescription("Budget amount").performTextInput("25.00")

        composeRule.onNodeWithText("Save").assertIsEnabled()
        composeRule.onNodeWithText("Save").performClick()

        assertTrue(harness.saveClicked)
        assertTrue(harness.state.canSave)
    }

    @Test
    fun categoryAndActiveInputsUpdateState() {
        val harness = setBudgetContent()

        composeRule.onNodeWithText("Food").performClick()
        composeRule.onNodeWithContentDescription("Active budget switch").performClick()

        assertEquals("food", harness.state.formState.categoryId)
        assertFalse(harness.state.formState.isActive)
    }

    @Test
    fun createModeTitleShown() {
        setBudgetContent()

        composeRule.onNodeWithText("Set budget").assertExists()
    }

    @Test
    fun editModeTitleShown() {
        setBudgetContent(
            initialFormState = BudgetSetupState(
                mode = BudgetSetupMode.EDIT,
                budgetId = "budget-1",
                nameInput = "Food budget",
                amountInput = "10.00",
                periodStart = PERIOD_START,
                periodEnd = PERIOD_END,
            ),
        )

        composeRule.onNodeWithText("Edit budget").assertExists()
        composeRule.onNodeWithText("Food budget").assertExists()
        composeRule.onNodeWithText("10.00").assertExists()
    }

    @Test
    fun emptyCategoryListDoesNotCrash() {
        setBudgetContent(categories = emptyList())

        composeRule.onNodeWithText("Set budget").assertExists()
        composeRule.onNodeWithText("Overall").assertExists()
    }

    private fun setBudgetContent(
        initialFormState: BudgetSetupState = BudgetSetupState(
            periodStart = PERIOD_START,
            periodEnd = PERIOD_END,
        ),
        categories: List<BudgetCategoryOption> = listOf(
            BudgetCategoryOption("food", "Food", "expense"),
            BudgetCategoryOption("salary", "Salary", "income"),
        ),
    ): ScreenHarness {
        val harness = ScreenHarness(initialFormState, categories)
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                BudgetSetupScreen(
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
        initialFormState: BudgetSetupState,
        categories: List<BudgetCategoryOption>,
    ) {
        var saveClicked = false
        var state by mutableStateOf(
            BudgetSetupUiState(
                formState = initialFormState,
                validationResult = BudgetSetupValidation.validate(initialFormState),
                categories = categories,
            ),
        )

        fun onAction(action: BudgetSetupAction) {
            when (action) {
                is BudgetSetupAction.NameChanged -> updateForm { copy(nameInput = action.value) }
                is BudgetSetupAction.AmountChanged -> updateForm { copy(amountInput = action.value) }
                is BudgetSetupAction.CurrencyChanged -> updateForm { copy(currencyCode = action.value) }
                is BudgetSetupAction.CategorySelected -> updateForm { copy(categoryId = action.categoryId) }
                is BudgetSetupAction.PeriodChanged -> updateForm {
                    copy(periodStart = action.periodStart, periodEnd = action.periodEnd)
                }
                is BudgetSetupAction.ActiveChanged -> updateForm { copy(isActive = action.value) }
                BudgetSetupAction.SaveClicked -> saveClicked = true
            }
        }

        private fun updateForm(reducer: BudgetSetupState.() -> BudgetSetupState) {
            val formState = state.formState.reducer()
            state = state.copy(
                formState = formState,
                validationResult = BudgetSetupValidation.validate(formState),
            )
        }
    }

    private companion object {
        const val PERIOD_START = 1_698_796_800_000L
        const val PERIOD_END = 1_701_388_799_999L
    }
}
