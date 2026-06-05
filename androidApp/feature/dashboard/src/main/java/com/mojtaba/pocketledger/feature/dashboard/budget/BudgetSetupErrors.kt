package com.mojtaba.pocketledger.feature.dashboard.budget

data class BudgetSetupErrors(
    val name: BudgetNameError? = null,
    val amount: BudgetAmountError? = null,
    val currency: BudgetCurrencyError? = null,
    val category: BudgetCategoryError? = null,
    val period: BudgetPeriodError? = null,
    val form: BudgetFormError? = null,
) {
    val hasErrors: Boolean
        get() = name != null ||
            amount != null ||
            currency != null ||
            category != null ||
            period != null ||
            form != null
}

enum class BudgetNameError {
    REQUIRED,
    TOO_LONG,
}

enum class BudgetAmountError {
    REQUIRED,
    INVALID_FORMAT,
    MUST_BE_GREATER_THAN_ZERO,
    NEGATIVE_NOT_ALLOWED,
    TOO_MANY_DECIMAL_PLACES,
}

enum class BudgetCurrencyError {
    REQUIRED,
    INVALID_CODE,
}

enum class BudgetCategoryError {
    BLANK,
}

enum class BudgetPeriodError {
    START_REQUIRED,
    END_REQUIRED,
    INVALID_RANGE,
}

enum class BudgetFormError {
    EDIT_MODE_REQUIRES_ID,
}
