package com.mojtaba.pocketledger.core.data.seed

import com.mojtaba.pocketledger.core.data.model.LedgerBudget
import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink

object DemoSeedData {
    const val CurrencyCode = "USD"
    const val IncomeType = "income"
    const val ExpenseType = "expense"
    const val PeriodTypeMonthly = "monthly"
    const val Source = "demo"

    const val CreatedAt = 1_769_904_000_000L // 2026-02-01T12:00:00Z
    const val PeriodStart = 1_769_817_600_000L // 2026-02-01T00:00:00Z
    const val PeriodEnd = 1_772_236_799_999L // 2026-02-28T23:59:59.999Z

    val categories: List<LedgerCategory> = listOf(
        category(DemoSeedIds.CategorySalary, "Salary / Income", IncomeType, "#2E7D32", "payments", 10),
        category(DemoSeedIds.CategoryGroceries, "Groceries", ExpenseType, "#1565C0", "shopping_cart", 20),
        category(DemoSeedIds.CategoryRent, "Rent", ExpenseType, "#6A1B9A", "home", 30),
        category(DemoSeedIds.CategoryTransportation, "Transportation", ExpenseType, "#00838F", "directions_car", 40),
        category(DemoSeedIds.CategoryDining, "Dining", ExpenseType, "#EF6C00", "restaurant", 50),
        category(DemoSeedIds.CategoryUtilities, "Utilities", ExpenseType, "#455A64", "bolt", 60),
        category(DemoSeedIds.CategoryEntertainment, "Entertainment", ExpenseType, "#C2185B", "theaters", 70),
        category(DemoSeedIds.CategorySavings, "Savings", ExpenseType, "#558B2F", "savings", 80),
    )

    val budgets: List<LedgerBudget> = listOf(
        budget(DemoSeedIds.BudgetGroceries, "Groceries monthly budget", 55_000, DemoSeedIds.CategoryGroceries),
        budget(DemoSeedIds.BudgetDining, "Dining monthly budget", 28_000, DemoSeedIds.CategoryDining),
        budget(DemoSeedIds.BudgetTransportation, "Transportation monthly budget", 18_000, DemoSeedIds.CategoryTransportation),
        budget(DemoSeedIds.BudgetEntertainment, "Entertainment monthly budget", 16_000, DemoSeedIds.CategoryEntertainment),
    )

    val tags: List<LedgerTag> = listOf(
        tag(DemoSeedIds.TagEssential, "essential", "#2E7D32"),
        tag(DemoSeedIds.TagRecurring, "recurring", "#1565C0"),
        tag(DemoSeedIds.TagWork, "work", "#6A1B9A"),
        tag(DemoSeedIds.TagFamily, "family", "#EF6C00"),
        tag(DemoSeedIds.TagWeekend, "weekend", "#C2185B"),
    )

    val transactions: List<LedgerTransaction> = listOf(
        transaction(DemoSeedIds.TransactionSalary, 320_000, IncomeType, 1_769_878_800_000L, DemoSeedIds.CategorySalary, "Pocket Ledger Co.", "Biweekly paycheck", true),
        transaction(DemoSeedIds.TransactionRent, -145_000, ExpenseType, 1_769_936_400_000L, DemoSeedIds.CategoryRent, "Cedar Apartments", "February rent", true),
        transaction(DemoSeedIds.TransactionGroceriesA, -8_742, ExpenseType, 1_770_055_200_000L, DemoSeedIds.CategoryGroceries, "Neighborhood Market", "Weekly groceries", false),
        transaction(DemoSeedIds.TransactionTransitPass, -9_600, ExpenseType, 1_770_105_600_000L, DemoSeedIds.CategoryTransportation, "Metro Transit", "Monthly pass", true),
        transaction(DemoSeedIds.TransactionCoffee, -625, ExpenseType, 1_770_217_200_000L, DemoSeedIds.CategoryDining, "Bluebird Coffee", "Client catch-up", false),
        transaction(DemoSeedIds.TransactionUtilities, -12_840, ExpenseType, 1_770_321_600_000L, DemoSeedIds.CategoryUtilities, "City Power", "Electric bill", true),
        transaction(DemoSeedIds.TransactionDiningA, -4_875, ExpenseType, 1_770_490_800_000L, DemoSeedIds.CategoryDining, "Noodle House", "Family dinner", false),
        transaction(DemoSeedIds.TransactionMovie, -3_450, ExpenseType, 1_770_584_400_000L, DemoSeedIds.CategoryEntertainment, "Grand Cinema", "Weekend movie", false),
        transaction(DemoSeedIds.TransactionFreelance, 48_000, IncomeType, 1_770_739_200_000L, DemoSeedIds.CategorySalary, "Northstar Studio", "Freelance invoice", false),
        transaction(DemoSeedIds.TransactionGroceriesB, -6_315, ExpenseType, 1_770_915_600_000L, DemoSeedIds.CategoryGroceries, "Fresh Basket", "Produce and pantry", false),
        transaction(DemoSeedIds.TransactionFuel, -4_220, ExpenseType, 1_771_063_200_000L, DemoSeedIds.CategoryTransportation, "Evergreen Fuel", "Fuel top-up", false),
        transaction(DemoSeedIds.TransactionSavings, -35_000, ExpenseType, 1_771_156_800_000L, DemoSeedIds.CategorySavings, "High Yield Savings", "Monthly transfer", true),
        transaction(DemoSeedIds.TransactionDiningB, -2_960, ExpenseType, 1_771_419_600_000L, DemoSeedIds.CategoryDining, "Market Deli", "Team lunch", false),
        transaction(DemoSeedIds.TransactionInternet, -7_900, ExpenseType, 1_771_570_800_000L, DemoSeedIds.CategoryUtilities, "FiberNet", "Internet service", true),
        transaction(DemoSeedIds.TransactionGroceriesC, -7_108, ExpenseType, 1_771_869_600_000L, DemoSeedIds.CategoryGroceries, "Neighborhood Market", "Household basics", false),
        transaction(DemoSeedIds.TransactionConcert, -8_250, ExpenseType, 1_772_229_600_000L, DemoSeedIds.CategoryEntertainment, "Riverside Hall", "Live show", false),
    )

