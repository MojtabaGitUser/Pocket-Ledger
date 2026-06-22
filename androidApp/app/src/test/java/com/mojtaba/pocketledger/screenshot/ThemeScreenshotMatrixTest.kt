package com.mojtaba.pocketledger.screenshot

import com.mojtaba.pocketledger.core.security.applock.AppLockAuthenticationResult
import com.mojtaba.pocketledger.core.security.applock.AppLockAuthenticator
import com.mojtaba.pocketledger.core.security.applock.AppLockAvailability
import com.mojtaba.pocketledger.core.security.applock.AppLockManager
import com.mojtaba.pocketledger.core.security.applock.AppLockUnavailableReason
import com.mojtaba.pocketledger.core.security.applock.AppLockState
import com.mojtaba.pocketledger.core.security.applock.AppLockStatus
import com.mojtaba.pocketledger.core.security.preferences.DefaultSensitivePreferenceKeys
import com.mojtaba.pocketledger.core.security.preferences.InMemorySensitivePreferences
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
class ThemeScreenshotMatrixTest(
    private val case: ThemeScreenshotCase,
) {
    @get:Rule
    val screenshotRule = AdaptiveScreenshotRule(
        device = case.device,
        theme = case.theme,
    )

    @Test
    fun dashboardContent() {
        screenshotRule.snapshotScreen("theme/dashboard", "content", includeThemeInName = true) {
            DashboardScreen(
                uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
                widthSizeClass = case.device.widthSizeClass,
                onAction = {},
            )
        }
    }

    @Test
    fun transactionAdaptiveContent() {
        screenshotRule.snapshotScreen("theme/transactions", "adaptive_content", includeThemeInName = true) {
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
    fun searchPopulatedResults() {
        screenshotRule.snapshotScreen("theme/search", "populated_results", includeThemeInName = true) {
            SearchScreen(
                uiState = ScreenshotTestData.populatedSearchState,
                onAction = {},
            )
        }
    }

    @Test
    fun settingsAppLockAvailable() {
        screenshotRule.snapshotScreen("theme/settings", "app_lock_available", includeThemeInName = true) {
            SettingsScreen(appLockManager = appLockManager(AppLockAvailability.Available))
        }
    }

    @Test
    fun settingsAppLockUnavailable() {
        screenshotRule.snapshotScreen("theme/settings", "app_lock_unavailable", includeThemeInName = true) {
            SettingsScreen(
                appLockManager = appLockManager(
                    AppLockAvailability.Unavailable(AppLockUnavailableReason.NoHardware),
                ),
            )
        }
    }

    @Test
    fun appLockLockedState() {
        screenshotRule.snapshotScreen("theme/security", "app_lock_locked", includeThemeInName = true) {
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

    private fun appLockManager(availability: AppLockAvailability): AppLockManager =
        runBlocking {
            val preferences = InMemorySensitivePreferences()
            preferences.putBoolean(DefaultSensitivePreferenceKeys.BiometricUnlockEnabled, availability is AppLockAvailability.Available)
            AppLockManager(
                preferences = preferences,
                authenticator = FakeAppLockAuthenticator(availability),
            ).also { it.initialize() }
        }

    private class FakeAppLockAuthenticator(
        private val availability: AppLockAvailability,
    ) : AppLockAuthenticator {
        override fun availability(): AppLockAvailability = availability

        override suspend fun authenticate(): AppLockAuthenticationResult = AppLockAuthenticationResult.Success
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): Collection<Array<Any>> =
            ScreenshotTheme.entries.flatMap { theme ->
                listOf(
                    ThemeScreenshotCase(theme, AdaptiveDeviceMatrix.CompactPhone),
                    ThemeScreenshotCase(theme, AdaptiveDeviceMatrix.ExpandedTablet),
                )
            }.map { arrayOf(it) }
    }
}

data class ThemeScreenshotCase(
    val theme: ScreenshotTheme,
    val device: AdaptiveScreenshotDevice,
) {
    override fun toString(): String = "${theme.id}_${device.id}"
}
