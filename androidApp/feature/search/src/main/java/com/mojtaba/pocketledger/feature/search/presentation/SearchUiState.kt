package com.mojtaba.pocketledger.feature.search.presentation

import androidx.compose.runtime.Immutable
import com.mojtaba.pocketledger.core.data.search.SearchQuery

@Immutable
data class SearchUiState(
    val query: SearchQuery = SearchQuery(),
    val keywordInput: String = "",
    val results: List<SearchResultUiModel> = emptyList(),
    val categories: List<CategoryFilterUiModel> = emptyList(),
    val tags: List<TagFilterUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isEmptyLedger: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasNoResults: Boolean
        get() = !isLoading && errorMessage == null && !isEmptyLedger && results.isEmpty()

    val canClearFilters: Boolean
        get() = !query.isEmpty || keywordInput.isNotBlank()
}

@Immutable
data class CategoryFilterUiModel(
    val id: String,
    val name: String,
    val selected: Boolean,
)

@Immutable
data class TagFilterUiModel(
    val id: String,
    val name: String,
    val selected: Boolean,
)
