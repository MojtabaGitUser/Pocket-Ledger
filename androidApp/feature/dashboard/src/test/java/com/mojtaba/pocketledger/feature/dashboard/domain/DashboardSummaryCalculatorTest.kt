package com.mojtaba.pocketledger.feature.dashboard.domain

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardInsight
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardPeriod
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardTransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardSummaryCalculatorTest {
    @Test
    fun emptyInputProducesZeroSummaryAndNoDataInsight() {
        val summary = calculate()

        assertEquals(0L, summary.cashFlow.incomeMinor)
        assertEquals(0L, summary.cashFlow.expenseMinor)
        assertEquals(0L, summary.cashFlow.netMinor)
        assertEquals(emptyList<Any>(), summary.topCategories)
        assertEquals(listOf(DashboardInsight.NoData), summary.insights)
    }

    @Test
    fun calculatesIncomeExpenseAndNet() {
        val summary = calculate(
            transactions = listOf(
                transaction(id = "income", amountMinor = 125_00, type = "income"),
                transaction(id = "expense", amountMinor = -40_00, type = "expense"),
            ),
        )

        assertEquals(125_00L, summary.cashFlow.incomeMinor)
        assertEquals(40_00L, summary.cashFlow.expenseMinor)
        assertEquals(85_00L, summary.cashFlow.netMinor)
        assertTrue(summary.insights.contains(DashboardInsight.PositiveCashFlow(85_00L, "USD")))
    }

    @Test
    fun aggregatesCategorySpendingAndPercentages() {
        val summary = calculate(
            categories = listOf(
                category(id = "food", name = "Food"),
                category(id = "travel", name = "Travel"),
            ),
            transactions = listOf(
                transaction(id = "food-1", amountMinor = -30_00, categoryId = "food"),
                transaction(id = "food-2", amountMinor = -20_00, categoryId = "food"),
                transaction(id = "travel-1", amountMinor = -50_00, categoryId = "travel"),
            ),
        )

        assertEquals(2, summary.topCategories.size)
        assertEquals("Food", summary.topCategories[0].categoryName)
        assertEquals(50_00L, summary.topCategories[0].amountMinor)
        assertEquals(2, summary.topCategories[0].transactionCount)
        assertEquals(50.0, summary.topCategories[0].percentageOfExpense, 0.001)
        assertEquals("Travel", summary.topCategories[1].categoryName)
        assertEquals(50.0, summary.topCategories[1].percentageOfExpense, 0.001)
    }

    @Test
    fun topCategoriesAreSortedByAmountThenNameThenId() {
        val summary = calculate(
            categories = listOf(
                category(id = "z-id", name = "Beta"),
                category(id = "a-id", name = "Alpha"),
                category(id = "b-id", name = "Alpha"),
            ),
            transactions = listOf(
                transaction(id = "beta", amountMinor = -50_00, categoryId = "z-id"),
                transaction(id = "alpha-b", amountMinor = -50_00, categoryId = "b-id"),
                transaction(id = "alpha-a", amountMinor = -50_00, categoryId = "a-id"),
            ),
        )

        assertEquals(listOf("a-id", "b-id", "z-id"), summary.topCategories.map { it.categoryId })
    }

    @Test
    fun topCategoryLimitIsApplied() {
        val summary = calculate(
            categories = listOf(
                category(id = "a", name = "A"),
                category(id = "b", name = "B"),
                category(id = "c", name = "C"),
            ),
            transactions = listOf(
                transaction(id = "a", amountMinor = -30_00, categoryId = "a"),
                transaction(id = "b", amountMinor = -20_00, categoryId = "b"),
                transaction(id = "c", amountMinor = -10_00, categoryId = "c"),
            ),
            topCategoryLimit = 2,
        )

        assertEquals(listOf("a", "b"), summary.topCategories.map { it.categoryId })
    }

    @Test
    fun recentTransactionsAreSortedByOccurredAtDescendingThenId() {
        val summary = calculate(
            transactions = listOf(
                transaction(id = "b", amountMinor = -10_00, occurredAt = 300L, note = "Newest"),
                transaction(id = "a", amountMinor = -20_00, occurredAt = 300L, note = "Same time"),
                transaction(id = "c", amountMinor = 50_00, type = "Income", occurredAt = 200L),
            ),
            recentTransactionLimit = 2,
        )

        assertEquals(listOf("a", "b"), summary.recentTransactions.map { it.transactionId })
        assertEquals(DashboardTransactionType.Expense, summary.recentTransactions[0].type)
        assertEquals("Same time", summary.recentTransactions[0].notePreview)
    }

    @Test
    fun notePreviewIsTrimmedAndTruncated() {
        val longNote = "  " + "a".repeat(80) + "  "
        val summary = calculate(
            transactions = listOf(
                transaction(id = "long-note", amountMinor = -10_00, note = longNote),
            ),
        )

        assertEquals("${"a".repeat(72)}...", summary.recentTransactions.single().notePreview)
    }

    @Test
    fun missingCategoryUsesFallbackForCategorySpendAndNullForRecentTransaction() {
        val summary = calculate(
            transactions = listOf(
                transaction(id = "missing", amountMinor = -10_00, categoryId = "missing"),
            ),
        )

        assertEquals("Uncategorized", summary.topCategories.single().categoryName)
        assertEquals(null, summary.recentTransactions.single().categoryName)
    }

    @Test
    fun budgetBelowLimitIsOnTrack() {
        val summary = calculate(
            categories = listOf(category("food", "Food")),
            budgets = listOf(budget(id = "budget", amountMinor = 100_00, categoryId = "food")),
            transactions = listOf(transaction(id = "food", amountMinor = -50_00, categoryId = "food")),
        )

        val progress = summary.budgetProgress.single()
        assertEquals(50_00L, progress.spentMinor)
        assertEquals(50.0, progress.progressPercent, 0.001)
        assertEquals(BudgetProgressStatus.OnTrack, progress.status)
    }

    @Test
    fun inactiveBudgetsAreIgnored() {
        val summary = calculate(
            budgets = listOf(
                budget(id = "inactive", amountMinor = 100_00, isActive = false),
                budget(id = "active", amountMinor = 100_00, isActive = true),
            ),
            transactions = listOf(transaction(id = "expense", amountMinor = -20_00)),
        )

        assertEquals(listOf("active"), summary.budgetProgress.map { it.budgetId })
    }

    @Test
    fun budgetPeriodMustOverlapDashboardPeriod() {
        val summary = calculate(
            budgets = listOf(
                budget(id = "before", amountMinor = 100_00, periodStart = 1L, periodEnd = 99L),
                budget(id = "overlap-start", amountMinor = 100_00, periodStart = 1L, periodEnd = 100L),
                budget(id = "inside", amountMinor = 100_00, periodStart = 150L, periodEnd = 250L),
                budget(id = "overlap-end", amountMinor = 100_00, periodStart = 400L, periodEnd = 450L),
                budget(id = "after", amountMinor = 100_00, periodStart = 401L, periodEnd = 500L),
            ),
            transactions = listOf(transaction(id = "expense", amountMinor = -20_00)),
        )

        assertEquals(
            listOf("inside", "overlap-end", "overlap-start"),
            summary.budgetProgress.map { it.budgetId },
        )
    }

    @Test
    fun budgetNearLimitCreatesNearLimitInsight() {
        val summary = calculate(
            budgets = listOf(budget(id = "budget", amountMinor = 100_00)),
            transactions = listOf(transaction(id = "expense", amountMinor = -80_00)),
        )

        assertEquals(BudgetProgressStatus.NearLimit, summary.budgetProgress.single().status)
        assertTrue(
            summary.insights.contains(
                DashboardInsight.BudgetNearLimit(
                    budgetId = "budget",
                    budgetName = "Monthly Budget",
                    progressPercent = 80.0,
                ),
            ),
        )
    }

    @Test
    fun budgetExceededCreatesExceededInsight() {
        val summary = calculate(
            budgets = listOf(budget(id = "budget", amountMinor = 100_00)),
            transactions = listOf(transaction(id = "expense", amountMinor = -150_00)),
        )

        assertEquals(BudgetProgressStatus.Exceeded, summary.budgetProgress.single().status)
        assertTrue(
            summary.insights.contains(
                DashboardInsight.BudgetExceeded(
                    budgetId = "budget",
                    budgetName = "Monthly Budget",
                    progressPercent = 150.0,
                ),
            ),
        )
    }

    @Test
    fun zeroBudgetLimitIsHandledSafely() {
        val summary = calculate(
            budgets = listOf(budget(id = "budget", amountMinor = 0L)),
            transactions = listOf(transaction(id = "expense", amountMinor = -20_00)),
        )

        val progress = summary.budgetProgress.single()
        assertEquals(0.0, progress.progressPercent, 0.001)
        assertEquals(BudgetProgressStatus.NoLimit, progress.status)
    }

    @Test
    fun unknownTransactionTypeIsIgnored() {
        val summary = calculate(
            transactions = listOf(
                transaction(id = "unknown", amountMinor = 99_00, type = "transfer"),
            ),
        )

        assertEquals(0L, summary.cashFlow.incomeMinor)
        assertEquals(0L, summary.cashFlow.expenseMinor)
        assertEquals(emptyList<Any>(), summary.recentTransactions)
        assertEquals(listOf(DashboardInsight.NoData), summary.insights)
    }

    @Test
    fun transactionsOutsidePeriodAreIgnored() {
        val summary = calculate(
            transactions = listOf(
                transaction(id = "before", amountMinor = -90_00, occurredAt = 99L),
                transaction(id = "inside", amountMinor = -10_00, occurredAt = 100L),
                transaction(id = "after", amountMinor = -80_00, occurredAt = 401L),
            ),
        )

        assertEquals(10_00L, summary.cashFlow.expenseMinor)
        assertEquals(listOf("inside"), summary.recentTransactions.map { it.transactionId })
    }

    @Test
    fun mixedCurrencyRecordsAreExcluded() {
        val summary = calculate(
            budgets = listOf(
                budget(id = "usd-budget", amountMinor = 100_00, currencyCode = "USD"),
                budget(id = "cad-budget", amountMinor = 100_00, currencyCode = "CAD"),
            ),
            transactions = listOf(
                transaction(id = "usd", amountMinor = -40_00, currencyCode = "usd"),
                transaction(id = "cad", amountMinor = -70_00, currencyCode = "CAD"),
            ),
            currencyCode = "USD",
        )

        assertEquals(40_00L, summary.cashFlow.expenseMinor)
        assertEquals(listOf("usd"), summary.recentTransactions.map { it.transactionId })
        assertEquals(listOf("usd-budget"), summary.budgetProgress.map { it.budgetId })
    }

    @Test
    fun onlyExpensesProducesNegativeCashFlowInsight() {
        val summary = calculate(
            transactions = listOf(transaction(id = "expense", amountMinor = -40_00)),
        )

        assertTrue(summary.insights.contains(DashboardInsight.NegativeCashFlow(-40_00L, "USD")))
    }

    @Test
    fun onlyIncomeHasZeroCategoryPercentagesAndNoTopCategories() {
        val summary = calculate(
            transactions = listOf(transaction(id = "income", amountMinor = 40_00, type = "income")),
        )

        assertEquals(40_00L, summary.cashFlow.incomeMinor)
        assertEquals(0L, summary.cashFlow.expenseMinor)
        assertEquals(emptyList<Any>(), summary.topCategories)
    }

    @Test
    fun incomeAndExpenseAmountsAreNormalizedByAbsoluteValue() {
        val summary = calculate(
            transactions = listOf(
                transaction(id = "income-negative", amountMinor = -100_00, type = "income"),
                transaction(id = "expense-positive", amountMinor = 25_00, type = "expense"),
            ),
        )

        assertEquals(100_00L, summary.cashFlow.incomeMinor)
        assertEquals(25_00L, summary.cashFlow.expenseMinor)
        assertEquals(75_00L, summary.cashFlow.netMinor)
    }

    @Test
    fun inputPreconditionsRejectInvalidValues() {
        assertThrows(IllegalArgumentException::class.java) {
            calculate(currencyCode = " ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            calculate(recentTransactionLimit = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            calculate(topCategoryLimit = -1)
        }
    }

    private fun calculate(
        transactions: List<LedgerTransaction> = emptyList(),
        categories: List<LedgerCategory> = emptyList(),
        budgets: List<LedgerBudget> = emptyList(),
        currencyCode: String = "USD",
        recentTransactionLimit: Int = DashboardSummaryCalculator.DefaultRecentTransactionLimit,
        topCategoryLimit: Int = DashboardSummaryCalculator.DefaultTopCategoryLimit,
    ) = DashboardSummaryCalculator.calculate(
        DashboardSummaryInput(
            period = DashboardPeriod(startMillis = 100L, endMillis = 400L, label = "Period"),
            currencyCode = currencyCode,
            transactions = transactions,
            categories = categories,
            budgets = budgets,
            generatedAt = 500L,
            recentTransactionLimit = recentTransactionLimit,
            topCategoryLimit = topCategoryLimit,
        ),
    )

    private fun transaction(
        id: String,
        amountMinor: Long = -10_00,
        currencyCode: String = "USD",
        type: String = "expense",
        occurredAt: Long = 200L,
        categoryId: String? = null,
        note: String? = null,
    ) = LedgerTransaction(
        id = id,
        amountMinor = amountMinor,
        currencyCode = currencyCode,
        type = type,
        occurredAt = occurredAt,
        categoryId = categoryId,
        merchant = null,
        note = note,
        source = "manual",
        isRecurring = false,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun category(
        id: String,
        name: String,
    ) = LedgerCategory(
        id = id,
        name = name,
        type = "expense",
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun budget(
        id: String,
        amountMinor: Long,
        currencyCode: String = "USD",
        categoryId: String? = null,
        periodStart: Long = 100L,
        periodEnd: Long = 400L,
        isActive: Boolean = true,
    ) = LedgerBudget(
        id = id,
        name = "Monthly Budget",
        amountMinor = amountMinor,
        currencyCode = currencyCode,
        periodType = "monthly",
        periodStart = periodStart,
        periodEnd = periodEnd,
        categoryId = categoryId,
        isActive = isActive,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
