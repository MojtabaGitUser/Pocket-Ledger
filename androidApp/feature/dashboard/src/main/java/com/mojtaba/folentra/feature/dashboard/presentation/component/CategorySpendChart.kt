package com.mojtaba.folentra.feature.dashboard.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.mojtaba.folentra.core.designsystem.accessibility.folentraProgressState
import com.mojtaba.folentra.core.designsystem.component.EmptyState
import com.mojtaba.folentra.core.designsystem.theme.FolentraThemeDefaults
import com.mojtaba.folentra.feature.dashboard.model.CategorySpendSummary
import com.mojtaba.folentra.feature.dashboard.presentation.model.DashboardFormatters

@Composable
fun CategorySpendChart(
    categories: List<CategorySpendSummary>,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = "Top spending",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            if (categories.isEmpty()) {
                EmptyState(
                    title = "No category spend",
                    message = "Expense categories will appear here.",
                )
            } else {
                categories.forEach { category ->
                    CategorySpendRow(category = category)
                }
            }
        }
    }
}

@Composable
private fun CategorySpendRow(
    category: CategorySpendSummary,
    modifier: Modifier = Modifier,
) {
    val spacing = FolentraThemeDefaults.spacing
    val percentText = DashboardFormatters.percent(category.percentageOfExpense)
    val amount = DashboardFormatters.formatAmountMinor(category.amountMinor, category.currencyCode)
    val highFontScale = LocalDensity.current.fontScale >= 2f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${category.categoryName}, $amount, $percentText of expenses"
        },
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        if (highFontScale) {
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$amount ($percentText)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "$amount ($percentText)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LinearProgressIndicator(
            progress = { (category.percentageOfExpense / 100.0).coerceIn(0.0, 1.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$percentText of expenses" }
                .folentraProgressState("$percentText of expenses"),
        )
    }
}
