package com.mojtaba.folentra.feature.dashboard.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mojtaba.folentra.core.designsystem.accessibility.folentraCheckedState
import com.mojtaba.folentra.core.designsystem.accessibility.folentraHeading
import com.mojtaba.folentra.core.designsystem.accessibility.folentraSelectedState
import com.mojtaba.folentra.core.designsystem.component.AdaptiveContainer
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSetupScreen(
    uiState: BudgetSetupUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (BudgetSetupAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (uiState.formState.mode) {
        BudgetSetupMode.CREATE -> "Set budget"
        BudgetSetupMode.EDIT -> "Edit budget"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.folentraHeading(),
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Close")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize(),
    ) { contentPadding ->
        AdaptiveContainer(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            maxWidth = 720.dp,
        ) {
            BudgetSetupContent(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun BudgetSetupContent(
    uiState: BudgetSetupUiState,
    onAction: (BudgetSetupAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        OutlinedTextField(
            value = uiState.formState.nameInput,
            onValueChange = { onAction(BudgetSetupAction.NameChanged(it)) },
            label = { Text("Budget name") },
            singleLine = true,
            isError = uiState.validationResult.errors.name != null,
            supportingText = {
                uiState.validationResult.errors.name?.message?.let { Text(it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Budget name"
                    uiState.validationResult.errors.name?.message?.let { error(it) }
                },
        )
        OutlinedTextField(
            value = uiState.formState.amountInput,
            onValueChange = { onAction(BudgetSetupAction.AmountChanged(it)) },
            label = { Text("Amount") },
            singleLine = true,
            prefix = { Text("$") },
            isError = uiState.validationResult.errors.amount != null,
            supportingText = {
                uiState.validationResult.errors.amount?.message?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Budget amount"
                    uiState.validationResult.errors.amount?.message?.let { error(it) }
                },
        )
        CategorySelector(
            categories = uiState.categories,
            selectedCategoryId = uiState.formState.categoryId,
            onCategorySelected = { onAction(BudgetSetupAction.CategorySelected(it)) },
            errorText = uiState.validationResult.errors.category?.message,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.formState.currencyCode,
            onValueChange = { onAction(BudgetSetupAction.CurrencyChanged(it)) },
            label = { Text("Currency") },
            singleLine = true,
            isError = uiState.validationResult.errors.currency != null,
            supportingText = {
                uiState.validationResult.errors.currency?.message?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Currency code"
                    uiState.validationResult.errors.currency?.message?.let { error(it) }
                },
        )
        PeriodSummary(
            periodStart = uiState.formState.periodStart,
            periodEnd = uiState.formState.periodEnd,
            errorText = uiState.validationResult.errors.period?.message,
        )
        ActiveToggle(
            isActive = uiState.formState.isActive,
            onActiveChanged = { onAction(BudgetSetupAction.ActiveChanged(it)) },
        )
        uiState.validationResult.errors.form?.message?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { error(message) },
            )
        }
        Spacer(Modifier.height(spacing.small))
        Button(
            onClick = { onAction(BudgetSetupAction.SaveClicked) },
            enabled = uiState.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp)
                .semantics {
                    contentDescription = "Save budget"
                    stateDescription = if (uiState.canSave) "Enabled" else "Disabled until required fields are valid"
                },
        ) {
            Text(if (uiState.isSaving) "Saving" else "Save")
        }
    }
}

@Composable
private fun CategorySelector(
    categories: List<BudgetCategoryOption>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    errorText: String?,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        modifier = modifier,
    ) {
        Text("Category")
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.small),
            modifier = Modifier.fillMaxWidth(),
        ) {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Overall") },
                modifier = Modifier.semantics {
                    contentDescription = "Category Overall"
                    this.selected = selectedCategoryId == null
                }.folentraSelectedState(selectedCategoryId == null),
            )
            categories.forEach { category ->
                val selected = category.id == selectedCategoryId
                FilterChip(
                    selected = selected,
                    onClick = { onCategorySelected(if (selected) null else category.id) },
                    label = { Text(text = category.name) },
                    modifier = Modifier.semantics {
                        contentDescription = "Category ${category.name}"
                        this.selected = selected
                    }.folentraSelectedState(selected),
                )
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { error(errorText) },
            )
        }
    }
}

@Composable
private fun PeriodSummary(
    periodStart: Long?,
    periodEnd: Long?,
    errorText: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Budget period"
                stateDescription = formatPeriod(periodStart, periodEnd)
                errorText?.let { error(it) }
            },
        verticalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.extraSmall),
    ) {
        Text(
            text = "Period",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = formatPeriod(periodStart, periodEnd),
            style = MaterialTheme.typography.bodyLarge,
            color = if (errorText == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { error(errorText) },
            )
        }
    }
}

@Composable
private fun ActiveToggle(
    isActive: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val highFontScale = LocalDensity.current.fontScale >= 2f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Active budget" }
            .folentraCheckedState(isActive),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FolentraThemeDefaults.spacing.extraSmall)) {
            Text(
                text = "Active",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Include this budget in dashboard progress",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!highFontScale) {
            Switch(
                checked = isActive,
                onCheckedChange = onActiveChanged,
                modifier = Modifier
                    .semantics { contentDescription = "Active budget switch" }
                    .folentraCheckedState(isActive),
            )
        }
    }
    if (highFontScale) {
        Switch(
            checked = isActive,
            onCheckedChange = onActiveChanged,
            modifier = modifier
                .semantics { contentDescription = "Active budget switch" }
                .folentraCheckedState(isActive),
        )
    }
}

private fun formatPeriod(
    periodStart: Long?,
    periodEnd: Long?,
): String {
    if (periodStart == null || periodEnd == null) return "Monthly budget"
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    val zoneId = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(periodStart).atZone(zoneId).toLocalDate().format(formatter)
    val end = Instant.ofEpochMilli(periodEnd).atZone(zoneId).toLocalDate().format(formatter)
    return "$start - $end"
}

private val BudgetNameError.message: String
    get() = when (this) {
        BudgetNameError.REQUIRED -> "Budget name is required"
        BudgetNameError.TOO_LONG -> "Budget name must be 80 characters or fewer"
    }

private val BudgetAmountError.message: String
    get() = when (this) {
        BudgetAmountError.REQUIRED -> "Amount is required"
        BudgetAmountError.INVALID_FORMAT -> "Enter a valid amount"
        BudgetAmountError.MUST_BE_GREATER_THAN_ZERO -> "Amount must be greater than zero"
        BudgetAmountError.NEGATIVE_NOT_ALLOWED -> "Budget amount cannot be negative"
        BudgetAmountError.TOO_MANY_DECIMAL_PLACES -> "Use no more than 2 decimal places"
    }

private val BudgetCurrencyError.message: String
    get() = when (this) {
        BudgetCurrencyError.REQUIRED -> "Currency is required"
        BudgetCurrencyError.INVALID_CODE -> "Use a 3-letter currency code"
    }

private val BudgetCategoryError.message: String
    get() = when (this) {
        BudgetCategoryError.BLANK -> "Choose a valid category"
    }

private val BudgetPeriodError.message: String
    get() = when (this) {
        BudgetPeriodError.START_REQUIRED -> "Period start is required"
        BudgetPeriodError.END_REQUIRED -> "Period end is required"
        BudgetPeriodError.INVALID_RANGE -> "Period start must be before period end"
    }

private val BudgetFormError.message: String
    get() = when (this) {
        BudgetFormError.EDIT_MODE_REQUIRES_ID -> "Edit mode requires a budget id"
    }
