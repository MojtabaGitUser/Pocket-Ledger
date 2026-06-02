package com.mojtaba.pocketledger.feature.transaction.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.designsystem.component.AdaptiveContainer
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.transaction.form.AmountError
import com.mojtaba.pocketledger.feature.transaction.form.CategoryError
import com.mojtaba.pocketledger.feature.transaction.form.CurrencyError
import com.mojtaba.pocketledger.feature.transaction.form.DateError
import com.mojtaba.pocketledger.feature.transaction.form.FormError
import com.mojtaba.pocketledger.feature.transaction.form.TextFieldError
import com.mojtaba.pocketledger.feature.transaction.form.TransactionFormMode
import com.mojtaba.pocketledger.feature.transaction.form.TransactionType
import com.mojtaba.pocketledger.feature.transaction.presentation.components.AmountField
import com.mojtaba.pocketledger.feature.transaction.presentation.components.CategorySelector
import com.mojtaba.pocketledger.feature.transaction.presentation.components.DateSelector
import com.mojtaba.pocketledger.feature.transaction.presentation.components.TagSelector
import com.mojtaba.pocketledger.feature.transaction.presentation.components.TransactionTypeSegmentedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorScreen(
    uiState: TransactionEditorUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (TransactionEditorAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (uiState.formState.mode) {
        TransactionFormMode.CREATE -> "Create Transaction"
        TransactionFormMode.EDIT -> "Edit Transaction"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            maxWidth = 1040.dp,
        ) {
            TransactionEditorContent(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun TransactionEditorContent(
    uiState: TransactionEditorUiState,
    onAction: (TransactionEditorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(vertical = spacing.medium),
    ) {
        val twoColumn = maxWidth >= 720.dp
        if (twoColumn) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.large),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PrimaryFields(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                )
                SecondaryFields(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.large),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PrimaryFields(uiState = uiState, onAction = onAction)
                SecondaryFields(uiState = uiState, onAction = onAction)
            }
        }
    }
}

@Composable
private fun PrimaryFields(
    uiState: TransactionEditorUiState,
    onAction: (TransactionEditorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
        modifier = modifier.fillMaxWidth(),
    ) {
        TransactionTypeSegmentedButton(
            selectedType = uiState.formState.transactionType,
            onTypeSelected = { onAction(TransactionEditorAction.TypeChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        AmountField(
            value = uiState.formState.amountInput,
            onValueChange = { onAction(TransactionEditorAction.AmountChanged(it)) },
            errorText = uiState.validationResult.errors.amount?.message,
            modifier = Modifier.fillMaxWidth(),
        )
        CategorySelector(
            categories = uiState.categories.filterFor(uiState.formState.transactionType),
            selectedCategoryId = uiState.formState.categoryId,
            onCategorySelected = { onAction(TransactionEditorAction.CategoryChanged(it)) },
            errorText = uiState.validationResult.errors.category?.message,
        )
        TagSelector(
            tags = uiState.tags,
            selectedTagIds = uiState.selectedTagIds,
            onTagToggled = { onAction(TransactionEditorAction.TagToggled(it)) },
        )
    }
}

@Composable
private fun SecondaryFields(
    uiState: TransactionEditorUiState,
    onAction: (TransactionEditorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
        modifier = modifier.fillMaxWidth(),
    ) {
        DateSelector(
            occurredAt = uiState.formState.occurredAt,
            onDateSelected = { onAction(TransactionEditorAction.DateChanged(it)) },
            errorText = uiState.validationResult.errors.occurredAt?.message,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.formState.currencyCode,
            onValueChange = { onAction(TransactionEditorAction.CurrencyChanged(it)) },
            label = { Text("Currency") },
            singleLine = true,
            isError = uiState.validationResult.errors.currencyCode != null,
            supportingText = {
                uiState.validationResult.errors.currencyCode?.message?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Currency code"
                    uiState.validationResult.errors.currencyCode?.message?.let { error(it) }
                },
        )
        OutlinedTextField(
            value = uiState.formState.merchant,
            onValueChange = { onAction(TransactionEditorAction.MerchantChanged(it)) },
            label = { Text("Merchant") },
            singleLine = true,
            isError = uiState.validationResult.errors.merchant != null,
            supportingText = {
                uiState.validationResult.errors.merchant?.message?.let { Text(it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Merchant"
                    uiState.validationResult.errors.merchant?.message?.let { error(it) }
                },
        )
        OutlinedTextField(
            value = uiState.formState.note,
            onValueChange = { onAction(TransactionEditorAction.NoteChanged(it)) },
            label = { Text("Note") },
            minLines = 3,
            maxLines = 5,
            isError = uiState.validationResult.errors.note != null,
            supportingText = {
                uiState.validationResult.errors.note?.message?.let { Text(it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Note"
                    uiState.validationResult.errors.note?.message?.let { error(it) }
                },
        )
        FilterChip(
            selected = uiState.formState.isRecurring,
            onClick = {
                onAction(TransactionEditorAction.RecurringChanged(!uiState.formState.isRecurring))
            },
            label = { Text("Recurring") },
            modifier = Modifier.semantics { contentDescription = "Recurring transaction" },
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
            onClick = { onAction(TransactionEditorAction.SaveClicked) },
            enabled = uiState.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp)
                .semantics { contentDescription = "Save transaction" },
        ) {
            Text(if (uiState.isSaving) "Saving" else "Save")
        }
        Spacer(Modifier.width(spacing.none))
    }
}

private fun List<TransactionCategoryOption>.filterFor(type: TransactionType): List<TransactionCategoryOption> =
    filter { category ->
        when (type) {
            TransactionType.EXPENSE -> category.type.equals("expense", ignoreCase = true)
            TransactionType.INCOME -> category.type.equals("income", ignoreCase = true)
        }
    }

private val AmountError.message: String
    get() = when (this) {
        AmountError.REQUIRED -> "Amount is required"
        AmountError.INVALID_FORMAT -> "Enter a valid amount"
        AmountError.MUST_BE_GREATER_THAN_ZERO -> "Amount must be greater than zero"
        AmountError.NEGATIVE_NOT_ALLOWED -> "Use transaction type instead of a negative amount"
        AmountError.TOO_MANY_DECIMAL_PLACES -> "Use no more than 2 decimal places"
    }

private val CategoryError.message: String
    get() = when (this) {
        CategoryError.REQUIRED_FOR_EXPENSE -> "Expense requires a category"
    }

private val DateError.message: String
    get() = when (this) {
        DateError.REQUIRED -> "Date is required"
        DateError.INVALID_TIMESTAMP -> "Enter a valid date"
        DateError.IN_FUTURE -> "Date cannot be in the future"
    }

private val CurrencyError.message: String
    get() = when (this) {
        CurrencyError.REQUIRED -> "Currency is required"
        CurrencyError.INVALID_CODE -> "Use a 3-letter currency code"
    }

private val TextFieldError.message: String
    get() = when (this) {
        TextFieldError.TOO_LONG -> "Text is too long"
    }

private val FormError.message: String
    get() = when (this) {
        FormError.EDIT_MODE_REQUIRES_ID -> "Edit mode requires a transaction id"
    }
