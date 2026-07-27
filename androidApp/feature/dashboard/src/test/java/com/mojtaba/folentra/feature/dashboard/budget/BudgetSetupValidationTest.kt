package com.mojtaba.folentra.feature.dashboard.budget

import com.mojtaba.folentra.core.testing.fixture.TestClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetSetupValidationTest {
    @Test
    fun validBudgetProducesValidatedInput() {
        val result = BudgetSetupValidation.validate(validState())

        assertTrue(result.isValid)
        val input = requireNotNull(result.validatedInput)
        assertEquals(null, input.id)
        assertEquals("Food budget", input.name)
        assertEquals(12_345L, input.amountMinor)
        assertEquals("USD", input.currencyCode)
        assertEquals("monthly", input.periodType)
        assertEquals(TestClock.NovemberPeriodStart, input.periodStart)
        assertEquals(TestClock.NovemberPeriodEnd, input.periodEnd)
        assertEquals("food", input.categoryId)
        assertTrue(input.isActive)
    }

    @Test
    fun nameRequired() {
        val result = BudgetSetupValidation.validate(validState(nameInput = " "))

        assertFalse(result.isValid)
        assertEquals(BudgetNameError.REQUIRED, result.errors.name)
    }

    @Test
    fun nameIsTrimmed() {
        val result = BudgetSetupValidation.validate(validState(nameInput = "  Food budget  "))

        assertEquals("Food budget", result.validatedInput?.name)
    }

    @Test
    fun nameTooLongRejected() {
        val result = BudgetSetupValidation.validate(validState(nameInput = "a".repeat(81)))

        assertEquals(BudgetNameError.TOO_LONG, result.errors.name)
    }

    @Test
    fun amountRequired() {
        val result = BudgetSetupValidation.validate(validState(amountInput = " "))

        assertEquals(BudgetAmountError.REQUIRED, result.errors.amount)
    }

    @Test
    fun invalidAmountRejected() {
        val result = BudgetSetupValidation.validate(validState(amountInput = "12.3.4"))

        assertEquals(BudgetAmountError.INVALID_FORMAT, result.errors.amount)
    }

    @Test
    fun zeroAndNegativeAmountsRejected() {
        assertEquals(
            BudgetAmountError.MUST_BE_GREATER_THAN_ZERO,
            BudgetSetupValidation.validate(validState(amountInput = "0")).errors.amount,
        )
        assertEquals(
            BudgetAmountError.NEGATIVE_NOT_ALLOWED,
            BudgetSetupValidation.validate(validState(amountInput = "-1")).errors.amount,
        )
    }

    @Test
    fun tooManyDecimalsRejected() {
        val result = BudgetSetupValidation.validate(validState(amountInput = "12.345"))

        assertEquals(BudgetAmountError.TOO_MANY_DECIMAL_PLACES, result.errors.amount)
    }

    @Test
    fun currencyInvalid() {
        assertEquals(
            BudgetCurrencyError.REQUIRED,
            BudgetSetupValidation.validate(validState(currencyCode = " ")).errors.currency,
        )
        assertEquals(
            BudgetCurrencyError.INVALID_CODE,
            BudgetSetupValidation.validate(validState(currencyCode = "US1")).errors.currency,
        )
    }

    @Test
    fun currencyIsNormalized() {
        val result = BudgetSetupValidation.validate(validState(currencyCode = " cad "))

        assertEquals("CAD", result.validatedInput?.currencyCode)
    }

    @Test
    fun periodRequired() {
        assertEquals(
            BudgetPeriodError.START_REQUIRED,
            BudgetSetupValidation.validate(validState(periodStart = null)).errors.period,
        )
        assertEquals(
            BudgetPeriodError.END_REQUIRED,
            BudgetSetupValidation.validate(validState(periodEnd = null)).errors.period,
        )
    }

    @Test
    fun invalidPeriodRangeRejected() {
        val result = BudgetSetupValidation.validate(
            validState(
                periodStart = TestClock.NovemberPeriodEnd,
                periodEnd = TestClock.NovemberPeriodStart,
            ),
        )

        assertEquals(BudgetPeriodError.INVALID_RANGE, result.errors.period)
    }

    @Test
    fun blankCategoryNormalizesToNull() {
        val result = BudgetSetupValidation.validate(validState(categoryId = " "))

        assertTrue(result.isValid)
        assertNull(result.validatedInput?.categoryId)
    }

    @Test
    fun createModeAcceptsNullId() {
        val result = BudgetSetupValidation.validate(validState(mode = BudgetSetupMode.CREATE, budgetId = null))

        assertTrue(result.isValid)
        assertNull(result.validatedInput?.id)
    }

    @Test
    fun editModeRequiresBudgetId() {
        val result = BudgetSetupValidation.validate(validState(mode = BudgetSetupMode.EDIT, budgetId = null))

        assertFalse(result.isValid)
        assertEquals(BudgetFormError.EDIT_MODE_REQUIRES_ID, result.errors.form)
    }

    private fun validState(
        mode: BudgetSetupMode = BudgetSetupMode.CREATE,
        budgetId: String? = null,
        nameInput: String = "Food budget",
        amountInput: String = "123.45",
        currencyCode: String = "USD",
        categoryId: String? = "food",
        periodStart: Long? = TestClock.NovemberPeriodStart,
        periodEnd: Long? = TestClock.NovemberPeriodEnd,
        isActive: Boolean = true,
    ): BudgetSetupState = BudgetSetupState(
        mode = mode,
        budgetId = budgetId,
        nameInput = nameInput,
        amountInput = amountInput,
        currencyCode = currencyCode,
        categoryId = categoryId,
        periodStart = periodStart,
        periodEnd = periodEnd,
        isActive = isActive,
    )
}
