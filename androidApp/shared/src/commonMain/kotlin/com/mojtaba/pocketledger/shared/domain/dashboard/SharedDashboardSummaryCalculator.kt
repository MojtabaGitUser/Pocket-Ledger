package com.mojtaba.pocketledger.shared.domain.dashboard

import kotlin.math.abs

data class SharedDashboardPeriod(
    val startMillis: Long,
    val endMillis: Long,
    val label: String,
) {
    init {
        require(startMillis <= endMillis) { "DashboardPeriod startMillis must be before or equal to endMillis." }
    }

    fun contains(epochMillis: Long): Boolean = epochMillis in startMillis..endMillis
}

data class SharedDashboardInput(
    val transactions: List<SharedDashboardTransaction>,
    val categories: List<SharedDashboardCategory>,
    val budgets: List<SharedDashboardBudget>,
    val period: SharedDashboardPeriod,
    val currencyCode: String,
    val generatedAt: Long,
    val recentTransactionLimit: Int = SharedDashboardSummaryCalculator.DefaultRecentTransactionLimit,
    val topCategoryLimit: Int = SharedDashboardSummaryCalculator.DefaultTopCategoryLimit,
)

data class SharedDashboardTransaction(
    val id: String,
    val amountMinor: Long,
    val type: String,
    val categoryId: String?,
    val note: String?,
    val occurredAt: Long,
    val currencyCode: String,
)

data class SharedDashboardCategory(
    val id: String,
    val name: String,
)

data class SharedDashboardBudget(
    val id: String,
    val name: String,
    val amountMinor: Long,
    val currencyCode: String,
    val periodStart: Long,
    val periodEnd: Long,
    val categoryId: String?,
    val isActive: Boolean,
)

data class SharedDashboardSummary(
    val period: SharedDashboardPeriod,
    val cashFlow: SharedCashFlowSummary,
    val topCategories: List<SharedCategorySpendSummary>,
    val budgetProgress: List<SharedBudgetProgressSummary>,
    val recentTransactions: List<SharedRecentTransactionSummary>,
    val insights: List<SharedDashboardInsight>,
    val generatedAt: Long,
)

data class SharedCashFlowSummary(
    val incomeMinor: Long,
    val expenseMinor: Long,
    val netMinor: Long,
    val currencyCode: String,
)

data class SharedCategorySpendSummary(
    val categoryId: String?,
    val categoryName: String,
    val amountMinor: Long,
    val currencyCode: String,
    val transactionCount: Int,
    val percentageOfExpense: Double,
)

data class SharedBudgetProgressSummary(
    val budgetId: String,
    val budgetName: String,
    val categoryId: String?,
    val categoryName: String?,
    val spentMinor: Long,
    val limitMinor: Long,
    val currencyCode: String,
    val progressPercent: Double,
    val status: SharedBudgetProgressStatus,
)

enum class SharedBudgetProgressStatus {
    OnTrack,
    NearLimit,
    Exceeded,
    NoLimit,
}

data class SharedRecentTransactionSummary(
    val transactionId: String,
    val amountMinor: Long,
    val currencyCode: String,
    val type: SharedDashboardTransactionType,
    val categoryName: String?,
    val notePreview: String?,
    val occurredAt: Long,
)

enum class SharedDashboardTransactionType {
    Income,
    Expense,
    Unknown,
}

sealed interface SharedDashboardInsight {
    data object NoData : SharedDashboardInsight

    data class PositiveCashFlow(
        val netMinor: Long,
        val currencyCode: String,
    ) : SharedDashboardInsight

    data class NegativeCashFlow(
        val netMinor: Long,
        val currencyCode: String,
    ) : SharedDashboardInsight

    data class OverspendingCategory(
        val categoryId: String?,
        val categoryName: String,
        val amountMinor: Long,
        val currencyCode: String,
        val percentageOfExpense: Double,
    ) : SharedDashboardInsight

    data class BudgetNearLimit(
        val budgetId: String,
        val budgetName: String,
        val progressPercent: Double,
    ) : SharedDashboardInsight

    data class BudgetExceeded(
        val budgetId: String,
        val budgetName: String,
        val progressPercent: Double,
    ) : SharedDashboardInsight
}

object SharedDashboardSummaryCalculator {
    const val DefaultRecentTransactionLimit = 5
    const val DefaultTopCategoryLimit = 5
    private const val NearLimitThreshold = 80.0
    private const val OverspendingCategoryThreshold = 50.0
    private const val NotePreviewLimit = 72
    private const val Uncategorized = "Uncategorized"

    fun calculate(input: SharedDashboardInput): SharedDashboardSummary {
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
                if (type == SharedDashboardTransactionType.Unknown) null else transaction to type
            }
            .toList()

        val incomeMinor = periodTransactions
            .filter { (_, type) -> type == SharedDashboardTransactionType.Income }
            .sumOf { (transaction, _) -> abs(transaction.amountMinor) }
        val expenseMinor = periodTransactions
            .filter { (_, type) -> type == SharedDashboardTransactionType.Expense }
            .sumOf { (transaction, _) -> abs(transaction.amountMinor) }
        val cashFlow = SharedCashFlowSummary(
            incomeMinor = incomeMinor,
            expenseMinor = expenseMinor,
            netMinor = incomeMinor - expenseMinor,
            currencyCode = normalizedCurrency,
        )

