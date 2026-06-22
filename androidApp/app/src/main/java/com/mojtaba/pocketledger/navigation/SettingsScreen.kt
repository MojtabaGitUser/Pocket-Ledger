package com.mojtaba.pocketledger.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojtaba.pocketledger.core.designsystem.component.AdaptiveContainer
import com.mojtaba.pocketledger.core.designsystem.component.SectionHeader
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.core.security.applock.AppLockAvailability
import com.mojtaba.pocketledger.core.security.applock.AppLockManager
import com.mojtaba.pocketledger.core.security.applock.AppLockState
import com.mojtaba.pocketledger.core.security.applock.AppLockStatus
import com.mojtaba.pocketledger.core.security.applock.AppLockUnavailableReason
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    appLockManager: AppLockManager,
    modifier: Modifier = Modifier,
) {
    val state by appLockManager.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val spacing = PocketLedgerThemeDefaults.spacing
    val canToggle = state.canEnable && state.status != AppLockStatus.Authenticating && state.status != AppLockStatus.Loading
    val appLockStateDescription = state.appLockStateDescription(canToggle)

    AdaptiveContainer(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            SectionHeader(
                title = "Settings",
                subtitle = "Security and privacy",
            )
            Column {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = spacing.medium)
                        .semantics { heading() },
                )
                ListItem(
                    modifier = Modifier.semantics {
                        contentDescription = "App lock"
                        stateDescription = appLockStateDescription
                        if (!canToggle) disabled()
                    },
                    headlineContent = {
                        Text(text = "App lock")
                    },
                    supportingContent = {
                        Text(text = state.availability.description())
                    },
                    trailingContent = {
                        Switch(
                            checked = state.isEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    appLockManager.setAppLockEnabled(enabled)
                                }
                            },
                            enabled = canToggle,
                            modifier = Modifier.semantics {
                                contentDescription = "App lock switch"
                                stateDescription = appLockStateDescription
                                if (!canToggle) disabled()
                            },
                        )
                    },
                )
            }
        }
    }
}

private fun AppLockAvailability.description(): String =
    when (this) {
        AppLockAvailability.Available -> "Require system authentication when opening or returning to the app."
        is AppLockAvailability.Unavailable -> when (reason) {
            AppLockUnavailableReason.NoHardware -> "System authentication is not available on this device."
            AppLockUnavailableReason.NoneEnrolled -> "Set up biometrics or a device screen lock to enable app lock."
            AppLockUnavailableReason.DeviceCredentialUnavailable -> "Device credential fallback is unavailable."
            AppLockUnavailableReason.Unknown -> "System authentication is unavailable right now."
        }
    }

private fun AppLockState.appLockStateDescription(
    canToggle: Boolean,
): String =
    when {
        status == AppLockStatus.Loading -> "Loading"
        status == AppLockStatus.Authenticating -> "Authenticating"
        !canEnable -> "Unavailable"
        isEnabled && canToggle -> "On"
        !isEnabled && canToggle -> "Off"
        isEnabled -> "On, disabled"
        else -> "Off, disabled"
    }
