package com.mojtaba.pocketledger.core.designsystem.preview

import androidx.compose.runtime.Immutable
import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay

@Immutable
data class PreviewTransaction(
    val title: String,
    val category: String,
    val subtitle: String,
    val amount: AmountDisplay,
)

@Immutable
data class PreviewCategoryBreakdown(
    val category: String,
    val amount: AmountDisplay,
    val percentageLabel: String,
)

object PreviewTransactions {
    val income = PreviewTransaction(
        title = "Monthly paycheck",
        category = PreviewCategories.salary,
        subtitle = PreviewDates.today,
        amount = PreviewAmounts.positive,
    )

    val expense = PreviewTransaction(
        title = "Neighborhood market",
        category = PreviewCategories.food,
        subtitle = PreviewDates.yesterday,
        amount = PreviewAmounts.negative,
    )

    val pending = PreviewTransaction(
        title = "Card authorization",
        category = PreviewCategories.shopping,
        subtitle = PreviewDates.pending,
        amount = PreviewAmounts.pending,
    )

    val longTitle = PreviewTransaction(
        title = PreviewText.longTitle,
        category = PreviewCategories.savings,
        subtitle = PreviewDates.recent,
        amount = PreviewAmounts.large,
    )

    val empty = emptyList<PreviewTransaction>()

    val recent = listOf(
        income,
        expense,
        pending,
        PreviewTransaction(
            title = "Apartment rent",
            category = PreviewCategories.rent,
            subtitle = PreviewDates.recent,
            amount = PreviewAmounts.negative.copy(
                text = "-$1,850.00",
                contentDescription = "1,850 dollars rent expense",
            ),
        ),
        PreviewTransaction(
            title = "Transit pass",
            category = PreviewCategories.transport,
            subtitle = PreviewDates.older,
            amount = PreviewAmounts.negative.copy(
                text = "-$98.00",
                contentDescription = "98 dollars transport expense",
            ),
        ),
    )

    val categoryBreakdown = listOf(
        PreviewCategoryBreakdown(
            category = PreviewCategories.rent,
            amount = PreviewAmounts.negative.copy(text = "-$1,850.00"),
            percentageLabel = "42%",
        ),
        PreviewCategoryBreakdown(
            category = PreviewCategories.food,
            amount = PreviewAmounts.negative.copy(text = "-$486.25"),
            percentageLabel = "18%",
        ),
        PreviewCategoryBreakdown(
            category = PreviewCategories.savings,
            amount = PreviewAmounts.positive.copy(text = "$750.00"),
            percentageLabel = "15%",
        ),
    )
}
