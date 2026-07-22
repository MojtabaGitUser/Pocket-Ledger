package com.mojtaba.folentra.core.testing.performance

import com.mojtaba.folentra.core.data.model.LedgerBudget
import com.mojtaba.folentra.core.data.model.LedgerCategory
import com.mojtaba.folentra.core.data.model.LedgerTag
import com.mojtaba.folentra.core.data.model.LedgerTransaction
import com.mojtaba.folentra.core.data.model.TransactionTagLink
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.Locale

object LargeBenchmarkDataset {
    const val IdPrefix = "large-benchmark-"
    const val TransactionCount = 6_000
    const val CurrencyCode = "USD"
    const val IncomeType = "income"
    const val ExpenseType = "expense"
    const val PeriodTypeMonthly = "monthly"
    const val Source = "large-benchmark"

    private const val CreatedAt = 1_781_952_000_000L // 2026-06-20T00:00:00Z
    private val StartDateTime = LocalDateTime.of(2026, 6, 20, 12, 0)

    fun create(transactionCount: Int = TransactionCount): LargeBenchmarkSeedData {
        require(transactionCount > 0) { "transactionCount must be positive." }

        val categories = incomeCategories() + expenseCategories()
        val tags = tags()
        val budgets = budgets(expenseCategories())
        val transactions = transactions(transactionCount, categories)
        val links = transactionTagLinks(transactions, tags)
        return LargeBenchmarkSeedData(
            categories = categories,
            budgets = budgets,
            tags = tags,
            transactions = transactions,
            transactionTagLinks = links,
        )
    }

    private fun incomeCategories(): List<LedgerCategory> = listOf(
        category("income-payroll", "Payroll", IncomeType, "#2E7D32", "payments", 10),
        category("income-contract", "Contract Work", IncomeType, "#00897B", "work", 20),
    )

    private fun expenseCategories(): List<LedgerCategory> = listOf(
        category("expense-groceries", "Groceries", ExpenseType, "#1565C0", "shopping_cart", 100),
        category("expense-housing", "Housing", ExpenseType, "#6A1B9A", "home", 110),
        category("expense-transportation", "Transportation", ExpenseType, "#00838F", "directions_car", 120),
        category("expense-dining", "Dining", ExpenseType, "#EF6C00", "restaurant", 130),
        category("expense-utilities", "Utilities", ExpenseType, "#455A64", "bolt", 140),
        category("expense-health", "Health", ExpenseType, "#00796B", "local_pharmacy", 150),
        category("expense-entertainment", "Entertainment", ExpenseType, "#C2185B", "theaters", 160),
        category("expense-savings", "Savings", ExpenseType, "#558B2F", "savings", 170),
    )

    private fun tags(): List<LedgerTag> = listOf(
        tag("essential", "essential", "#2E7D32"),
        tag("recurring", "recurring", "#1565C0"),
        tag("work", "work", "#6A1B9A"),
        tag("home", "home", "#00838F"),
        tag("weekday", "weekday", "#EF6C00"),
        tag("weekend", "weekend", "#C2185B"),
        tag("review", "review", "#455A64"),
        tag("planned", "planned", "#558B2F"),
    )

    private fun budgets(expenseCategories: List<LedgerCategory>): List<LedgerBudget> {
        val months = (0 until 6).map { YearMonth.of(2026, 6).minusMonths(it.toLong()) }
        return months.flatMapIndexed { monthIndex, month ->
            expenseCategories.mapIndexed { categoryIndex, category ->
                LedgerBudget(
                    id = prefixedId("budget-${month}-${category.id.removePrefix(prefixedId(""))}"),
                    name = "${category.name} ${month.month.name.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }} budget",
                    amountMinor = 28_000L + categoryIndex * 6_500L + monthIndex * 850L,
                    currencyCode = CurrencyCode,
                    periodType = PeriodTypeMonthly,
                    periodStart = month.atDay(1).startOfDayMillis(),
                    periodEnd = month.atEndOfMonth().endOfDayMillis(),
                    categoryId = category.id,
                    isActive = true,
                    createdAt = CreatedAt,
                    updatedAt = CreatedAt,
                )
            }
        }
    }

