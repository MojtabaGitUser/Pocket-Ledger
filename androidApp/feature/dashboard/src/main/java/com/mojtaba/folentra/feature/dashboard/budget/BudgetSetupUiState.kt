package com.mojtaba.folentra.feature.dashboard.budget

data class BudgetSetupUiState(
    val formState: BudgetSetupState = BudgetSetupState(),
    val validationResult: BudgetSetupValidationResult =
        BudgetSetupValidation.validate(BudgetSetupState()),
    val categories: List<BudgetCategoryOption> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = validationResult.isSubmitReady && !isSaving && !isLoading
}

data class BudgetCategoryOption(
    val id: String,
    val name: String,
    val type: String,
)
