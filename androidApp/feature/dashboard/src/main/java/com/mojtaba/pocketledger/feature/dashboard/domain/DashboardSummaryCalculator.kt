package com.mojtaba.pocketledger.feature.dashboard.domain

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressSummary
import com.mojtaba.pocketledger.feature.dashboard.model.CashFlowSummary
import com.mojtaba.pocketledger.feature.dashboard.model.CategorySpendSummary
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardInsight
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardSummary
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardTransactionType
import com.mojtaba.pocketledger.feature.dashboard.model.RecentTransactionSummary
import java.util.Locale
import kotlin.math.abs

object DashboardSummaryCalculator {
    const val DefaultRecentTransactionLimit = 5
    const val DefaultTopCategoryLimit = 5
    private const val NearLimitThreshold = 80.0
    private const val OverspendingCategoryThreshold = 50.0
    private const val NotePreviewLimit = 72
    private const val Uncategorized = "Uncategorized"

    fun calculate(input: DashboardSummaryInput): DashboardSummary {
        require(input.currencyCode.isNotBlank()) { "DashboardSummaryInput currencyCode must not be blank." }
        require(input.recentTransactionLimit >= 0) { "recentTransactionLimit must not be negative." }
        require(input.topCategoryLimit >= 0) { "topCategoryLimit must not be negative." }

        val normalizedCurrency = input.currencyCode.normalizedCurrency()
        val categoriesById = input.categories.associateBy { it.id }
        val periodTransactions = input.transactions
            .asSequence()
            .filter { input.period.contains(it.occurredAt) }
            .filter { it.currencyCode.normalizedCurrency() == normalizedCurrency }
            .mapNotNull { transaction ->
                val type = transaction.dashboardType()
                if (type == DashboardTransactionType.Unknown) null else transaction to type
            }
            .toList()

        val incomeMinor = periodTransactions
            .filter { (_, type) -> type == DashboardTransactionType.Income }
            .sumOf { (transaction, _) -> abs(transaction.amountMinor) }
        val expenseMinor = periodTransactions
            .filter { (_, type) -> type == DashboardTransactionType.Expense }
            .sumOf { (transaction, _) -> abs(transaction.amountMinor) }
        val cashFlow = CashFlowSummary(
            incomeMinor = incomeMinor,
            expenseMinor = expenseMinor,
            netMinor = incomeMinor - expenseMinor,
            currencyCode = normalizedCurrency,
        )

        val expenseTransactions = periodTransactions
            .filter { (_, type) -> type == DashboardTransactionType.Expense }
            .map { (transaction, _) -> transaction }
        val topCategories = expenseTransactions
            .groupBy { it.categoryId }
            .map { (categoryId, transactions) ->
                val amountMinor = transactions.sumOf { abs(it.amountMinor) }
                CategorySpendSummary(
                    categoryId = categoryId,
                    categoryName = categoriesById[categoryId]?.name.cleanOrNull() ?: Uncategorized,
                    amountMinor = amountMinor,
                    currencyCode = normalizedCurrency,
                    transactionCount = transactions.size,
                    percentageOfExpense = percentage(amountMinor, expenseMinor),
                )
            }
            .sortedWith(
                compareByDescending<CategorySpendSummary> { it.amountMinor }
                    .thenBy { it.categoryName.lowercase(Locale.US) }
                    .thenBy { it.categoryId.orEmpty() },
            )
            .take(input.topCategoryLimit)

        val recentTransactions = periodTransactions
            .sortedWith(
                compareByDescending<Pair<LedgerTransaction, DashboardTransactionType>> { it.first.occurredAt }
                    .thenBy { it.first.id },
            )
            .take(input.recentTransactionLimit)
            .map { (transaction, type) ->
                RecentTransactionSummary(
                    transactionId = transaction.id,
                    amountMinor = transaction.amountMinor,
                    currencyCode = normalizedCurrency,
                    type = type,
                    categoryName = categoriesById[transaction.categoryId]?.name.cleanOrNull(),
                    notePreview = transaction.note.previewOrNull(),
                    occurredAt = transaction.occurredAt,
                )
            }

        val budgetProgress = input.budgets
            .asSequence()
            .filter { it.isActive }
            .filter { it.currencyCode.normalizedCurrency() == normalizedCurrency }
            .filter { it.overlaps(input.period.startMillis, input.period.endMillis) }
            .map { budget ->
                val spentMinor = expenseTransactions
                    .filter { transaction ->
                        budget.categoryId == null || transaction.categoryId == budget.categoryId
                    }
                    .sumOf { abs(it.amountMinor) }
                budget.toProgressSummary(
                    spentMinor = spentMinor,
                    categoriesById = categoriesById,
                    currencyCode = normalizedCurrency,
                )
            }
            .sortedWith(
                compareByDescending<BudgetProgressSummary> { it.progressPercent }
                    .thenBy { it.budgetName.lowercase(Locale.US) }
                    .thenBy { it.budgetId },
            )
            .toList()

        return DashboardSummary(
            period = input.period,
            cashFlow = cashFlow,
            topCategories = topCategories,
            budgetProgress = budgetProgress,
            recentTransactions = recentTransactions,
            insights = insights(
                cashFlow = cashFlow,
                topCategories = topCategories,
                budgetProgress = budgetProgress,
                hasDashboardData = periodTransactions.isNotEmpty() || budgetProgress.isNotEmpty(),
            ),
            generatedAt = input.generatedAt,
        )
    }

