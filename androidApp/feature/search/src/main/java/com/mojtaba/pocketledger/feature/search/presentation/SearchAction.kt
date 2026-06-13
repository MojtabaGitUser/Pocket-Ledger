package com.mojtaba.pocketledger.feature.search.presentation

import com.mojtaba.pocketledger.core.data.search.SearchAmountRange
import com.mojtaba.pocketledger.core.data.search.SearchDateRange
import com.mojtaba.pocketledger.core.data.search.SearchMode
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType

sealed interface SearchAction {
    data class KeywordChanged(val text: String) : SearchAction
    data class SearchModeSelected(val mode: SearchMode) : SearchAction
    data class TypeFilterChanged(val type: SearchTransactionType?) : SearchAction
    data class CategoryToggled(val categoryId: String) : SearchAction
    data class TagToggled(val tagId: String) : SearchAction
    data class DateRangeChanged(val dateRange: SearchDateRange?) : SearchAction
    data class AmountRangeChanged(val amountRange: SearchAmountRange?) : SearchAction
    data object ClearFiltersClicked : SearchAction
    data class ResultClicked(val transactionId: String) : SearchAction
    data object RetryClicked : SearchAction
}
