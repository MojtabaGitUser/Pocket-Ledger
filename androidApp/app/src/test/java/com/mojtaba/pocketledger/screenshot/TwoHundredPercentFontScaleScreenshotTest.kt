package com.mojtaba.pocketledger.screenshot

import androidx.compose.material3.SnackbarHostState
import com.mojtaba.pocketledger.core.security.applock.AppLockAuthenticationResult
import com.mojtaba.pocketledger.core.security.applock.AppLockAuthenticator
import com.mojtaba.pocketledger.core.security.applock.AppLockAvailability
import com.mojtaba.pocketledger.core.security.applock.AppLockManager
import com.mojtaba.pocketledger.core.security.applock.AppLockState
import com.mojtaba.pocketledger.core.security.applock.AppLockStatus
import com.mojtaba.pocketledger.core.security.preferences.DefaultSensitivePreferenceKeys
import com.mojtaba.pocketledger.core.security.preferences.InMemorySensitivePreferences
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetCategoryOption
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetSetupScreen
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetSetupState
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetSetupUiState
import com.mojtaba.pocketledger.feature.dashboard.budget.BudgetSetupValidation
import com.mojtaba.pocketledger.feature.dashboard.presentation.DashboardScreen
import com.mojtaba.pocketledger.feature.dashboard.presentation.DashboardUiState
import com.mojtaba.pocketledger.feature.dashboard.presentation.preview.DashboardPreviewFixtures
import com.mojtaba.pocketledger.feature.search.presentation.SearchScreen
import com.mojtaba.pocketledger.feature.transaction.adaptive.TransactionListDetailContent
import com.mojtaba.pocketledger.feature.transaction.presentation.detail.TransactionDetailContent
import com.mojtaba.pocketledger.navigation.SettingsScreen
import com.mojtaba.pocketledger.security.AppLockScreen
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
