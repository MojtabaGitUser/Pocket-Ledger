package com.mojtaba.folentra.core.data.seed

import com.mojtaba.folentra.core.data.model.LedgerCategory

object DefaultCategoryCatalog {
    const val IncomeType = "income"
    const val ExpenseType = "expense"

    const val CategorySalary = "default-category-salary"
    const val CategoryGroceries = "default-category-groceries"
    const val CategoryRent = "default-category-rent"
    const val CategoryTransportation = "default-category-transportation"
    const val CategoryDining = "default-category-dining"
    const val CategoryUtilities = "default-category-utilities"
    const val CategoryEntertainment = "default-category-entertainment"
    const val CategorySavings = "default-category-savings"

    fun categories(createdAtMillis: Long): List<LedgerCategory> = listOf(
        category(CategorySalary, "Salary / Income", IncomeType, "#2E7D32", "payments", 10, createdAtMillis),
        category(CategoryGroceries, "Groceries", ExpenseType, "#1565C0", "shopping_cart", 20, createdAtMillis),
        category(CategoryRent, "Rent", ExpenseType, "#6A1B9A", "home", 30, createdAtMillis),
        category(
            CategoryTransportation,
            "Transportation",
            ExpenseType,
            "#00838F",
            "directions_car",
            40,
            createdAtMillis,
        ),
        category(CategoryDining, "Dining", ExpenseType, "#EF6C00", "restaurant", 50, createdAtMillis),
        category(CategoryUtilities, "Utilities", ExpenseType, "#455A64", "bolt", 60, createdAtMillis),
        category(CategoryEntertainment, "Entertainment", ExpenseType, "#C2185B", "theaters", 70, createdAtMillis),
        category(CategorySavings, "Savings", ExpenseType, "#558B2F", "savings", 80, createdAtMillis),
    )

    private fun category(
        id: String,
        name: String,
        type: String,
        colorHex: String,
        iconName: String,
        sortOrder: Int,
        createdAtMillis: Long,
    ): LedgerCategory = LedgerCategory(
        id = id,
        name = name,
        type = type,
        colorHex = colorHex,
        iconName = iconName,
        sortOrder = sortOrder,
        isActive = true,
        createdAt = createdAtMillis,
        updatedAt = createdAtMillis,
    )
}
