package com.mojtaba.pocketledger.desktop.persistence

import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import com.mojtaba.pocketledger.core.database.model.BudgetEntity
import com.mojtaba.pocketledger.core.database.model.CategoryEntity
import com.mojtaba.pocketledger.core.database.model.TagEntity
import com.mojtaba.pocketledger.core.database.model.TransactionEntity
import com.mojtaba.pocketledger.core.database.model.TransactionTagCrossRef
import com.mojtaba.pocketledger.desktop.insights.DesktopBudgetComparison
import com.mojtaba.pocketledger.desktop.insights.DesktopCategorySummary
import com.mojtaba.pocketledger.desktop.insights.DesktopMonthlySummaryInput
import com.mojtaba.pocketledger.desktop.insights.DesktopRecurringHint
import com.mojtaba.pocketledger.desktop.search.DesktopSearchRecord
import com.mojtaba.pocketledger.desktop.search.DesktopSearchTransactionType
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class DesktopLedgerLocalDataSource(
    private val database: PocketLedgerDatabase,
    private val clockMillis: () -> Long = { System.currentTimeMillis() },
) {
    suspend fun loadSnapshot(): DesktopLedgerSnapshot {
        seedIfEmpty()
        val transactions = database.transactionDao().observeRecentTransactions(250).first()
        val categories = database.categoryDao().observeAll().first().associateBy { it.id }
        val budgets = database.budgetDao().observeActiveBudgets().first()
        val tagsByTransaction = transactions.associate { transaction ->
            transaction.id to database.tagDao().observeTagsForTransaction(transaction.id).first().map { it.name }
        }
        return DesktopLedgerSnapshot(
            searchRecords = transactions.map { transaction ->
                transaction.toSearchRecord(
                    categoryName = transaction.categoryId?.let { categories[it]?.name }.orEmpty().ifBlank { "Uncategorized" },
                    tags = tagsByTransaction.getValue(transaction.id),
                )
            },
            monthlySummary = transactions.toMonthlySummary(categories, budgets),
        )
    }

    private suspend fun seedIfEmpty() {
        if (database.transactionDao().observeRecentTransactions(1).first().isNotEmpty()) return

        val now = clockMillis()
        val categories = listOf(
            CategoryEntity("desktop-income", "Income", "income", sortOrder = 0, createdAt = now, updatedAt = now),
            CategoryEntity("desktop-housing", "Housing", "expense", sortOrder = 10, createdAt = now, updatedAt = now),
            CategoryEntity("desktop-groceries", "Groceries", "expense", sortOrder = 20, createdAt = now, updatedAt = now),
            CategoryEntity("desktop-transport", "Transportation", "expense", sortOrder = 30, createdAt = now, updatedAt = now),
            CategoryEntity("desktop-dining", "Dining", "expense", sortOrder = 40, createdAt = now, updatedAt = now),
            CategoryEntity("desktop-utilities", "Utilities", "expense", sortOrder = 50, createdAt = now, updatedAt = now),
            CategoryEntity("desktop-entertainment", "Entertainment", "expense", sortOrder = 60, createdAt = now, updatedAt = now),
        )
        val tags = listOf(
            TagEntity("desktop-work", "work", createdAt = now, updatedAt = now),
            TagEntity("desktop-recurring", "recurring", createdAt = now, updatedAt = now),
            TagEntity("desktop-essential", "essential", createdAt = now, updatedAt = now),
            TagEntity("desktop-weekend", "weekend", createdAt = now, updatedAt = now),
        )
        val transactions = listOf(
            TransactionSeed("demo-search-salary", 320_000, "income", 1_769_878_800_000L, "desktop-income", "Sample Payroll Deposit", "Sample paycheck", true, listOf("desktop-work", "desktop-recurring")),
            TransactionSeed("demo-search-rent", -145_000, "expense", 1_769_936_400_000L, "desktop-housing", "Sample Apartment Rent", "Monthly housing sample", true, listOf("desktop-essential", "desktop-recurring")),
            TransactionSeed("demo-search-groceries-a", -8_742, "expense", 1_770_055_200_000L, "desktop-groceries", "Sample Neighborhood Market", "Weekly grocery sample", false, listOf("desktop-essential")),
            TransactionSeed("demo-search-transit", -9_600, "expense", 1_770_105_600_000L, "desktop-transport", "Sample Transit Pass", "Monthly pass sample", true, listOf("desktop-essential", "desktop-recurring")),
            TransactionSeed("demo-search-coffee", -625, "expense", 1_770_217_200_000L, "desktop-dining", "Client Coffee Stop", "Client catch-up sample", false, listOf("desktop-work")),
            TransactionSeed("demo-search-utilities", -12_840, "expense", 1_770_321_600_000L, "desktop-utilities", "Sample Power Bill", "Electric bill sample", true, listOf("desktop-essential", "desktop-recurring")),
            TransactionSeed("demo-search-freelance", 48_000, "income", 1_770_739_200_000L, "desktop-income", "Sample Freelance Invoice", "Local demo invoice", false, listOf("desktop-work")),
            TransactionSeed("demo-search-concert", -8_250, "expense", 1_772_229_600_000L, "desktop-entertainment", "Sample Riverside Show", "Weekend event sample", false, listOf("desktop-weekend")),
        )
        val budgets = listOf(
            BudgetEntity("desktop-budget-groceries", "Groceries sample budget", 55_000, "USD", "monthly", 1_769_616_000_000L, 1_772_031_999_999L, "desktop-groceries", createdAt = now, updatedAt = now),
            BudgetEntity("desktop-budget-dining", "Dining sample budget", 28_000, "USD", "monthly", 1_769_616_000_000L, 1_772_031_999_999L, "desktop-dining", createdAt = now, updatedAt = now),
            BudgetEntity("desktop-budget-transport", "Transportation sample budget", 18_000, "USD", "monthly", 1_769_616_000_000L, 1_772_031_999_999L, "desktop-transport", createdAt = now, updatedAt = now),
            BudgetEntity("desktop-budget-entertainment", "Entertainment sample budget", 16_000, "USD", "monthly", 1_769_616_000_000L, 1_772_031_999_999L, "desktop-entertainment", createdAt = now, updatedAt = now),
        )

        database.categoryDao().insertAll(categories)
        database.tagDao().insertAll(tags)
        database.budgetDao().insertAll(budgets)
        database.transactionDao().insertAll(transactions.map { it.toEntity(now) })
        transactions.forEach { seed ->
            seed.tagIds.forEach { tagId ->
                database.tagDao().addTagToTransaction(TransactionTagCrossRef(seed.id, tagId))
            }
        }
    }
}