    val transactionTagLinks: List<TransactionTagLink> = listOf(
        TransactionTagLink(DemoSeedIds.TransactionSalary, DemoSeedIds.TagRecurring),
        TransactionTagLink(DemoSeedIds.TransactionSalary, DemoSeedIds.TagWork),
        TransactionTagLink(DemoSeedIds.TransactionRent, DemoSeedIds.TagEssential),
        TransactionTagLink(DemoSeedIds.TransactionRent, DemoSeedIds.TagRecurring),
        TransactionTagLink(DemoSeedIds.TransactionTransitPass, DemoSeedIds.TagEssential),
        TransactionTagLink(DemoSeedIds.TransactionTransitPass, DemoSeedIds.TagRecurring),
        TransactionTagLink(DemoSeedIds.TransactionUtilities, DemoSeedIds.TagEssential),
        TransactionTagLink(DemoSeedIds.TransactionUtilities, DemoSeedIds.TagRecurring),
        TransactionTagLink(DemoSeedIds.TransactionDiningA, DemoSeedIds.TagFamily),
        TransactionTagLink(DemoSeedIds.TransactionMovie, DemoSeedIds.TagWeekend),
        TransactionTagLink(DemoSeedIds.TransactionFreelance, DemoSeedIds.TagWork),
        TransactionTagLink(DemoSeedIds.TransactionSavings, DemoSeedIds.TagRecurring),
        TransactionTagLink(DemoSeedIds.TransactionDiningB, DemoSeedIds.TagWork),
        TransactionTagLink(DemoSeedIds.TransactionInternet, DemoSeedIds.TagEssential),
        TransactionTagLink(DemoSeedIds.TransactionInternet, DemoSeedIds.TagRecurring),
        TransactionTagLink(DemoSeedIds.TransactionConcert, DemoSeedIds.TagWeekend),
    )

    val result = DemoSeedResult(
        categoriesUpserted = categories.size,
        budgetsUpserted = budgets.size,
        tagsUpserted = tags.size,
        transactionsUpserted = transactions.size,
        linksUpserted = transactionTagLinks.size,
    )

    private fun category(
        id: String,
        name: String,
        type: String,
        colorHex: String,
        iconName: String,
        sortOrder: Int,
    ): LedgerCategory = LedgerCategory(
        id = id,
        name = name,
        type = type,
        colorHex = colorHex,
        iconName = iconName,
        sortOrder = sortOrder,
        isActive = true,
        createdAt = CreatedAt,
        updatedAt = CreatedAt,
    )

    private fun budget(
        id: String,
        name: String,
        amountMinor: Long,
        categoryId: String,
    ): LedgerBudget = LedgerBudget(
        id = id,
        name = name,
        amountMinor = amountMinor,
        currencyCode = CurrencyCode,
        periodType = PeriodTypeMonthly,
        periodStart = PeriodStart,
        periodEnd = PeriodEnd,
        categoryId = categoryId,
        isActive = true,
        createdAt = CreatedAt,
        updatedAt = CreatedAt,
    )

    private fun tag(
        id: String,
        name: String,
        colorHex: String,
    ): LedgerTag = LedgerTag(
        id = id,
        name = name,
        colorHex = colorHex,
        createdAt = CreatedAt,
        updatedAt = CreatedAt,
    )

    private fun transaction(
        id: String,
        amountMinor: Long,
        type: String,
        occurredAt: Long,
        categoryId: String,
        merchant: String,
        note: String,
        isRecurring: Boolean,
    ): LedgerTransaction = LedgerTransaction(
        id = id,
        amountMinor = amountMinor,
        currencyCode = CurrencyCode,
        type = type,
        occurredAt = occurredAt,
        categoryId = categoryId,
        merchant = merchant,
        note = note,
        source = Source,
        isRecurring = isRecurring,
        createdAt = CreatedAt,
        updatedAt = CreatedAt,
    )

}
