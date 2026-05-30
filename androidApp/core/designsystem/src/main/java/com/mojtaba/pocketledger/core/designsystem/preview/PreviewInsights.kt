package com.mojtaba.pocketledger.core.designsystem.preview

import androidx.compose.runtime.Immutable

@Immutable
data class PreviewInsight(
    val title: String,
    val message: String,
    val trendLabel: String,
)

object PreviewInsights {
    val spendingIncreased = PreviewInsight(
        title = "Spending increased",
        message = "Food spending is higher than your recent average.",
        trendLabel = "+12%",
    )

    val spendingDecreased = PreviewInsight(
        title = "Spending decreased",
        message = "Transport spending is lower than last month.",
        trendLabel = "-8%",
    )

    val neutral = PreviewInsight(
        title = "Stable month",
        message = "Your total spending is close to your usual range.",
        trendLabel = "0%",
    )

    val all = listOf(spendingIncreased, spendingDecreased, neutral)
}
