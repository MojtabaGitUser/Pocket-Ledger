package com.mojtaba.pocketledger.core.designsystem.preview

import androidx.compose.runtime.Immutable

@Immutable
data class PreviewBudget(
    val name: String,
    val spentLabel: String,
    val limitLabel: String,
    val progress: Float,
    val statusLabel: String,
)

object PreviewBudgets {
    val healthy = PreviewBudget(
        name = "Groceries",
        spentLabel = "$320 spent",
        limitLabel = "$600 limit",
        progress = 0.53f,
        statusLabel = "On track",
    )

    val warning = PreviewBudget(
        name = "Entertainment",
        spentLabel = "$265 spent",
        limitLabel = "$300 limit",
        progress = 0.88f,
        statusLabel = "Near limit",
    )

    val exceeded = PreviewBudget(
        name = "Shopping",
        spentLabel = "$425 spent",
        limitLabel = "$350 limit",
        progress = 1f,
        statusLabel = "Over budget",
    )

    val all = listOf(healthy, warning, exceeded)
}
