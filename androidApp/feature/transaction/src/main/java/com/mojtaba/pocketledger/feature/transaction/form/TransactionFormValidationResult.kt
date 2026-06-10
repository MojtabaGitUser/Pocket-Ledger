package com.mojtaba.pocketledger.feature.transaction.form

data class TransactionFormValidationResult(
    val errors: TransactionFormErrors,
    val validatedInput: ValidatedTransactionInput?,
) {
    val isValid: Boolean = validatedInput != null && !errors.hasErrors

    val isSubmitReady: Boolean = isValid
}