        val expenseTransactions = periodTransactions
            .filter { (_, type) -> type == SharedDashboardTransactionType.Expense }
            .map { (transaction, _) -> transaction }
        val topCategories = expenseTransactions
            .groupBy { it.categoryId }
            .map { (categoryId, transactions) ->
                val amountMinor = transactions.sumOf { abs(it.amountMinor) }
                SharedCategorySpendSummary(
                    categoryId = categoryId,
                    categoryName = categoriesById[categoryId]?.name.cleanOrNull() ?: Uncategorized,
                    amountMinor = amountMinor,
                    currencyCode = normalizedCurrency,
                    transactionCount = transactions.size,
                    percentageOfExpense = percentage(amountMinor, expenseMinor),
                )
            }
            .sortedWith(
                compareByDescending<SharedCategorySpendSummary> { it.amountMinor }
                    .thenBy { it.categoryName.lowercase() }
                    .thenBy { it.categoryId.orEmpty() },
            )
            .take(input.topCategoryLimit)

        val recentTransactions = periodTransactions
            .sortedWith(
                compareByDescending<Pair<SharedDashboardTransaction, SharedDashboardTransactionType>> { it.first.occurredAt }
                    .thenBy { it.first.id },
            )
            .take(input.recentTransactionLimit)
            .map { (transaction, type) ->
                SharedRecentTransactionSummary(
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
                    .filter { transaction -> budget.categoryId == null || transaction.categoryId == budget.categoryId }
                    .sumOf { abs(it.amountMinor) }
                budget.toProgressSummary(
                    spentMinor = spentMinor,
                    categoriesById = categoriesById,
                    currencyCode = normalizedCurrency,
                )
            }
            .sortedWith(
                compareByDescending<SharedBudgetProgressSummary> { it.progressPercent }
                    .thenBy { it.budgetName.lowercase() }
                    .thenBy { it.budgetId },
            )
            .toList()

        return SharedDashboardSummary(
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

    private fun SharedDashboardTransaction.dashboardType(): SharedDashboardTransactionType =
        when (type.trim().lowercase()) {
            "income" -> SharedDashboardTransactionType.Income
            "expense" -> SharedDashboardTransactionType.Expense
            else -> SharedDashboardTransactionType.Unknown
        }

    private fun SharedDashboardBudget.overlaps(startMillis: Long, endMillis: Long): Boolean =
        periodStart <= endMillis && periodEnd >= startMillis

    private fun SharedDashboardBudget.toProgressSummary(
        spentMinor: Long,
        categoriesById: Map<String, SharedDashboardCategory>,
        currencyCode: String,
    ): SharedBudgetProgressSummary {
        val progressPercent = if (amountMinor > 0L) {
            spentMinor.toDouble() / amountMinor.toDouble() * 100.0
        } else {
            0.0
        }
        val status = when {
            amountMinor <= 0L -> SharedBudgetProgressStatus.NoLimit
            progressPercent >= 100.0 -> SharedBudgetProgressStatus.Exceeded
            progressPercent >= NearLimitThreshold -> SharedBudgetProgressStatus.NearLimit
            else -> SharedBudgetProgressStatus.OnTrack
        }
        return SharedBudgetProgressSummary(
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
        cashFlow: SharedCashFlowSummary,
        topCategories: List<SharedCategorySpendSummary>,
        budgetProgress: List<SharedBudgetProgressSummary>,
        hasDashboardData: Boolean,
    ): List<SharedDashboardInsight> {
        if (!hasDashboardData) return listOf(SharedDashboardInsight.NoData)

        val items = mutableListOf<SharedDashboardInsight>()
        when {
            cashFlow.netMinor > 0L -> items += SharedDashboardInsight.PositiveCashFlow(
                netMinor = cashFlow.netMinor,
                currencyCode = cashFlow.currencyCode,
            )
            cashFlow.netMinor < 0L -> items += SharedDashboardInsight.NegativeCashFlow(
                netMinor = cashFlow.netMinor,
                currencyCode = cashFlow.currencyCode,
            )
        }

        topCategories.firstOrNull()
            ?.takeIf { it.percentageOfExpense >= OverspendingCategoryThreshold }
            ?.let { category ->
                items += SharedDashboardInsight.OverspendingCategory(
                    categoryId = category.categoryId,
                    categoryName = category.categoryName,
                    amountMinor = category.amountMinor,
                    currencyCode = category.currencyCode,
                    percentageOfExpense = category.percentageOfExpense,
                )
            }

        budgetProgress.forEach { budget ->
            when (budget.status) {
                SharedBudgetProgressStatus.Exceeded -> items += SharedDashboardInsight.BudgetExceeded(
                    budgetId = budget.budgetId,
                    budgetName = budget.budgetName,
                    progressPercent = budget.progressPercent,
                )
                SharedBudgetProgressStatus.NearLimit -> items += SharedDashboardInsight.BudgetNearLimit(
                    budgetId = budget.budgetId,
                    budgetName = budget.budgetName,
                    progressPercent = budget.progressPercent,
                )
                SharedBudgetProgressStatus.NoLimit,
                SharedBudgetProgressStatus.OnTrack,
                -> Unit
            }
        }

        return items.ifEmpty { listOf(SharedDashboardInsight.NoData) }
    }

    private fun percentage(part: Long, whole: Long): Double =
        if (whole <= 0L) 0.0 else part.toDouble() / whole.toDouble() * 100.0

    private fun String.normalizedCurrency(): String = trim().uppercase()

    private fun String?.cleanOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String?.previewOrNull(): String? {
        val cleaned = cleanOrNull() ?: return null
        return if (cleaned.length <= NotePreviewLimit) cleaned else cleaned.take(NotePreviewLimit).trimEnd() + "..."
    }
}