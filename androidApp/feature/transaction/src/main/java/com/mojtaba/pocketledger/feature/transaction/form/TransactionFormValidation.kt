package com.mojtaba.pocketledger.feature.transaction.form

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

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
        if (trimmed.isEmpty()) {
            return AmountValidation(error = AmountError.REQUIRED)
        }
        if (trimmed.startsWith("-")) {
            return AmountValidation(error = AmountError.NEGATIVE_NOT_ALLOWED)
        }
        if (!amountPattern.matches(trimmed)) {
            return AmountValidation(error = AmountError.INVALID_FORMAT)
        }

        val amount = trimmed.toBigDecimalOrNull()
            ?: return AmountValidation(error = AmountError.INVALID_FORMAT)

        if (amount.scale() > 2) {
            return AmountValidation(error = AmountError.TOO_MANY_DECIMAL_PLACES)
        }
        if (amount <= BigDecimal.ZERO) {
            return AmountValidation(error = AmountError.MUST_BE_GREATER_THAN_ZERO)
        }

        val minorUnits = runCatching {
            amount
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact()
        }.getOrElse {
            return AmountValidation(error = AmountError.INVALID_FORMAT)
        }

        return AmountValidation(amountMinor = minorUnits)
    }

    private fun validateCurrencyCode(currencyCode: String): CurrencyValidation {
        val normalized = currencyCode.trim().uppercase(Locale.US)
        if (normalized.isEmpty()) {
            return CurrencyValidation(error = CurrencyError.REQUIRED)
        }
        if (!currencyCodePattern.matches(normalized)) {
            return CurrencyValidation(error = CurrencyError.INVALID_CODE)
        }
        return CurrencyValidation(currencyCode = normalized)
    }

    private fun validateCategory(
        transactionType: TransactionType,
        categoryId: String?,
    ): CategoryError? =
        if (transactionType == TransactionType.EXPENSE && categoryId.isNullOrBlank()) {
            CategoryError.REQUIRED_FOR_EXPENSE
        } else {
            null
        }

    private fun validateOccurredAt(
        occurredAt: Long?,
        currentTimeMillis: Long,
    ): DateError? = when {
        occurredAt == null -> DateError.REQUIRED
        occurredAt <= 0L -> DateError.INVALID_TIMESTAMP
        occurredAt > currentTimeMillis -> DateError.IN_FUTURE
        else -> null
    }

    private fun validateText(value: String?, maxLength: Int): TextFieldError? =
        if (value != null && value.length > maxLength) {
            TextFieldError.TOO_LONG
        } else {
            null
        }

    private data class AmountValidation(
        val amountMinor: Long? = null,
        val error: AmountError? = null,
    )

    private data class CurrencyValidation(
        val currencyCode: String? = null,
        val error: CurrencyError? = null,
    )
}