    private fun transactions(
        transactionCount: Int,
        categories: List<LedgerCategory>,
    ): List<LedgerTransaction> {
        val incomeCategories = categories.filter { it.type == IncomeType }
        val expenseCategories = categories.filter { it.type == ExpenseType }
        return (0 until transactionCount).map { index ->
            val isIncome = index % 20 == 0
            val category = if (isIncome) {
                incomeCategories[(index / 20) % incomeCategories.size]
            } else {
                expenseCategories[index % expenseCategories.size]
            }
            val occurredAt = StartDateTime.minusHours(index * 2L).toInstant(ZoneOffset.UTC).toEpochMilli()
            LedgerTransaction(
                id = prefixedId("transaction-${index.padded()}"),
                amountMinor = if (isIncome) {
                    185_000L + (index % 9) * 12_500L
                } else {
                    -(850L + ((index * 173L) % 19_500L))
                },
                currencyCode = CurrencyCode,
                type = if (isIncome) IncomeType else ExpenseType,
                occurredAt = occurredAt,
                categoryId = category.id,
                merchant = merchant(index, isIncome),
                note = note(index, category),
                source = Source,
                isRecurring = index % 15 == 0,
                createdAt = CreatedAt + index,
                updatedAt = CreatedAt + index,
            )
        }
    }

    private fun transactionTagLinks(
        transactions: List<LedgerTransaction>,
        tags: List<LedgerTag>,
    ): List<TransactionTagLink> {
        val rotatingTags = tags.filterNot { it.id.endsWith("recurring") }
        return transactions.flatMapIndexed { index, transaction ->
            buildList {
                if (index % 3 == 0) {
                    add(TransactionTagLink(transaction.id, rotatingTags[index % rotatingTags.size].id))
                }
                if (transaction.isRecurring) {
                    add(TransactionTagLink(transaction.id, prefixedId("tag-recurring")))
                }
            }
        }
    }

    private fun category(
        idSuffix: String,
        name: String,
        type: String,
        colorHex: String,
        iconName: String,
        sortOrder: Int,
    ): LedgerCategory = LedgerCategory(
        id = prefixedId("category-$idSuffix"),
        name = name,
        type = type,
        colorHex = colorHex,
        iconName = iconName,
        sortOrder = sortOrder,
        isActive = true,
        createdAt = CreatedAt,
        updatedAt = CreatedAt,
    )

    private fun tag(
        idSuffix: String,
        name: String,
        colorHex: String,
    ): LedgerTag = LedgerTag(
        id = prefixedId("tag-$idSuffix"),
        name = name,
        colorHex = colorHex,
        createdAt = CreatedAt,
        updatedAt = CreatedAt,
    )

    private fun merchant(index: Int, isIncome: Boolean): String =
        when {
            index % 37 == 0 -> "LedgerMart Market ${index.padded()}"
            isIncome -> IncomeMerchants[(index / 20) % IncomeMerchants.size]
            else -> ExpenseMerchants[index % ExpenseMerchants.size]
        }

    private fun note(index: Int, category: LedgerCategory): String =
        if (index % 37 == 0) {
            "Searchable benchmark ledger mart sample ${index.padded()}"
        } else {
            "Benchmark ${category.name.lowercase(Locale.US)} entry ${(index % 96).padded(2)}"
        }

    private fun prefixedId(suffix: String): String = "$IdPrefix$suffix"

    private fun Int.padded(width: Int = 4): String = toString().padStart(width, '0')

    private fun LocalDate.startOfDayMillis(): Long =
        atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    private fun LocalDate.endOfDayMillis(): Long =
        atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC).toEpochMilli()

    private val IncomeMerchants = listOf(
        "Northline Payroll",
        "Freelance Studio",
        "Contract Desk",
        "Design Partner",
    )

    private val ExpenseMerchants = listOf(
        "Neighborhood Market",
        "Metro Loop Transit",
        "City Power Utility",
        "Noodle Lab",
        "Fresh Basket",
        "Riverside Cinema",
        "Wellness Supply",
        "Home Supplies Co",
    )
}

data class LargeBenchmarkSeedData(
    val categories: List<LedgerCategory>,
    val budgets: List<LedgerBudget>,
    val tags: List<LedgerTag>,
    val transactions: List<LedgerTransaction>,
    val transactionTagLinks: List<TransactionTagLink>,
)
