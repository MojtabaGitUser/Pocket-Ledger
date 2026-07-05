package com.mojtaba.pocketledger.desktop.insights

import java.util.Locale
import kotlin.math.abs

class DesktopInsightsStateMapper(
    private val provider: DesktopInsightsProvider,
) {
    fun map(input: DesktopMonthlySummaryInput): DesktopInsightsUiState {
        if (input.transactionCount == 0 && input.budgets.none { it.budgetMinor > 0L }) {
            return DesktopInsightsUiState.Empty(input.periodLabel)
        }

        val result = runCatching { provider.generate(input) }
            .getOrElse {
                return DesktopInsightsUiState.Error("Could not generate desktop insights.")
            }
        val expense = abs(input.totalExpenseMinor)
        val net = input.totalIncomeMinor - expense
        return DesktopInsightsUiState.Content(
            periodLabel = input.periodLabel,
            incomeText = input.totalIncomeMinor.toMoney(input.currencyCode),
            expenseText = expense.toMoney(input.currencyCode),
            netText = net.toMoney(input.currencyCode),
            topCategories = input.categories
                .filter { it.totalExpenseMinor > 0L }
                .sortedWith(compareByDescending<DesktopCategorySummary> { it.totalExpenseMinor }.thenBy { it.label })
                .take(5),
            result = result,
        )
    }
}

interface DesktopInsightsProvider {
    fun generate(input: DesktopMonthlySummaryInput): DesktopMonthlyInsightResult
}

class RuleBasedDesktopInsightsProvider : DesktopInsightsProvider {
    override fun generate(input: DesktopMonthlySummaryInput): DesktopMonthlyInsightResult {
        val expense = abs(input.totalExpenseMinor)
        val net = input.totalIncomeMinor - expense
        val topCategories = input.categories
            .filter { it.totalExpenseMinor > 0L }
            .sortedWith(compareByDescending<DesktopCategorySummary> { it.totalExpenseMinor }.thenBy { it.label })
            .take(3)

        val summary = when {
            input.transactionCount == 0 -> "No local transactions were found for ${input.periodLabel}."
            input.totalIncomeMinor == 0L && expense > 0L ->
                "${input.periodLabel} included ${input.transactionCount} sample transactions and recorded expenses with no income in this period."
            expense == 0L && input.totalIncomeMinor > 0L ->
                "${input.periodLabel} included ${input.transactionCount} sample transactions and recorded income with no expenses in this period."
            else -> "${input.periodLabel} included ${input.transactionCount} sample transactions with ${net.netDirection()} net cash flow."
        }

        val insights = buildList {
            add("Income total: ${input.totalIncomeMinor.toMajorUnits()} ${input.currencyCode}.")
            add("Expense total: ${expense.toMajorUnits()} ${input.currencyCode}.")
            topCategories.firstOrNull()?.let { add("Top spending group: ${it.label}.") }
            input.recurringHints.firstOrNull()?.let {
                add("Frequent local pattern: ${it.label} appeared ${it.transactionCount} times.")
            }
        }
        val warnings = buildList {
            if (expense > input.totalIncomeMinor && expense > 0L) {
                add("Expenses were higher than income for this period.")
            }
            val top = topCategories.firstOrNull()
            if (top != null && expense > 0L && top.totalExpenseMinor * 100L >= expense * 50L) {
                add("Spending was concentrated in ${top.label}.")
            }
            input.budgets.firstOrNull { it.budgetMinor > 0L && it.spentMinor >= it.budgetMinor }?.let {
                add("One sample budget is at or above its configured limit: ${it.label}.")
            }
        }
        val actions = buildList {
            if (input.transactionCount == 0) {
                add("Add transactions to generate more useful local insights.")
            } else {
                add("Review top spending groups for unusual local activity.")
            }
            if (warnings.isNotEmpty()) {
                add("Compare this month with budget settings.")
            }
        }

        return DesktopMonthlyInsightResult(
            title = "${input.periodLabel} private summary",
            summaryText = summary,
            insights = insights,
            warnings = warnings,
            suggestedActions = actions,
            providerStatus = DesktopProviderStatus.RuleBasedFallback,
        )
    }
}

private fun Long.toMoney(currencyCode: String): String =
    String.format(Locale.US, "%s %.2f", currencyCode.trim().uppercase(Locale.US), this / 100.0)

private fun Long.toMajorUnits(): String = String.format(Locale.US, "%.2f", this / 100.0)

private fun Long.netDirection(): String = when {
    this > 0L -> "positive"
    this < 0L -> "negative"
    else -> "flat"
}
