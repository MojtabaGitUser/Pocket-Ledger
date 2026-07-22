package com.mojtaba.folentra.shared.domain.transaction

data class TransactionFormState(
    val mode: TransactionFormMode = TransactionFormMode.CREATE,
    val transactionId: String? = null,
    val amountInput: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val categoryId: String? = null,
    val occurredAt: Long? = null,
    val merchant: String = "",
    val note: String = "",
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
    val isRecurring: Boolean = false,
) {
    companion object {
        const val DEFAULT_CURRENCY_CODE = "USD"
    }
}

enum class TransactionFormMode {
    CREATE,
    EDIT,
}

enum class TransactionType {
    EXPENSE,
    INCOME,
}

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

data class ValidatedTransactionInput(
    val transactionId: String?,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: String?,
    val occurredAt: Long,
    val merchant: String?,
    val note: String?,
    val currencyCode: String,
    val isRecurring: Boolean,
)

data class TransactionFormValidationResult(
    val errors: TransactionFormErrors,
    val validatedInput: ValidatedTransactionInput?,
) {
    val isValid: Boolean
        get() = !errors.hasErrors && validatedInput != null

    val isSubmitReady: Boolean
        get() = isValid
}

object TransactionFormValidation {
    const val MERCHANT_MAX_LENGTH = 120
    const val NOTE_MAX_LENGTH = 500

    private val amountPattern = Regex("""^\d+(\.\d+)?$""")
    private val currencyCodePattern = Regex("""^[A-Z]{3}$""")

    fun validate(
        state: TransactionFormState,
        currentTimeMillis: Long,
    ): TransactionFormValidationResult {
        val normalizedTransactionId = state.transactionId?.trim()?.takeIf { it.isNotEmpty() }
        val amountResult = validateAmount(state.amountInput)
        val currencyResult = validateCurrencyCode(state.currencyCode)
        val categoryError = validateCategory(state.transactionType, state.categoryId)
        val dateError = validateOccurredAt(state.occurredAt, currentTimeMillis)
        val normalizedMerchant = state.merchant.trim().takeIf { it.isNotEmpty() }
        val normalizedNote = state.note.trim().takeIf { it.isNotEmpty() }
        val merchantError = validateText(normalizedMerchant, MERCHANT_MAX_LENGTH)
        val noteError = validateText(normalizedNote, NOTE_MAX_LENGTH)
        val formError = when {
            state.mode == TransactionFormMode.EDIT && normalizedTransactionId == null ->
                FormError.EDIT_MODE_REQUIRES_ID
            else -> null
        }

        val errors = TransactionFormErrors(
            amount = amountResult.error,
            category = categoryError,
            occurredAt = dateError,
            merchant = merchantError,
            note = noteError,
            currencyCode = currencyResult.error,
            form = formError,
        )

        val validatedInput = if (!errors.hasErrors) {
            ValidatedTransactionInput(
                transactionId = normalizedTransactionId,
                amountMinor = requireNotNull(amountResult.amountMinor),
                type = state.transactionType,
                categoryId = state.categoryId?.trim()?.takeIf { it.isNotEmpty() },
                occurredAt = requireNotNull(state.occurredAt),
                merchant = normalizedMerchant,
                note = normalizedNote,
                currencyCode = requireNotNull(currencyResult.currencyCode),
                isRecurring = state.isRecurring,
            )
        } else {
            null
        }

        return TransactionFormValidationResult(
            errors = errors,
            validatedInput = validatedInput,
        )
    }

    private fun validateAmount(amountInput: String): AmountValidation {
        val trimmed = amountInput.trim()
        if (trimmed.isEmpty()) return AmountValidation(error = AmountError.REQUIRED)
        if (trimmed.startsWith("-")) return AmountValidation(error = AmountError.NEGATIVE_NOT_ALLOWED)
        if (!amountPattern.matches(trimmed)) return AmountValidation(error = AmountError.INVALID_FORMAT)

        val parts = trimmed.split('.')
        val whole = parts[0].toLongOrNull() ?: return AmountValidation(error = AmountError.INVALID_FORMAT)
        val fraction = parts.getOrNull(1).orEmpty()
        if (fraction.length > 2) return AmountValidation(error = AmountError.TOO_MANY_DECIMAL_PLACES)
        val cents = fraction.padEnd(2, '0').ifEmpty { "0" }.toLongOrNull()
            ?: return AmountValidation(error = AmountError.INVALID_FORMAT)
        if (whole == 0L && cents == 0L) return AmountValidation(error = AmountError.MUST_BE_GREATER_THAN_ZERO)
        if (whole > (Long.MAX_VALUE - cents) / 100L) return AmountValidation(error = AmountError.INVALID_FORMAT)

        return AmountValidation(amountMinor = whole * 100L + cents)
    }

    private fun validateCurrencyCode(currencyCode: String): CurrencyValidation {
        val normalized = currencyCode.trim().uppercase()
        if (normalized.isEmpty()) return CurrencyValidation(error = CurrencyError.REQUIRED)
        if (!currencyCodePattern.matches(normalized)) return CurrencyValidation(error = CurrencyError.INVALID_CODE)
        return CurrencyValidation(currencyCode = normalized)
    }

    private fun validateCategory(transactionType: TransactionType, categoryId: String?): CategoryError? =
        if (transactionType == TransactionType.EXPENSE && categoryId.isNullOrBlank()) {
            CategoryError.REQUIRED_FOR_EXPENSE
        } else {
            null
        }

    private fun validateOccurredAt(occurredAt: Long?, currentTimeMillis: Long): DateError? = when {
        occurredAt == null -> DateError.REQUIRED
        occurredAt <= 0L -> DateError.INVALID_TIMESTAMP
        occurredAt > currentTimeMillis -> DateError.IN_FUTURE
        else -> null
    }

    private fun validateText(value: String?, maxLength: Int): TextFieldError? =
        if (value != null && value.length > maxLength) TextFieldError.TOO_LONG else null

    private data class AmountValidation(
        val amountMinor: Long? = null,
        val error: AmountError? = null,
    )

    private data class CurrencyValidation(
        val currencyCode: String? = null,
        val error: CurrencyError? = null,
    )
}
