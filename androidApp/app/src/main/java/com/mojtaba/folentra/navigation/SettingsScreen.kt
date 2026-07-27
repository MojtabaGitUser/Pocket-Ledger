package com.mojtaba.folentra.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mojtaba.folentra.background.BackgroundJobSettingsManager
import com.mojtaba.folentra.background.BackgroundJobSettingsState
import com.mojtaba.folentra.account.OptionalAccountSettingsState
import com.mojtaba.folentra.background.MonthlySummaryReminderTime
import com.mojtaba.folentra.core.designsystem.component.AdaptiveContainer
import com.mojtaba.folentra.core.designsystem.component.SectionHeader
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import com.mojtaba.folentra.core.security.applock.AppLockAvailability
import com.mojtaba.folentra.core.security.applock.AppLockManager
import com.mojtaba.folentra.core.security.applock.AppLockState
import com.mojtaba.folentra.core.security.applock.AppLockStatus
import com.mojtaba.folentra.core.security.applock.AppLockUnavailableReason
import com.mojtaba.folentra.core.security.backup.BackupReadyProfileManager
import com.mojtaba.folentra.core.security.backup.BackupReadyProfilePrerequisites
import com.mojtaba.folentra.core.security.backup.BackupReadyProfileState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    appLockManager: AppLockManager,
    modifier: Modifier = Modifier,
    backgroundJobSettingsManager: BackgroundJobSettingsManager? = null,
    backupReadyProfileManager: BackupReadyProfileManager? = null,
    optionalAccountSettingsState: OptionalAccountSettingsState = OptionalAccountSettingsState(
        featureEnabled = false,
        passkeyClientAvailable = false,
        playIntegrityHookAvailable = false,
    ),
) {
    val state by appLockManager.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var backgroundState by remember(backgroundJobSettingsManager) { mutableStateOf(BackgroundJobSettingsState()) }
    var backupReadyProfileState by remember(backupReadyProfileManager) {
        mutableStateOf(localOnlyBackupReadyProfileState())
    }
    LaunchedEffect(backgroundJobSettingsManager) {
        backgroundState = backgroundJobSettingsManager?.state() ?: BackgroundJobSettingsState()
    }
    LaunchedEffect(backupReadyProfileManager) {
        backupReadyProfileState = backupReadyProfileManager?.state() ?: localOnlyBackupReadyProfileState()
    }

    val spacing = FolentraThemeDefaults.spacing
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
            SecuritySettingsSection(
                state = state,
                canToggle = canToggle,
                appLockStateDescription = appLockStateDescription,
                onAppLockEnabledChange = { enabled ->
                    scope.launch { appLockManager.setAppLockEnabled(enabled) }
                },
                optionalAccountSettingsState = optionalAccountSettingsState,
                backupReadyProfileState = backupReadyProfileState,
                onBackupReadyProfileOptInChange = { accepted ->
                    backupReadyProfileManager?.let { manager ->
                        scope.launch {
                            backupReadyProfileState = manager.setOptInAccepted(accepted)
                        }
                    }
                },
            )
            backgroundJobSettingsManager?.let { manager ->
                BackgroundJobsSettingsSection(
                    state = backgroundState,
                    onEnabledChange = { enabled ->
                        scope.launch {
                            backgroundState = manager.setMonthlySummaryEnabled(enabled)
                        }
                    },
                    onTimeSelected = { time ->
                        scope.launch {
                            backgroundState = manager.setMonthlySummaryTime(time)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SecuritySettingsSection(
    state: AppLockState,
    canToggle: Boolean,
    appLockStateDescription: String,
    onAppLockEnabledChange: (Boolean) -> Unit,
    optionalAccountSettingsState: OptionalAccountSettingsState,
    backupReadyProfileState: BackupReadyProfileState,
    onBackupReadyProfileOptInChange: (Boolean) -> Unit,
) {
    val spacing = FolentraThemeDefaults.spacing
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
            headlineContent = { Text(text = "App lock") },
            supportingContent = { Text(text = state.availability.description()) },
            trailingContent = {
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = onAppLockEnabledChange,
                    enabled = canToggle,
                    modifier = Modifier.semantics {
                        contentDescription = "App lock switch"
                        stateDescription = appLockStateDescription
                        if (!canToggle) disabled()
                    },
                )
            },
        )
        ListItem(
            modifier = Modifier.semantics {
                contentDescription = "Optional account profile"
                stateDescription = optionalAccountSettingsState.stateDescription
                if (!optionalAccountSettingsState.controlsEnabled) disabled()
            },
            headlineContent = { Text(text = "Optional account profile") },
            supportingContent = { Text(text = optionalAccountSettingsState.supportingText) },
            trailingContent = {
                Text(
                    text = optionalAccountSettingsState.stateDescription,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (optionalAccountSettingsState.controlsEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            },
        )
        ListItem(
            modifier = Modifier.semantics {
                contentDescription = "Backup-ready profile"
                stateDescription = backupReadyProfileState.stateDescription
                if (!backupReadyProfileState.controlsEnabled) disabled()
            },
            headlineContent = { Text(text = "Backup-ready profile") },
            supportingContent = { Text(text = backupReadyProfileState.supportingText) },
            trailingContent = {
                Switch(
                    checked = backupReadyProfileState.optInAccepted,
                    onCheckedChange = onBackupReadyProfileOptInChange,
                    enabled = backupReadyProfileState.controlsEnabled,
                    modifier = Modifier.semantics {
                        contentDescription = "Backup-ready profile switch"
                        stateDescription = backupReadyProfileState.stateDescription
                        if (!backupReadyProfileState.controlsEnabled) disabled()
                    },
                )
            },
        )
    }
}

@Composable
private fun BackgroundJobsSettingsSection(
    state: BackgroundJobSettingsState,
    onEnabledChange: (Boolean) -> Unit,
    onTimeSelected: (MonthlySummaryReminderTime) -> Unit,
) {
    val spacing = FolentraThemeDefaults.spacing
    Column {
        Text(
            text = "Background jobs",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = spacing.medium)
                .semantics { heading() },
        )
        ListItem(
            modifier = Modifier.semantics {
                contentDescription = "Monthly summary preparation"
                stateDescription = if (state.monthlySummaryEnabled) "On" else "Off"
                if (!state.controlsEnabled) disabled()
            },
            headlineContent = { Text(text = "Monthly summary preparation") },
            supportingContent = { Text(text = state.monthlySummarySupportingText) },
            trailingContent = {
                Switch(
                    checked = state.monthlySummaryEnabled,
                    onCheckedChange = onEnabledChange,
                    enabled = state.controlsEnabled,
                    modifier = Modifier.semantics {
                        contentDescription = "Monthly summary preparation switch"
                        stateDescription = if (state.monthlySummaryEnabled) "On" else "Off"
                        if (!state.controlsEnabled) disabled()
                    },
                )
            },
        )
        Row(
            modifier = Modifier.padding(horizontal = spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            listOf(
                MonthlySummaryReminderTime(hour = 9, minute = 0),
                MonthlySummaryReminderTime(hour = 18, minute = 0),
            ).forEach { time ->
                TextButton(
                    onClick = { onTimeSelected(time) },
                    enabled = state.controlsEnabled && state.monthlySummaryEnabled,
                ) {
                    Text(text = time.displayLabel())
                }
            }
        }
        state.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = spacing.medium),
            )
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
private fun localOnlyBackupReadyProfileState(): BackupReadyProfileState = BackupReadyProfileState.from(
    optInAccepted = false,
    acceptedAtMillis = null,
    policyVersion = null,
    prerequisites = BackupReadyProfilePrerequisites(),
)
