package com.mojtaba.pocketledger.feature.dashboard.budget

data class BudgetSetupValidationResult(
    val errors: BudgetSetupErrors,
    val validatedInput: ValidatedBudgetInput?,
) {
    val isValid: Boolean = validatedInput != null && !errors.hasErrors

    val isSubmitReady: Boolean = isValid
}
