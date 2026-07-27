package com.mojtaba.folentra.screenshot

import androidx.compose.material3.SnackbarHostState
import com.mojtaba.folentra.core.security.applock.AppLockAuthenticationResult
import com.mojtaba.folentra.core.security.applock.AppLockAuthenticator
import com.mojtaba.folentra.core.security.applock.AppLockAvailability
import com.mojtaba.folentra.core.security.applock.AppLockManager
import com.mojtaba.folentra.core.security.applock.AppLockState
import com.mojtaba.folentra.core.security.applock.AppLockStatus
import com.mojtaba.folentra.core.security.preferences.DefaultSensitivePreferenceKeys
import com.mojtaba.folentra.core.security.preferences.InMemorySensitivePreferences
import com.mojtaba.folentra.feature.dashboard.budget.BudgetCategoryOption
import com.mojtaba.folentra.feature.dashboard.budget.BudgetSetupScreen
import com.mojtaba.folentra.feature.dashboard.budget.BudgetSetupState
import com.mojtaba.folentra.feature.dashboard.budget.BudgetSetupUiState
import com.mojtaba.folentra.feature.dashboard.budget.BudgetSetupValidation
import com.mojtaba.folentra.feature.dashboard.presentation.DashboardScreen
import com.mojtaba.folentra.feature.dashboard.presentation.DashboardUiState
import com.mojtaba.folentra.feature.dashboard.presentation.preview.DashboardPreviewFixtures
import com.mojtaba.folentra.feature.search.presentation.SearchScreen
import com.mojtaba.folentra.feature.transaction.form.TransactionFormState
import com.mojtaba.folentra.feature.transaction.form.TransactionFormValidation
import com.mojtaba.folentra.feature.transaction.presentation.editor.TransactionCategoryOption
import com.mojtaba.folentra.feature.transaction.presentation.editor.TransactionEditorScreen
import com.mojtaba.folentra.feature.transaction.presentation.editor.TransactionEditorUiState
import com.mojtaba.folentra.feature.transaction.presentation.editor.TransactionTagOption
import com.mojtaba.folentra.feature.transaction.adaptive.TransactionListDetailContent
import com.mojtaba.folentra.feature.transaction.presentation.detail.TransactionDetailContent
import com.mojtaba.folentra.navigation.SettingsScreen
import com.mojtaba.folentra.security.AppLockScreen
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TwoHundredPercentFontScaleScreenshotTest(
    private val device: AdaptiveScreenshotDevice,
) {
    @get:Rule
    val screenshotRule = AdaptiveScreenshotRule(device)

    @Test
    fun dashboardAtTwoHundredPercentFontScale() {
        screenshotRule.snapshotScreen("accessibility-200/dashboard", "content") {
            DashboardScreen(
                uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
                widthSizeClass = device.widthSizeClass,
                onAction = {},
            )
        }
    }

    @Test
    fun transactionsAtTwoHundredPercentFontScale() {
        screenshotRule.snapshotScreen("accessibility-200/transactions", "list_detail") {
            TransactionListDetailContent(
                listUiState = ScreenshotTestData.transactionListContent,
                selectedTransactionId = ScreenshotTestData.selectedTransactionId,
                onListAction = {},
            ) {
                TransactionDetailContent(
                    uiState = ScreenshotTestData.transactionDetail,
                    onAction = {},
                )
            }
        }
    }

    @Test
    fun searchAtTwoHundredPercentFontScale() {
        screenshotRule.snapshotScreen("accessibility-200/search", "populated_results") {
            SearchScreen(
                uiState = ScreenshotTestData.populatedSearchState,
                onAction = {},
            )
        }
    }

    @Test
    fun budgetSetupAtTwoHundredPercentFontScale() {
        val formState = BudgetSetupState(
            nameInput = "Food budget",
            amountInput = "450.00",
            categoryId = "food",
            periodStart = 1_698_796_800_000L,
            periodEnd = 1_701_388_799_999L,
        )

        screenshotRule.snapshotScreen("accessibility-200/budget", "setup") {
            BudgetSetupScreen(
                uiState = BudgetSetupUiState(
                    formState = formState,
                    validationResult = BudgetSetupValidation.validate(formState),
                    categories = listOf(
                        BudgetCategoryOption("food", "Food and dining", "expense"),
                        BudgetCategoryOption("transport", "Transportation", "expense"),
                        BudgetCategoryOption("utilities", "Utilities", "expense"),
                    ),
                ),
                snackbarHostState = SnackbarHostState(),
                onAction = {},
                onNavigateBack = {},
            )
        }
    }


    @Test
    fun transactionEditorAtTwoHundredPercentFontScale() {
        val categories = listOf(
            TransactionCategoryOption("food", "Food and dining", "expense"),
            TransactionCategoryOption("transport", "Transportation", "expense"),
        )
        val tags = listOf(
            TransactionTagOption("work", "Work"),
            TransactionTagOption("family", "Family"),
        )
        val validForm = TransactionFormState(
            amountInput = "86.42",
            categoryId = "food",
            occurredAt = 1_700_000_000_000L,
            merchant = "Neighborhood Market",
            note = "Weekly groceries",
        )
        screenshotRule.snapshotScreen("accessibility-200/transaction-editor", "valid") {
            TransactionEditorScreen(
                uiState = TransactionEditorUiState(
                    formState = validForm,
                    validationResult = TransactionFormValidation.validate(
                        validForm,
                        currentTimeMillis = 1_700_000_000_000L,
                    ),
                    categories = categories,
                    tags = tags,
                ),
                snackbarHostState = SnackbarHostState(),
                onAction = {},
                onNavigateBack = {},
            )
        }

        val invalidForm = validForm.copy(amountInput = "invalid", categoryId = null)
        screenshotRule.snapshotScreen("accessibility-200/transaction-editor", "validation_errors") {
            TransactionEditorScreen(
                uiState = TransactionEditorUiState(
                    formState = invalidForm,
                    validationResult = TransactionFormValidation.validate(
                        invalidForm,
                        currentTimeMillis = 1_700_000_000_000L,
                    ),
                    categories = categories,
                    tags = tags,
                ),
                snackbarHostState = SnackbarHostState(),
                onAction = {},
                onNavigateBack = {},
            )
        }
    }
    @Test
    fun settingsAndAppLockAtTwoHundredPercentFontScale() {
        screenshotRule.snapshotScreen("accessibility-200/settings", "app_lock_available") {
            SettingsScreen(appLockManager = appLockManager())
        }
        screenshotRule.snapshotScreen("accessibility-200/security", "app_lock_locked") {
            AppLockScreen(
                state = AppLockState(
                    status = AppLockStatus.Locked,
                    isEnabled = true,
                    availability = AppLockAvailability.Available,
                ),
                onUnlock = {},
            )
        }
    }

    private fun appLockManager(): AppLockManager =
        runBlocking {
            val preferences = InMemorySensitivePreferences()
            preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, true)
            AppLockManager(
                preferences = preferences,
                authenticator = AlwaysAvailableAuthenticator,
            ).also { it.initialize() }
        }

    private object AlwaysAvailableAuthenticator : AppLockAuthenticator {
        override fun availability(): AppLockAvailability = AppLockAvailability.Available

        override suspend fun authenticate(): AppLockAuthenticationResult = AppLockAuthenticationResult.Success
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun devices(): Collection<Array<Any>> =
            AdaptiveDeviceMatrix.TwoHundredPercentFontScaleDevices.map { arrayOf(it) }
    }
}
