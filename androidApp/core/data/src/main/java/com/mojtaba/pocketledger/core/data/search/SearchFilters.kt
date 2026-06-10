package com.mojtaba.pocketledger.core.data.search

data class SearchFilters(
    val transactionTypes: Set<SearchTransactionType> = emptySet(),
    val categoryIds: Set<String> = emptySet(),
    val tagIds: Set<String> = emptySet(),
    val dateRange: SearchDateRange? = null,
    val amountRange: SearchAmountRange? = null,
    val currencyCode: String? = null,
    val recurring: SearchRecurringFilter = SearchRecurringFilter.Any,
) {
    val isEmpty: Boolean
        get() = transactionTypes.isEmpty() &&
            categoryIds.isEmpty() &&
            tagIds.isEmpty() &&
            dateRange?.isEmpty != false &&
            amountRange?.isEmpty != false &&
            currencyCode.isNullOrBlank() &&
            recurring == SearchRecurringFilter.Any
}
