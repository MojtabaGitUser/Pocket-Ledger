package com.mojtaba.pocketledger.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.mojtaba.pocketledger.core.security.applock.AppLockManager
import com.mojtaba.pocketledger.core.security.applock.AppLockMessage
import com.mojtaba.pocketledger.core.security.applock.AppLockState
import com.mojtaba.pocketledger.core.security.applock.AppLockStatus
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import kotlinx.coroutines.launch

@Composable
fun AppLockGate(
    appLockManager: AppLockManager,
    modifier: Modifier = Modifier,
    protectedContent: @Composable () -> Unit,
) {
    val state by appLockManager.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(appLockManager) {
        appLockManager.initialize()
    }
    LaunchedEffect(state.lockedChallengeId) {
        if (state.status == AppLockStatus.Locked) {
            appLockManager.unlock()
        }
    }

    if (state.isContentVisible) {
        protectedContent()
    } else {
        AppLockScreen(
            state = state,
            onUnlock = {
                scope.launch {
                    appLockManager.unlock()
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
internal fun AppLockScreen(
    state: AppLockState,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Pocket Ledger is locked",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(spacing.small))
        Text(
            text = state.message.lockMessage(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(spacing.large))
        if (state.status == AppLockStatus.Authenticating || state.status == AppLockStatus.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onUnlock,
                enabled = state.status == AppLockStatus.Locked,
            ) {
                Text(text = "Unlock")
            }
        }
    }
}

private fun AppLockMessage?.lockMessage(): String =
    when (this) {
        AppLockMessage.AuthenticationCancelled -> "Authentication was cancelled. Unlock to continue."
        AppLockMessage.AuthenticationFailed -> "Authentication failed. Unlock to continue."
        AppLockMessage.AuthenticationError -> "Authentication is unavailable right now."
        AppLockMessage.AppLockUnavailable -> "System authentication is unavailable on this device."
        null -> "Unlock to view your ledger."
    }
