package com.mojtaba.pocketledger.feature.transaction.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionFormValidationTest {
    @Test
    fun validExpenseForm_returnsValidatedInput() {
        val result = validate(
            validState(
                amountInput = "12.34",
                transactionType = TransactionType.EXPENSE,
                categoryId = "category-food",
                merchant = "  Coffee Shop  ",
                note = "  Latte  ",
                currencyCode = "usd",
                isRecurring = true,
            ),
        )

        assertTrue(result.isValid)
        assertTrue(result.isSubmitReady)
        assertEquals(1_234L, result.validatedInput?.amountMinor)
        assertEquals(TransactionType.EXPENSE, result.validatedInput?.type)
        assertEquals("category-food", result.validatedInput?.categoryId)
        assertEquals("Coffee Shop", result.validatedInput?.merchant)
        assertEquals("Latte", result.validatedInput?.note)
        assertEquals("USD", result.validatedInput?.currencyCode)
        assertEquals(true, result.validatedInput?.isRecurring)
    }

    @Test
    fun validIncomeForm_allowsMissingCategory() {
        val result = validate(
            validState(
                amountInput = "100",
                transactionType = TransactionType.INCOME,
                categoryId = null,
            ),
        )

        assertTrue(result.isValid)
        assertEquals(10_000L, result.validatedInput?.amountMinor)
        assertNull(result.validatedInput?.categoryId)
    }

    @Test
    fun amountRequired_returnsAmountError() {
        val result = validate(validState(amountInput = " "))

        assertFalse(result.isValid)
        assertEquals(AmountError.REQUIRED, result.errors.amount)
        assertNull(result.validatedInput)
    }

    @Test
    fun invalidAmountText_returnsInvalidFormat() {
        val result = validate(validState(amountInput = "12.3a"))

        assertFalse(result.isValid)
        assertEquals(AmountError.INVALID_FORMAT, result.errors.amount)
    }

    @Test
    fun zeroAmount_returnsGreaterThanZeroError() {
        val result = validate(validState(amountInput = "0.00"))

        assertFalse(result.isValid)
        assertEquals(AmountError.MUST_BE_GREATER_THAN_ZERO, result.errors.amount)
    }

    @Test
    fun negativeAmount_returnsNegativeNotAllowed() {
        val result = validate(validState(amountInput = "-12.34"))

        assertFalse(result.isValid)
        assertEquals(AmountError.NEGATIVE_NOT_ALLOWED, result.errors.amount)
    }

    @Test
    fun amountWithTooManyDecimals_returnsDecimalPlacesError() {
        val result = validate(validState(amountInput = "12.345"))

        assertFalse(result.isValid)
        assertEquals(AmountError.TOO_MANY_DECIMAL_PLACES, result.errors.amount)
    }

    @Test
    fun amountConvertsToMinorUnits() {
        assertEquals(1L, validate(validState(amountInput = "0.01")).validatedInput?.amountMinor)
        assertEquals(120L, validate(validState(amountInput = "1.2")).validatedInput?.amountMinor)
        assertEquals(12_000L, validate(validState(amountInput = "120")).validatedInput?.amountMinor)
    }

    @Test
    fun expenseRequiresCategory() {
        val result = validate(
            validState(
                transactionType = TransactionType.EXPENSE,
                categoryId = " ",
            ),
        )

        assertFalse(result.isValid)
        assertEquals(CategoryError.REQUIRED_FOR_EXPENSE, result.errors.category)
    }

    @Test
    fun dateRequired_returnsDateError() {
        val result = validate(validState(occurredAt = null))

        assertFalse(result.isValid)
        assertEquals(DateError.REQUIRED, result.errors.occurredAt)
    }

    @Test
    fun invalidTimestamp_returnsDateError() {
        val result = validate(validState(occurredAt = 0L))

        assertFalse(result.isValid)
        assertEquals(DateError.INVALID_TIMESTAMP, result.errors.occurredAt)
    }

    @Test
    fun futureDate_returnsFutureDateError() {
        val result = validate(validState(occurredAt = CURRENT_TIME_MILLIS + 1L))

        assertFalse(result.isValid)
        assertEquals(DateError.IN_FUTURE, result.errors.occurredAt)
    }

    @Test
    fun merchantTrimmedAndBlankNormalizedToNull() {
        val trimmed = validate(validState(merchant = "  Store  "))
        val blank = validate(validState(merchant = "  "))

        assertEquals("Store", trimmed.validatedInput?.merchant)
        assertNull(blank.validatedInput?.merchant)
    }

    @Test
    fun merchantTooLong_returnsTextError() {
        val result = validate(validState(merchant = "a".repeat(TransactionFormValidation.MERCHANT_MAX_LENGTH + 1)))

        assertFalse(result.isValid)
        assertEquals(TextFieldError.TOO_LONG, result.errors.merchant)
    }

    @Test
    fun noteTrimmedAndBlankNormalizedToNull() {
        val trimmed = validate(validState(note = "  Memo  "))
        val blank = validate(validState(note = "  "))

        assertEquals("Memo", trimmed.validatedInput?.note)
        assertNull(blank.validatedInput?.note)
    }

    @Test
    fun noteTooLong_returnsTextError() {
        val result = validate(validState(note = "a".repeat(TransactionFormValidation.NOTE_MAX_LENGTH + 1)))

        assertFalse(result.isValid)
        assertEquals(TextFieldError.TOO_LONG, result.errors.note)
    }

    @Test
    fun invalidCurrencyCode_returnsCurrencyError() {
        val result = validate(validState(currencyCode = "US1"))

        assertFalse(result.isValid)
        assertEquals(CurrencyError.INVALID_CODE, result.errors.currencyCode)
    }

    @Test
    fun blankCurrencyCode_returnsRequiredError() {
        val result = validate(validState(currencyCode = " "))

        assertFalse(result.isValid)
        assertEquals(CurrencyError.REQUIRED, result.errors.currencyCode)
    }

    @Test
    fun editModeRequiresTransactionId() {
        val result = validate(
            validState(
                mode = TransactionFormMode.EDIT,
                transactionId = " ",
            ),
        )

        assertFalse(result.isValid)
        assertEquals(FormError.EDIT_MODE_REQUIRES_ID, result.errors.form)
    }

    @Test
    fun createModeDoesNotRequireTransactionId() {
        val result = validate(
            validState(
                mode = TransactionFormMode.CREATE,
                transactionId = null,
            ),
        )

        assertTrue(result.isValid)
        assertNull(result.validatedInput?.transactionId)
    }

    private fun validate(state: TransactionFormState): TransactionFormValidationResult =
        TransactionFormValidation.validate(
            state = state,
            currentTimeMillis = CURRENT_TIME_MILLIS,
        )

    private fun validState(
        mode: TransactionFormMode = TransactionFormMode.CREATE,
        transactionId: String? = null,
        amountInput: String = "12.34",
        transactionType: TransactionType = TransactionType.EXPENSE,
        categoryId: String? = "category-food",
        occurredAt: Long? = CURRENT_TIME_MILLIS,
        merchant: String = "",
        note: String = "",
        currencyCode: String = "USD",
        isRecurring: Boolean = false,
    ): TransactionFormState = TransactionFormState(
        mode = mode,
        transactionId = transactionId,
        amountInput = amountInput,
        transactionType = transactionType,
        categoryId = categoryId,
        occurredAt = occurredAt,
        merchant = merchant,
        note = note,
        currencyCode = currencyCode,
        isRecurring = isRecurring,
    )

    private companion object {
        const val CURRENT_TIME_MILLIS = 1_700_000_000_000L
    }
}
