package com.mojtaba.folentra.feature.dashboard.budget

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme

@Preview(showBackground = true)
@Composable
private fun BudgetSetupScreenPreview() {
    FolentraTheme(dynamicColor = false) {
        BudgetSetupScreen(
            uiState = BudgetSetupUiState(
                formState = BudgetSetupState(
                    nameInput = "Food budget",
                    amountInput = "500",
                    periodStart = 1_698_796_800_000L,
                    periodEnd = 1_701_388_799_999L,
                ),
                validationResult = BudgetSetupValidation.validate(
                    BudgetSetupState(
                        nameInput = "Food budget",
                        amountInput = "500",
                        periodStart = 1_698_796_800_000L,
                        periodEnd = 1_701_388_799_999L,
                    ),
                ),
                categories = listOf(
                    BudgetCategoryOption("groceries", "Groceries", "expense"),
                    BudgetCategoryOption("dining", "Dining", "expense"),
                ),
            ),
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onNavigateBack = {},
        )
    }
}
