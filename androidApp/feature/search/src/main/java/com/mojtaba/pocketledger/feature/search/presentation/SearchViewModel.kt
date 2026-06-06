package com.mojtaba.pocketledger.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.search.SearchFilters
import com.mojtaba.pocketledger.core.data.search.SearchQuery
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {
    private val query = MutableStateFlow(SearchQuery())
    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<SearchUiState> = refreshRequests
        .flatMapLatest { observeUiState() }
        .onStart { emit(SearchUiState(isLoading = true)) }
        .catch { throwable ->
            emit(
                SearchUiState(
                    query = query.value,
                    keywordInput = query.value.text,
                    isLoading = false,
                    errorMessage = throwable.message ?: "Unable to search transactions.",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState(isLoading = true),
        )

    private val _effects = MutableSharedFlow<SearchEffect>()
    val effects: SharedFlow<SearchEffect> = _effects.asSharedFlow()

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.KeywordChanged -> updateQuery { current ->
                current.copy(text = action.text.take(SearchQuery.MAX_TEXT_LENGTH))
            }
            is SearchAction.TypeFilterChanged -> updateQuery { current ->
                current.copy(
                    filters = current.filters.copy(
                        transactionTypes = action.type?.let(::setOf).orEmpty(),
                    ),
                )
            }
            is SearchAction.CategoryToggled -> updateQuery { current ->
                current.copy(
                    filters = current.filters.copy(
                        categoryIds = current.filters.categoryIds.toggle(action.categoryId),
                    ),
                )
            }
            is SearchAction.TagToggled -> updateQuery { current ->
                current.copy(
                    filters = current.filters.copy(
                        tagIds = current.filters.tagIds.toggle(action.tagId),
                    ),
                )
            }
            is SearchAction.DateRangeChanged -> updateQuery { current ->
                current.copy(filters = current.filters.copy(dateRange = action.dateRange))
            }
            is SearchAction.AmountRangeChanged -> updateQuery { current ->
                current.copy(filters = current.filters.copy(amountRange = action.amountRange))
            }
            SearchAction.ClearFiltersClicked -> query.value = SearchQuery()
            is SearchAction.ResultClicked -> {
                viewModelScope.launch {
                    _effects.emit(SearchEffect.OpenTransaction(action.transactionId))
                }
            }
            SearchAction.RetryClicked -> refreshRequests.update { it + 1 }
        }
    }

    private fun updateQuery(reducer: (SearchQuery) -> SearchQuery) {
        query.update { reducer(it).normalized() }
    }

    private fun observeUiState(): Flow<SearchUiState> {
        val categories = categoryRepository.observeActiveCategories()
        val tags = tagRepository.observeTags()
        val hasAnyTransactions = transactionRepository.observeRecentTransactions(limit = 1)
            .map { it.isNotEmpty() }

        val searchResults = query.flatMapLatest { searchQuery ->
            transactionRepository.searchTransactions(searchQuery).withTags()
        }

        return combine(
            query,
            searchResults,
            categories,
            tags,
            hasAnyTransactions,
        ) { searchQuery, transactionTagPairs, categoryOptions, tagOptions, hasTransactions ->
            val categoriesById = categoryOptions.associateBy { it.id }
            SearchUiState(
                query = searchQuery,
                keywordInput = searchQuery.text,
                results = transactionTagPairs.map { (transaction, transactionTags) ->
                    SearchResultMapper.map(
                        transaction = transaction,
                        category = categoriesById[transaction.categoryId],
                        tags = transactionTags,
                    )
                },
                categories = categoryOptions.map { category ->
                    CategoryFilterUiModel(
                        id = category.id,
                        name = category.name,
                        selected = category.id in searchQuery.filters.categoryIds,
                    )
                },
                tags = tagOptions.map { tag ->
                    TagFilterUiModel(
                        id = tag.id,
                        name = tag.name,
                        selected = tag.id in searchQuery.filters.tagIds,
                    )
                },
                isLoading = false,
                isEmptyLedger = !hasTransactions,
                errorMessage = null,
            )
        }
    }

    private fun Flow<List<LedgerTransaction>>.withTags(): Flow<List<Pair<LedgerTransaction, List<LedgerTag>>>> =
        flatMapLatest { transactions ->
            if (transactions.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    transactions.map { transaction ->
                        tagRepository.observeTagsForTransaction(transaction.id).map { tags ->
                            transaction to tags
                        }
                    },
                ) { pairs -> pairs.toList() }
            }
        }
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value
