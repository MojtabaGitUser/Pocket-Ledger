package com.mojtaba.pocketledger.security

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerTheme
import com.mojtaba.pocketledger.core.security.applock.AppLockState
import com.mojtaba.pocketledger.core.security.applock.AppLockStatus
import org.junit.Rule
import org.junit.Test

class AppLockScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lockedStateExposesUnlockActionAndState() {
        setContent(AppLockState(status = AppLockStatus.Locked))

        composeRule.onNodeWithContentDescription("Pocket Ledger is locked", substring = true)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Locked"))
        composeRule.onNodeWithContentDescription("Unlock Pocket Ledger")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Enabled"))
    }

    @Test
    fun authenticatingStateExposesProgressState() {
        setContent(AppLockState(status = AppLockStatus.Authenticating))

        composeRule.onNodeWithContentDescription("App lock authentication")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Authenticating"))
    }

    private fun setContent(state: AppLockState) {
        composeRule.setContent {
            PocketLedgerTheme(dynamicColor = false) {
                AppLockScreen(
                    state = state,
                    onUnlock = {},
                )
            }
        }
    }
}
