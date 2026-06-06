package com.mojtaba.pocketledger.feature.search.presentation.preview

import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay
import com.mojtaba.pocketledger.core.designsystem.component.AmountTone
import com.mojtaba.pocketledger.feature.search.presentation.CategoryFilterUiModel
import com.mojtaba.pocketledger.feature.search.presentation.SearchResultUiModel
import com.mojtaba.pocketledger.feature.search.presentation.SearchUiState
import com.mojtaba.pocketledger.feature.search.presentation.TagFilterUiModel

internal object SearchPreviewFixtures {
    val contentState = SearchUiState(
        keywordInput = "coffee",
        results = listOf(
            SearchResultUiModel(
                transactionId = "preview-transaction",
                title = "Coffee Shop",
                amount = AmountDisplay("-${'$'}4.50", AmountTone.Negative, "-${'$'}4.50 expense"),
                typeLabel = "Expense",
                categoryLabel = "Food",
                dateLabel = "Nov 14, 2023",
                notePreview = "Team breakfast",
                tagLabels = listOf("Work"),
                contentDescription = "Transaction Coffee Shop, Expense, Food, Nov 14, 2023, -${'$'}4.50 expense",
            ),
        ),
        categories = listOf(CategoryFilterUiModel("food", "Food", true)),
        tags = listOf(TagFilterUiModel("work", "Work", false)),
        isLoading = false,
    )
}
