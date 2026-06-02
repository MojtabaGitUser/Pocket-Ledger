package com.mojtaba.pocketledger.feature.transaction.form

data class TransactionFormErrors(
    val amount: AmountError? = null,
    val category: CategoryError? = null,
    val occurredAt: DateError? = null,
    val merchant: TextFieldError? = null,
    val note: TextFieldError? = null,
    val currencyCode: CurrencyError? = null,
    val form: FormError? = null,
) {
    val hasErrors: Boolean
        get() = amount != null ||
            category != null ||
            occurredAt != null ||
            merchant != null ||
            note != null ||
            currencyCode != null ||
            form != null
}

enum class AmountError {
    REQUIRED,
    INVALID_FORMAT,
    MUST_BE_GREATER_THAN_ZERO,
    NEGATIVE_NOT_ALLOWED,
    TOO_MANY_DECIMAL_PLACES,
}

enum class CategoryError {
    REQUIRED_FOR_EXPENSE,
}

enum class DateError {
    REQUIRED,
    INVALID_TIMESTAMP,
    IN_FUTURE,
}

enum class TextFieldError {
    TOO_LONG,
}

enum class CurrencyError {
    REQUIRED,
    INVALID_CODE,
}

enum class FormError {
    EDIT_MODE_REQUIRES_ID,
}
