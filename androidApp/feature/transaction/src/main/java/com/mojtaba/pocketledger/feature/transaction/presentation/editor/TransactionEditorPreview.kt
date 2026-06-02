package com.mojtaba.pocketledger.feature.transaction.presentation.editor

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.feature.transaction.form.TransactionFormState
import com.mojtaba.pocketledger.feature.transaction.form.TransactionFormValidation
import com.mojtaba.pocketledger.feature.transaction.form.TransactionType

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TransactionEditorScreenPhonePreview() {
    PocketLedgerPreviewTheme {
        TransactionEditorScreen(
            uiState = previewState(),
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 900)
@Composable
private fun TransactionEditorScreenTabletPreview() {
    PocketLedgerPreviewTheme {
        TransactionEditorScreen(
            uiState = previewState(),
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

private fun previewState(): TransactionEditorUiState {
    val formState = TransactionFormState(
        amountInput = "42.50",
        transactionType = TransactionType.EXPENSE,
        categoryId = "food",
        occurredAt = 1_700_000_000_000,
        merchant = "Coffee Shop",
        note = "Team breakfast",
    )
    return TransactionEditorUiState(
        formState = formState,
        validationResult = TransactionFormValidation.validate(formState, 1_800_000_000_000),
        categories = listOf(
            TransactionCategoryOption("food", "Food", "expense"),
            TransactionCategoryOption("rent", "Rent", "expense"),
            TransactionCategoryOption("salary", "Salary", "income"),
        ),
        tags = listOf(
            TransactionTagOption("work", "Work"),
            TransactionTagOption("weekend", "Weekend"),
        ),
        selectedTagIds = setOf("work"),
    )
}