data class DesktopLedgerSnapshot(
    val searchRecords: List<DesktopSearchRecord>,
    val monthlySummary: DesktopMonthlySummaryInput,
)

private data class TransactionSeed(
    val id: String,
    val amountMinor: Long,
    val type: String,
    val occurredAt: Long,
    val categoryId: String,
    val merchant: String,
    val note: String,
    val recurring: Boolean,
    val tagIds: List<String>,
) {
    fun toEntity(now: Long): TransactionEntity = TransactionEntity(
        id = id,
        amountMinor = amountMinor,
        currencyCode = "USD",
        type = type,
        occurredAt = occurredAt,
        categoryId = categoryId,
        merchant = merchant,
        note = note,
        source = "desktop-demo",
        isRecurring = recurring,
        createdAt = now,
        updatedAt = now,
    )
}

private fun TransactionEntity.toSearchRecord(
    categoryName: String,
    tags: List<String>,
): DesktopSearchRecord = DesktopSearchRecord(
    id = id,
    title = merchant ?: source ?: "Transaction",
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    type = if (type.lowercase(Locale.US) == "income") DesktopSearchTransactionType.Income else DesktopSearchTransactionType.Expense,
    occurredAtMillis = occurredAt,
    category = categoryName,
    notePreview = note,
    tags = tags,
    recurring = isRecurring,
)

private fun List<TransactionEntity>.toMonthlySummary(
    categories: Map<String, CategoryEntity>,
    budgets: List<BudgetEntity>,
): DesktopMonthlySummaryInput {
    val periodLabel = firstOrNull()?.occurredAt?.toPeriodLabel() ?: "Local ledger"
    val categorySummaries = filter { it.amountMinor < 0L }
        .groupBy { it.categoryId?.let { categoryId -> categories[categoryId]?.name } ?: "Uncategorized" }
        .map { (label, transactions) -> DesktopCategorySummary(label, transactions.sumOf { abs(it.amountMinor) }, transactions.size) }
    val spentByCategory = filter { it.amountMinor < 0L }
        .groupBy { it.categoryId }
        .mapValues { (_, transactions) -> transactions.sumOf { abs(it.amountMinor) } }
    val recurringHints = filter { it.isRecurring }
        .groupBy { it.categoryId?.let { categoryId -> categories[categoryId]?.name } ?: it.merchant ?: "Recurring" }
        .map { (label, transactions) -> DesktopRecurringHint(label, transactions.size) }
        .sortedWith(compareByDescending<DesktopRecurringHint> { it.transactionCount }.thenBy { it.label })

    return DesktopMonthlySummaryInput(
        periodLabel = periodLabel,
        currencyCode = firstOrNull()?.currencyCode ?: "USD",
        totalIncomeMinor = filter { it.amountMinor > 0L }.sumOf { it.amountMinor },
        totalExpenseMinor = filter { it.amountMinor < 0L }.sumOf { abs(it.amountMinor) },
        transactionCount = size,
        categories = categorySummaries,
        budgets = budgets.map { budget ->
            DesktopBudgetComparison(
                label = budget.name,
                spentMinor = spentByCategory[budget.categoryId] ?: 0L,
                budgetMinor = budget.amountMinor,
            )
        },
        recurringHints = recurringHints,
    )
}

private fun Long.toPeriodLabel(): String =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
        .format(Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC))
