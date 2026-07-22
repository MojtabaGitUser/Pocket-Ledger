package com.mojtaba.folentra.screenshot

import com.mojtaba.folentra.core.data.search.SearchFilters
import com.mojtaba.folentra.core.data.search.SearchQuery
import com.mojtaba.folentra.core.data.search.SearchTransactionType
import com.mojtaba.folentra.core.designsystem.component.AmountDisplay
import com.mojtaba.folentra.core.designsystem.component.AmountTone
import com.mojtaba.folentra.feature.search.presentation.CategoryFilterUiModel
import com.mojtaba.folentra.feature.search.presentation.SearchCapabilities
import com.mojtaba.folentra.feature.search.presentation.SearchResultUiModel
import com.mojtaba.folentra.feature.search.presentation.SearchUiState
import com.mojtaba.folentra.feature.search.presentation.TagFilterUiModel
import com.mojtaba.folentra.feature.transaction.model.TransactionDetailUiModel
import com.mojtaba.folentra.feature.transaction.model.TransactionListItemUiModel
import com.mojtaba.folentra.feature.transaction.presentation.detail.TransactionDetailUiState
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListUiState

object ScreenshotTestData {
    val transactionListItems = listOf(
        transactionListItem(
            id = "transaction-paycheck",
            title = "Monthly paycheck",
            amount = AmountDisplay("+$4,850.00", AmountTone.Positive, "$4,850.00 income"),
            typeLabel = "Income",
            categoryLabel = "Salary",
            dateLabel = "Nov 30, 2023",
            notePreview = "Primary income",
            tagLabels = listOf("Recurring"),
        ),
        transactionListItem(
            id = "transaction-rent",
            title = "Rent payment",
            amount = AmountDisplay("-$1,250.00", AmountTone.Negative, "$1,250.00 expense"),
            categoryLabel = "Housing",
            dateLabel = "Nov 1, 2023",
            notePreview = "Apartment",
        ),
        transactionListItem(
            id = "transaction-groceries",
            title = "Neighborhood Market",
            amount = AmountDisplay("-$86.32", AmountTone.Negative, "$86.32 expense"),
            categoryLabel = "Food and dining",
            dateLabel = "Nov 15, 2023",
            notePreview = "Weekly groceries",
            tagLabels = listOf("Essential"),
        ),
        transactionListItem(
            id = "transaction-coffee",
            title = "Coffee Shop",
            amount = AmountDisplay("-$42.50", AmountTone.Negative, "$42.50 expense"),
            categoryLabel = "Food and dining",
            dateLabel = "Nov 14, 2023",
            notePreview = "Team breakfast",
            tagLabels = listOf("Work", "Client"),
        ),
        transactionListItem(
            id = "transaction-transport",
            title = "Transit card",
            amount = AmountDisplay("-$48.00", AmountTone.Negative, "$48.00 expense"),
            categoryLabel = "Transportation",
            dateLabel = "Nov 12, 2023",
            notePreview = "Monthly pass",
        ),
        transactionListItem(
            id = "transaction-utilities",
            title = "Utility bill",
            amount = AmountDisplay("-$132.18", AmountTone.Negative, "$132.18 expense"),
            categoryLabel = "Utilities",
            dateLabel = "Nov 8, 2023",
            notePreview = "Electricity",
        ),
    )

    val transactionListContent = TransactionListUiState.Content(transactionListItems)
    val selectedTransactionId = "transaction-groceries"

    val transactionDetail = TransactionDetailUiState.Content(
        TransactionDetailUiModel(
            id = selectedTransactionId,
            amount = AmountDisplay("-$86.32", AmountTone.Negative, "$86.32 expense"),
            typeLabel = "Expense",
            categoryLabel = "Food and dining",
            dateLabel = "Nov 15, 2023",
            merchantLabel = "Neighborhood Market",
            noteLabel = "Weekly groceries and pantry restock",
            tagLabels = listOf("Essential", "Household"),
            createdAtLabel = "Nov 15, 2023, 6:30 PM",
            updatedAtLabel = "Nov 15, 2023, 6:45 PM",
        ),
    )

    val populatedSearchState = SearchUiState(
        query = SearchQuery(text = "coffee"),
        keywordInput = "coffee",
        results = listOf(
            SearchResultUiModel(
                transactionId = "transaction-coffee",
                title = "Coffee Shop",
                amount = AmountDisplay("-$42.50", AmountTone.Negative, "$42.50 expense"),
                typeLabel = "Expense",
                categoryLabel = "Food and dining",
                dateLabel = "Nov 14, 2023",
                notePreview = "Team breakfast",
                tagLabels = listOf("Work", "Client"),
                contentDescription = "Transaction Coffee Shop, Expense, Food and dining, Nov 14, 2023, $42.50 expense",
            ),
            SearchResultUiModel(
                transactionId = "transaction-groceries",
                title = "Neighborhood Market",
                amount = AmountDisplay("-$86.32", AmountTone.Negative, "$86.32 expense"),
                typeLabel = "Expense",
                categoryLabel = "Food and dining",
                dateLabel = "Nov 15, 2023",
                notePreview = "Weekly groceries",
                tagLabels = listOf("Essential"),
                contentDescription = "Transaction Neighborhood Market, Expense, Food and dining, Nov 15, 2023, $86.32 expense",
            ),
        ),
        categories = listOf(
            CategoryFilterUiModel("food", "Food and dining", true),
            CategoryFilterUiModel("transport", "Transportation", false),
            CategoryFilterUiModel("housing", "Housing", false),
        ),
        tags = listOf(
            TagFilterUiModel("work", "Work", true),
            TagFilterUiModel("essential", "Essential", false),
        ),
        isLoading = false,
    )

    val initialSearchState = populatedSearchState.copy(
        query = SearchQuery(),
        keywordInput = "",
        results = emptyList(),
        categories = populatedSearchState.categories.map { it.copy(selected = false) },
        tags = populatedSearchState.tags.map { it.copy(selected = false) },
        isLoading = false,
    )

    val emptyLedgerSearchState = initialSearchState.copy(isEmptyLedger = true)

    val noResultsSearchState = populatedSearchState.copy(
        query = SearchQuery(
            text = "subscription",
            filters = SearchFilters(transactionTypes = setOf(SearchTransactionType.Expense)),
        ),
        keywordInput = "subscription",
        results = emptyList(),
        isLoading = false,
    )

    val filteredSearchState = populatedSearchState.copy(
        capabilities = SearchCapabilities(semanticSearchVisible = true),
    )

    private fun transactionListItem(
        id: String,
        title: String,
        amount: AmountDisplay,
        typeLabel: String = "Expense",
        categoryLabel: String,
        dateLabel: String,
        notePreview: String? = null,
        tagLabels: List<String> = emptyList(),
    ): TransactionListItemUiModel =
        TransactionListItemUiModel(
            id = id,
            amount = amount,
            typeLabel = typeLabel,
            categoryLabel = categoryLabel,
            dateLabel = dateLabel,
            title = title,
            notePreview = notePreview,
            tagLabels = tagLabels,
            tone = amount.tone,
        )
}
