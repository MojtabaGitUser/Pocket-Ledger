package com.mojtaba.pocketledger.feature.search.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.pocketledger.core.ai.AiCapability
import com.mojtaba.pocketledger.core.ai.AiFallbackStrategy
import com.mojtaba.pocketledger.core.ai.AiInferenceResult
import com.mojtaba.pocketledger.core.ai.AiProviderSelector
import com.mojtaba.pocketledger.core.ai.SemanticSearchDocument
import com.mojtaba.pocketledger.core.ai.SemanticSearchRequest
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.LedgerTransaction
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository
import com.mojtaba.pocketledger.core.data.search.SearchAmountRange
import com.mojtaba.pocketledger.core.data.search.SearchDateRange
import com.mojtaba.pocketledger.core.data.search.SearchFilters
import com.mojtaba.pocketledger.core.data.search.SearchMode
import com.mojtaba.pocketledger.core.data.search.SearchQuery
import com.mojtaba.pocketledger.core.data.search.SearchRecurringFilter
import com.mojtaba.pocketledger.core.data.search.SearchSort
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType
import com.mojtaba.pocketledger.core.featureflags.DefaultFeatureFlags
import com.mojtaba.pocketledger.core.featureflags.FeatureFlagEvaluator
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
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val featureFlags: FeatureFlagEvaluator,
    private val aiProviderSelector: AiProviderSelector,
    private val aiFallbackStrategy: AiFallbackStrategy,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query = MutableStateFlow(savedStateHandle.restoredSearchQuery())
    private val refreshRequests = MutableStateFlow(0)
    private val modeUnavailableMessage = MutableStateFlow<String?>(null)

    private val capabilities: SearchCapabilities
        get() = SearchCapabilities(
            semanticSearchVisible = featureFlags.isEnabled(DefaultFeatureFlags.SemanticSearchEnabled),
            semanticSearchAvailable = aiProviderSelector.isAvailable(AiCapability.SemanticSearch),
        )

    val uiState: StateFlow<SearchUiState> = refreshRequests
        .flatMapLatest { observeUiState() }
        .onStart { emit(SearchUiState(isLoading = true)) }
        .catch { throwable ->
            emit(
                SearchUiState(
                    query = query.value,
                    capabilities = capabilities,
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
            is SearchAction.SearchModeSelected -> selectSearchMode(action.mode)
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
            SearchAction.ClearFiltersClicked -> {
                modeUnavailableMessage.value = null
                setQuery(SearchQuery())
            }
            is SearchAction.ResultClicked -> {
                viewModelScope.launch {
                    _effects.emit(SearchEffect.OpenTransaction(action.transactionId))
                }
            }
            SearchAction.RetryClicked -> refreshRequests.update { it + 1 }
        }
    }

    private fun updateQuery(reducer: (SearchQuery) -> SearchQuery) {
        modeUnavailableMessage.value = null
        setQuery(reducer(query.value))
    }

    private fun setQuery(nextQuery: SearchQuery) {
        val normalized = nextQuery.normalized()
        savedStateHandle.saveSearchQuery(normalized)
        query.value = normalized
    }

    private fun selectSearchMode(mode: SearchMode) {
        when (mode) {
            SearchMode.Keyword -> {
                modeUnavailableMessage.value = null
                setQuery(query.value.copy(mode = SearchMode.Keyword))
            }
            SearchMode.Semantic -> {
                if (capabilities.semanticSearchAvailable) {
                    setQuery(query.value.copy(mode = SearchMode.Semantic))
                } else {
                    modeUnavailableMessage.value = "Semantic search is not available on this device."
                    setQuery(query.value.copy(mode = SearchMode.Keyword))
                }
            }
        }
    }

    private fun observeUiState(): Flow<SearchUiState> {
        val categories = categoryRepository.observeActiveCategories()
        val tags = tagRepository.observeTags()
        val hasAnyTransactions = transactionRepository.observeRecentTransactions(limit = 1)
            .map { it.isNotEmpty() }
        val searchState = combine(query, modeUnavailableMessage) { searchQuery, unavailableMessage ->
            SearchQueryState(
                query = searchQuery,
                modeUnavailableMessage = unavailableMessage,
            )
        }

        val searchResults = query.flatMapLatest(::observeSearchResults)

        return combine(
            searchState,
            searchResults,
            categories,
            tags,
            hasAnyTransactions,
        ) { currentSearchState, transactionTagPairs, categoryOptions, tagOptions, hasTransactions ->
            val searchQuery = currentSearchState.query
            val categoriesById = categoryOptions.associateBy { it.id }
            SearchUiState(
                query = searchQuery,
                capabilities = capabilities,
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
                modeUnavailableMessage = currentSearchState.modeUnavailableMessage,
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

    private fun observeSearchResults(searchQuery: SearchQuery): Flow<List<Pair<LedgerTransaction, List<LedgerTag>>>> {
        val executableQuery = when (searchQuery.mode) {
            SearchMode.Keyword -> searchQuery.copy(mode = SearchMode.Keyword).normalized()
            SearchMode.Semantic -> searchQuery.copy(text = "", mode = SearchMode.Keyword).normalized()
        }
        val localCandidates = transactionRepository.searchTransactions(executableQuery).withTags()
        return if (searchQuery.mode == SearchMode.Semantic) {
            localCandidates.transformLatest { candidates ->
                emit(rankSemanticResults(searchQuery.text, candidates))
            }
        } else {
            localCandidates
        }
    }

    private suspend fun rankSemanticResults(
        text: String,
        candidates: List<Pair<LedgerTransaction, List<LedgerTag>>>,
    ): List<Pair<LedgerTransaction, List<LedgerTag>>> {
        if (text.isBlank() || candidates.isEmpty()) {
            return candidates
        }

        val candidatesById = candidates.associateBy { (transaction, _) -> transaction.id }
        val request = SemanticSearchRequest(
            query = text,
            documents = candidates.map { (transaction, tags) -> transaction.toSemanticDocument(tags) },
        )
        val rankedIds = when (val result = aiFallbackStrategy.semanticSearch(request)) {
            is AiInferenceResult.Success -> result.value.rankedIds
            is AiInferenceResult.Unavailable,
            is AiInferenceResult.Failure,
            -> emptyList()
        }
        return rankedIds.mapNotNull(candidatesById::get)
    }

    private fun LedgerTransaction.toSemanticDocument(tags: List<LedgerTag>): SemanticSearchDocument =
        SemanticSearchDocument(
            id = id,
            title = merchant.orEmpty(),
            body = listOfNotNull(note, source, type, currencyCode, tags.joinToString(" ") { it.name })
                .filter { it.isNotBlank() }
                .joinToString(" "),
            metadata = mapOf(
                "type" to type,
                "currency" to currencyCode,
            ),
        )

}


private object SearchSavedStateKeys {
    const val TEXT = "search.query.text"
    const val MODE = "search.query.mode"
    const val TRANSACTION_TYPES = "search.query.transactionTypes"
    const val CATEGORY_IDS = "search.query.categoryIds"
    const val TAG_IDS = "search.query.tagIds"
    const val DATE_START = "search.query.dateStart"
    const val DATE_END = "search.query.dateEnd"
    const val AMOUNT_MIN = "search.query.amountMin"
    const val AMOUNT_MAX = "search.query.amountMax"
    const val CURRENCY = "search.query.currency"
    const val RECURRING = "search.query.recurring"
    const val SORT = "search.query.sort"
}
private data class SearchQueryState(
    val query: SearchQuery,
    val modeUnavailableMessage: String?,
)

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value

private fun SavedStateHandle.restoredSearchQuery(): SearchQuery {
    val dateRange = SearchDateRange(
        startMillis = this[SearchSavedStateKeys.DATE_START],
        endMillis = this[SearchSavedStateKeys.DATE_END],
    ).takeUnless { it.isEmpty }
    val amountRange = SearchAmountRange(
        minMinor = this[SearchSavedStateKeys.AMOUNT_MIN],
        maxMinor = this[SearchSavedStateKeys.AMOUNT_MAX],
    ).takeUnless { it.isEmpty }
    return SearchQuery(
        text = this[SearchSavedStateKeys.TEXT] ?: "",
        mode = enumValueOrDefault(this[SearchSavedStateKeys.MODE], SearchMode.Keyword),
        filters = SearchFilters(
            transactionTypes = restoredStringList(SearchSavedStateKeys.TRANSACTION_TYPES)
                .mapNotNull { enumValueOrNull<SearchTransactionType>(it) }
                .toSet(),
            categoryIds = restoredStringList(SearchSavedStateKeys.CATEGORY_IDS).toSet(),
            tagIds = restoredStringList(SearchSavedStateKeys.TAG_IDS).toSet(),
            dateRange = dateRange,
            amountRange = amountRange,
            currencyCode = this[SearchSavedStateKeys.CURRENCY],
            recurring = enumValueOrDefault(this[SearchSavedStateKeys.RECURRING], SearchRecurringFilter.Any),
        ),
        sort = enumValueOrDefault(this[SearchSavedStateKeys.SORT], SearchSort.DateDescending),
    ).normalized()
}

private fun SavedStateHandle.saveSearchQuery(query: SearchQuery) {
    this[SearchSavedStateKeys.TEXT] = query.text
    this[SearchSavedStateKeys.MODE] = query.mode.name
    this[SearchSavedStateKeys.TRANSACTION_TYPES] = ArrayList(query.filters.transactionTypes.map { it.name }.sorted())
    this[SearchSavedStateKeys.CATEGORY_IDS] = ArrayList(query.filters.categoryIds.sorted())
    this[SearchSavedStateKeys.TAG_IDS] = ArrayList(query.filters.tagIds.sorted())
    this[SearchSavedStateKeys.DATE_START] = query.filters.dateRange?.startMillis
    this[SearchSavedStateKeys.DATE_END] = query.filters.dateRange?.endMillis
    this[SearchSavedStateKeys.AMOUNT_MIN] = query.filters.amountRange?.minMinor
    this[SearchSavedStateKeys.AMOUNT_MAX] = query.filters.amountRange?.maxMinor
    this[SearchSavedStateKeys.CURRENCY] = query.filters.currencyCode
    this[SearchSavedStateKeys.RECURRING] = query.filters.recurring.name
    this[SearchSavedStateKeys.SORT] = query.sort.name
}

private fun SavedStateHandle.restoredStringList(key: String): List<String> =
    get<ArrayList<String>>(key).orEmpty()

private inline fun <reified T : Enum<T>> enumValueOrDefault(
    rawValue: String?,
    defaultValue: T,
): T = enumValueOrNull<T>(rawValue) ?: defaultValue

private inline fun <reified T : Enum<T>> enumValueOrNull(rawValue: String?): T? =
    rawValue?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
