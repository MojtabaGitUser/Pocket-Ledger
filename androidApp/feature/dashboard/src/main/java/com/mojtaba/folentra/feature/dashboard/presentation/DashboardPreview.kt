package com.mojtaba.folentra.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.folentra.core.designsystem.adaptive.FolentraWindowWidthSizeClass
import com.mojtaba.folentra.core.designsystem.theme.FolentraPreviewTheme
import com.mojtaba.folentra.feature.dashboard.presentation.preview.DashboardPreviewFixtures

@Preview(name = "Loading compact", showBackground = true, widthDp = 420)
@Composable
private fun DashboardLoadingPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Loading,
            onAction = {},
        )
    }
}

@Preview(name = "Empty compact", showBackground = true, widthDp = 420)
@Composable
private fun DashboardEmptyPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Empty,
            onAction = {},
        )
    }
}

@Preview(name = "Error compact", showBackground = true, widthDp = 420)
@Composable
private fun DashboardErrorPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Error("The local dashboard summary could not be read."),
            onAction = {},
        )
    }
}

@Preview(name = "Compact content", showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun DashboardPhoneContentPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            widthSizeClass = FolentraWindowWidthSizeClass.Compact,
            onAction = {},
        )
    }
}

@Preview(name = "Compact empty inner lists", showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun DashboardEmptyListsContentPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.emptySummary),
            widthSizeClass = FolentraWindowWidthSizeClass.Compact,
            onAction = {},
        )
    }
}

@Preview(name = "Medium tablet content", showBackground = true, widthDp = 720, heightDp = 900)
@Composable
private fun DashboardMediumContentPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            widthSizeClass = FolentraWindowWidthSizeClass.Medium,
            onAction = {},
        )
    }
}

@Preview(name = "Expanded desktop content", showBackground = true, widthDp = 1280, heightDp = 820)
@Composable
private fun DashboardExpandedContentPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            widthSizeClass = FolentraWindowWidthSizeClass.Expanded,
            onAction = {},
        )
    }
}

@Preview(name = "Landscape content", showBackground = true, widthDp = 900, heightDp = 420)
@Composable
private fun DashboardLandscapeContentPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            widthSizeClass = FolentraWindowWidthSizeClass.Expanded,
            onAction = {},
        )
    }
}

@Preview(name = "Large font content", showBackground = true, widthDp = 420, heightDp = 900, fontScale = 2.0f)
@Composable
private fun DashboardLargeFontContentPreview() {
    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(DashboardPreviewFixtures.summary),
            widthSizeClass = FolentraWindowWidthSizeClass.Compact,
            onAction = {},
        )
    }
}

@Preview(name = "Expanded dense content", showBackground = true, widthDp = 1280, heightDp = 900)
@Composable
private fun DashboardExpandedDenseContentPreview() {
    val summary = DashboardPreviewFixtures.summary.copy(
        topCategories = DashboardPreviewFixtures.summary.topCategories +
            DashboardPreviewFixtures.summary.topCategories.mapIndexed { index, category ->
                category.copy(
                    categoryId = "${category.categoryId}-extra-$index",
                    categoryName = "${category.categoryName} extra",
                )
            },
        budgetProgress = DashboardPreviewFixtures.summary.budgetProgress +
            DashboardPreviewFixtures.summary.budgetProgress.mapIndexed { index, budget ->
                budget.copy(
                    budgetId = "${budget.budgetId}-extra-$index",
                    budgetName = "${budget.budgetName} extra",
                )
            },
        recentTransactions = DashboardPreviewFixtures.summary.recentTransactions +
            DashboardPreviewFixtures.summary.recentTransactions.mapIndexed { index, transaction ->
                transaction.copy(transactionId = "${transaction.transactionId}-extra-$index")
            },
    )

    FolentraPreviewTheme {
        DashboardScreen(
            uiState = DashboardUiState.Content(summary),
            widthSizeClass = FolentraWindowWidthSizeClass.Expanded,
            onAction = {},
        )
    }
}