    private fun LedgerTransaction.dashboardType(): DashboardTransactionType =
        when (type.trim().lowercase(Locale.US)) {
            "income" -> DashboardTransactionType.Income
            "expense" -> DashboardTransactionType.Expense
            else -> DashboardTransactionType.Unknown
        }

    private fun LedgerBudget.overlaps(startMillis: Long, endMillis: Long): Boolean =
        periodStart <= endMillis && periodEnd >= startMillis

    private fun LedgerBudget.toProgressSummary(
        spentMinor: Long,
        categoriesById: Map<String, LedgerCategory>,
        currencyCode: String,
    ): BudgetProgressSummary {
        val progressPercent = if (amountMinor > 0L) {
            spentMinor.toDouble() / amountMinor.toDouble() * 100.0
        } else {
            0.0
        }
        val status = when {
            amountMinor <= 0L -> BudgetProgressStatus.NoLimit
            progressPercent >= 100.0 -> BudgetProgressStatus.Exceeded
            progressPercent >= NearLimitThreshold -> BudgetProgressStatus.NearLimit
            else -> BudgetProgressStatus.OnTrack
        }
        return BudgetProgressSummary(
            budgetId = id,
            budgetName = name.cleanOrNull() ?: "Budget",
            categoryId = categoryId,
            categoryName = categoriesById[categoryId]?.name.cleanOrNull(),
            spentMinor = spentMinor,
            limitMinor = amountMinor,
            currencyCode = currencyCode,
            progressPercent = progressPercent,
            status = status,
        )
    }

    private fun insights(
        cashFlow: CashFlowSummary,
        topCategories: List<CategorySpendSummary>,
        budgetProgress: List<BudgetProgressSummary>,
        hasDashboardData: Boolean,
    ): List<DashboardInsight> {
        if (!hasDashboardData) return listOf(DashboardInsight.NoData)

        val items = mutableListOf<DashboardInsight>()
        when {
            cashFlow.netMinor > 0L -> items += DashboardInsight.PositiveCashFlow(
                netMinor = cashFlow.netMinor,
                currencyCode = cashFlow.currencyCode,
            )
            cashFlow.netMinor < 0L -> items += DashboardInsight.NegativeCashFlow(
                netMinor = cashFlow.netMinor,
                currencyCode = cashFlow.currencyCode,
            )
        }

        topCategories.firstOrNull()
            ?.takeIf { it.percentageOfExpense >= OverspendingCategoryThreshold }
            ?.let { category ->
                items += DashboardInsight.OverspendingCategory(
                    categoryId = category.categoryId,
                    categoryName = category.categoryName,
                    amountMinor = category.amountMinor,
                    currencyCode = category.currencyCode,
                    percentageOfExpense = category.percentageOfExpense,
                )
            }

        budgetProgress.forEach { budget ->
            when (budget.status) {
                BudgetProgressStatus.Exceeded -> items += DashboardInsight.BudgetExceeded(
                    budgetId = budget.budgetId,
                    budgetName = budget.budgetName,
                    progressPercent = budget.progressPercent,
                )
                BudgetProgressStatus.NearLimit -> items += DashboardInsight.BudgetNearLimit(
                    budgetId = budget.budgetId,
                    budgetName = budget.budgetName,
                    progressPercent = budget.progressPercent,
                )
                BudgetProgressStatus.NoLimit,
                BudgetProgressStatus.OnTrack,
                -> Unit
            }
        }

        return items.ifEmpty { listOf(DashboardInsight.NoData) }
    }

    private fun percentage(part: Long, whole: Long): Double =
        if (whole <= 0L) 0.0 else part.toDouble() / whole.toDouble() * 100.0

    private fun String.normalizedCurrency(): String = trim().uppercase(Locale.US)

    private fun String?.cleanOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String?.previewOrNull(): String? {
        val cleaned = cleanOrNull() ?: return null
        return if (cleaned.length <= NotePreviewLimit) {
            cleaned
        } else {
            cleaned.take(NotePreviewLimit).trimEnd() + "..."
        }
    }
}
