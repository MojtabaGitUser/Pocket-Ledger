package com.mojtaba.folentra.feature.transaction.form

object TransactionFormValidation {
    const val MERCHANT_MAX_LENGTH = com.mojtaba.folentra.shared.domain.transaction.TransactionFormValidation.MERCHANT_MAX_LENGTH
    const val NOTE_MAX_LENGTH = com.mojtaba.folentra.shared.domain.transaction.TransactionFormValidation.NOTE_MAX_LENGTH

    fun validate(
        state: TransactionFormState,
        currentTimeMillis: Long,
    ): TransactionFormValidationResult =
        com.mojtaba.folentra.shared.domain.transaction.TransactionFormValidation.validate(
            state = state,
            currentTimeMillis = currentTimeMillis,
        )
}
