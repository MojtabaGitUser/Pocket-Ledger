package com.mojtaba.folentra.shared.domain.transaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TransactionFormValidationTest {
    @Test
    fun validExpenseReturnsNormalizedInput() {
        val result = TransactionFormValidation.validate(
            TransactionFormState(
                amountInput = "12.30",
                categoryId = " groceries ",
                occurredAt = 1_000L,
                merchant = " Market ",
                note = " Weekly food ",
                currencyCode = " usd ",
            ),
            currentTimeMillis = 2_000L,
        )

        assertFalse(result.errors.hasErrors)
        val input = assertNotNull(result.validatedInput)
        assertEquals(1_230L, input.amountMinor)
        assertEquals("groceries", input.categoryId)
        assertEquals("Market", input.merchant)
        assertEquals("Weekly food", input.note)
        assertEquals("USD", input.currencyCode)
    }

    @Test
    fun invalidAmountAndFutureDateReturnErrors() {
        val result = TransactionFormValidation.validate(
            TransactionFormState(
                amountInput = "10.999",
                categoryId = "cat",
                occurredAt = 3_000L,
            ),
            currentTimeMillis = 2_000L,
        )

        assertTrue(result.errors.hasErrors)
        assertEquals(AmountError.TOO_MANY_DECIMAL_PLACES, result.errors.amount)
        assertEquals(DateError.IN_FUTURE, result.errors.occurredAt)
        assertNull(result.validatedInput)
    }
}