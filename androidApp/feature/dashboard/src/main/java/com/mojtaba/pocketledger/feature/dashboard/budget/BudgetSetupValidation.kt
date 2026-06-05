package com.mojtaba.pocketledger.feature.dashboard.budget

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object BudgetSetupValidation {
    const val NAME_MAX_LENGTH = 80

    private val amountPattern = Regex("""^\d+(\.\d+)?$""")
    private val currencyCodePattern = Regex("""^[A-Z]{3}$""")

    fun validate(state: BudgetSetupState): BudgetSetupValidationResult {
        val normalizedBudgetId = state.budgetId?.trim()?.takeIf { it.isNotEmpty() }
        val nameResult = validateName(state.nameInput)
        val amountResult = validateAmount(state.amountInput)
        val currencyResult = validateCurrencyCode(state.currencyCode)
        val categoryResult = validateCategory(state.categoryId)
        val periodError = validatePeriod(state.periodStart, state.periodEnd)
        val formError = when {
            state.mode == BudgetSetupMode.EDIT && normalizedBudgetId == null ->
                BudgetFormError.EDIT_MODE_REQUIRES_ID
            else -> null
        }

        val errors = BudgetSetupErrors(
            name = nameResult.error,
            amount = amountResult.error,
            currency = currencyResult.error,
            category = categoryResult.error,
            period = periodError,
            form = formError,
        )

        val validatedInput = if (!errors.hasErrors) {
            ValidatedBudgetInput(
                id = normalizedBudgetId,
                name = requireNotNull(nameResult.name),
                amountMinor = requireNotNull(amountResult.amountMinor),
                currencyCode = requireNotNull(currencyResult.currencyCode),
                periodStart = requireNotNull(state.periodStart),
                periodEnd = requireNotNull(state.periodEnd),
                categoryId = categoryResult.categoryId,
                isActive = state.isActive,
            )
        } else {
            null
        }

        return BudgetSetupValidationResult(
            errors = errors,
            validatedInput = validatedInput,
        )
    }

    private fun validateName(nameInput: String): NameValidation {
        val trimmed = nameInput.trim()
        if (trimmed.isEmpty()) {
            return NameValidation(error = BudgetNameError.REQUIRED)
        }
        if (trimmed.length > NAME_MAX_LENGTH) {
            return NameValidation(error = BudgetNameError.TOO_LONG)
        }
        return NameValidation(name = trimmed)
    }

    private fun validateAmount(amountInput: String): AmountValidation {
        val trimmed = amountInput.trim()
        if (trimmed.isEmpty()) {
            return AmountValidation(error = BudgetAmountError.REQUIRED)
        }
        if (trimmed.startsWith("-")) {
            return AmountValidation(error = BudgetAmountError.NEGATIVE_NOT_ALLOWED)
        }
        if (!amountPattern.matches(trimmed)) {
            return AmountValidation(error = BudgetAmountError.INVALID_FORMAT)
        }

        val amount = trimmed.toBigDecimalOrNull()
            ?: return AmountValidation(error = BudgetAmountError.INVALID_FORMAT)

        if (amount.scale() > 2) {
            return AmountValidation(error = BudgetAmountError.TOO_MANY_DECIMAL_PLACES)
        }
        if (amount <= BigDecimal.ZERO) {
            return AmountValidation(error = BudgetAmountError.MUST_BE_GREATER_THAN_ZERO)
        }

        val minorUnits = runCatching {
            amount
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact()
        }.getOrElse {
            return AmountValidation(error = BudgetAmountError.INVALID_FORMAT)
        }

        return AmountValidation(amountMinor = minorUnits)
    }

    private fun validateCurrencyCode(currencyCode: String): CurrencyValidation {
        val normalized = currencyCode.trim().uppercase(Locale.US)
        if (normalized.isEmpty()) {
            return CurrencyValidation(error = BudgetCurrencyError.REQUIRED)
        }
        if (!currencyCodePattern.matches(normalized)) {
            return CurrencyValidation(error = BudgetCurrencyError.INVALID_CODE)
        }
        return CurrencyValidation(currencyCode = normalized)
    }

    private fun validateCategory(categoryId: String?): CategoryValidation {
        val normalized = categoryId?.trim()
        return when {
            categoryId == null -> CategoryValidation(categoryId = null)
            normalized.isNullOrEmpty() -> CategoryValidation(categoryId = null)
            else -> CategoryValidation(categoryId = normalized)
        }
    }

    private fun validatePeriod(
        periodStart: Long?,
        periodEnd: Long?,
    ): BudgetPeriodError? = when {
        periodStart == null -> BudgetPeriodError.START_REQUIRED
        periodEnd == null -> BudgetPeriodError.END_REQUIRED
        periodStart > periodEnd -> BudgetPeriodError.INVALID_RANGE
        else -> null
    }

    private data class NameValidation(
        val name: String? = null,
        val error: BudgetNameError? = null,
    )

    private data class AmountValidation(
        val amountMinor: Long? = null,
        val error: BudgetAmountError? = null,
    )

    private data class CurrencyValidation(
        val currencyCode: String? = null,
        val error: BudgetCurrencyError? = null,
    )

    private data class CategoryValidation(
        val categoryId: String?,
        val error: BudgetCategoryError? = null,
    )
}
