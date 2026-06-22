package com.mojtaba.pocketledger.feature.search.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.data.search.SearchAmountRange
import com.mojtaba.pocketledger.core.data.search.SearchDateRange
import com.mojtaba.pocketledger.core.data.search.SearchMode
import com.mojtaba.pocketledger.core.data.search.SearchTransactionType
import com.mojtaba.pocketledger.core.designsystem.accessibility.pocketLedgerHeading
import com.mojtaba.pocketledger.core.designsystem.accessibility.pocketLedgerSelectedState
import com.mojtaba.pocketledger.core.designsystem.component.AdaptiveContainer
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState
import com.mojtaba.pocketledger.core.designsystem.component.ErrorState
import com.mojtaba.pocketledger.core.designsystem.component.LoadingState
import com.mojtaba.pocketledger.core.designsystem.component.TransactionRow
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults
import com.mojtaba.pocketledger.feature.search.presentation.preview.SearchPreviewFixtures
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search",
                        modifier = Modifier.pocketLedgerHeading(),
                    )
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { contentPadding ->
        AdaptiveContainer(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            maxWidth = 1040.dp,
        ) {
            SearchContent(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SearchContent(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing

    Column(
        modifier = modifier.padding(horizontal = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        SearchTextField(
            value = uiState.keywordInput,
            onValueChange = { onAction(SearchAction.KeywordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
        SearchFilterBar(
            uiState = uiState,
            onAction = onAction,
            modifier = Modifier.fillMaxWidth(),
        )
        SearchStateContent(
            uiState = uiState,
            onAction = onAction,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Keyword") },
        singleLine = true,
        modifier = modifier.semantics {
            contentDescription = "Search transactions by keyword"
        },
    )
}

@Composable
private fun SearchFilterBar(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    val dateOptions = remember { DateFilterOption.defaults() }
    val amountOptions = remember { AmountFilterOption.defaults() }

    LazyRow(
        modifier = modifier.semantics {
            contentDescription = "Search filters"
        },
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        contentPadding = PaddingValues(vertical = spacing.extraSmall),
    ) {
        item {
            val selected = uiState.query.mode == SearchMode.Keyword
            FilterChip(
                selected = selected,
                onClick = { onAction(SearchAction.SearchModeSelected(SearchMode.Keyword)) },
                label = { Text("Keyword") },
                enabled = uiState.capabilities.keywordSearchAvailable,
                modifier = Modifier
                    .semantics { contentDescription = "Keyword search" }
                    .pocketLedgerSelectedState(selected),
            )
        }
        if (uiState.capabilities.semanticSearchVisible) {
            item {
                val semanticAvailable = uiState.capabilities.semanticSearchAvailable
                val selected = uiState.query.mode == SearchMode.Semantic
                FilterChip(
                    selected = selected,
                    onClick = { onAction(SearchAction.SearchModeSelected(SearchMode.Semantic)) },
                    label = { Text("Semantic") },
                    enabled = semanticAvailable,
                    modifier = Modifier
                        .semantics {
                            contentDescription = "Semantic search, coming soon"
                            stateDescription = if (semanticAvailable) {
                                if (selected) "Selected" else "Not selected"
                            } else {
                                "Disabled, coming soon"
                            }
                        },
                )
            }
        }
        item {
            val selected = uiState.query.filters.transactionTypes.isEmpty()
            FilterChip(
                selected = selected,
                onClick = { onAction(SearchAction.TypeFilterChanged(null)) },
                label = { Text("All") },
                modifier = Modifier
                    .semantics { contentDescription = "Filter by all transaction types" }
                    .pocketLedgerSelectedState(selected),
            )
        }
        item {
            TypeFilterChip(
                label = "Income",
                type = SearchTransactionType.Income,
                selected = SearchTransactionType.Income in uiState.query.filters.transactionTypes,
                onAction = onAction,
            )
        }
        item {
            TypeFilterChip(
                label = "Expense",
                type = SearchTransactionType.Expense,
                selected = SearchTransactionType.Expense in uiState.query.filters.transactionTypes,
                onAction = onAction,
            )
        }
        items(uiState.categories, key = { "category-${it.id}" }) { category ->
            FilterChip(
                selected = category.selected,
                onClick = { onAction(SearchAction.CategoryToggled(category.id)) },
                label = { Text(category.name) },
                modifier = Modifier
                    .semantics {
                        contentDescription = "Filter by category ${category.name}"
                    }
                    .pocketLedgerSelectedState(category.selected),
            )
        }
        items(uiState.tags, key = { "tag-${it.id}" }) { tag ->
            FilterChip(
                selected = tag.selected,
                onClick = { onAction(SearchAction.TagToggled(tag.id)) },
                label = { Text("#${tag.name}") },
                modifier = Modifier
                    .semantics {
                        contentDescription = "Filter by tag ${tag.name}"
                    }
                    .pocketLedgerSelectedState(tag.selected),
            )
        }
        items(dateOptions, key = { "date-${it.label}" }) { option ->
            val selected = uiState.query.filters.dateRange == option.range
            FilterChip(
                selected = selected,
                onClick = { onAction(SearchAction.DateRangeChanged(option.range)) },
                label = { Text(option.label) },
                modifier = Modifier
                    .semantics {
                        contentDescription = "Filter by date ${option.label}"
                    }
                    .pocketLedgerSelectedState(selected),
            )
        }
        items(amountOptions, key = { "amount-${it.label}" }) { option ->
            val selected = uiState.query.filters.amountRange == option.range
            FilterChip(
                selected = selected,
                onClick = { onAction(SearchAction.AmountRangeChanged(option.range)) },
                label = { Text(option.label) },
                modifier = Modifier
                    .semantics {
                        contentDescription = "Filter by amount ${option.label}"
                    }
                    .pocketLedgerSelectedState(selected),
            )
        }
        item {
            TextButton(
                enabled = uiState.canClearFilters,
                onClick = { onAction(SearchAction.ClearFiltersClicked) },
                modifier = Modifier.semantics {
                    contentDescription = "Clear search filters"
                    stateDescription = if (uiState.canClearFilters) {
                        "Enabled"
                    } else {
                        "Disabled until filters are applied"
                    }
                },
            ) {
                Text("Clear")
            }
        }
    }
}

@Composable
private fun TypeFilterChip(
    label: String,
    type: SearchTransactionType,
    selected: Boolean,
    onAction: (SearchAction) -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = { onAction(SearchAction.TypeFilterChanged(type)) },
        label = { Text(label) },
        modifier = Modifier
            .semantics {
                contentDescription = "Filter by transaction type $label"
            }
            .pocketLedgerSelectedState(selected),
    )
}

@Composable
private fun SearchStateContent(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> Centered(modifier) {
            LoadingState(message = "Searching transactions")
        }
        uiState.errorMessage != null -> Centered(modifier) {
            ErrorState(
                title = "Could not search transactions",
                message = uiState.errorMessage,
                onRetry = { onAction(SearchAction.RetryClicked) },
            )
        }
        uiState.modeUnavailableMessage != null -> Centered(modifier) {
            EmptyState(
                title = "Search mode unavailable",
                message = uiState.modeUnavailableMessage,
            )
        }
        uiState.isEmptyLedger -> Centered(modifier) {
            EmptyState(
                title = "No transactions yet",
                message = "Saved transactions will appear in search.",
            )
        }
        uiState.hasNoResults -> Centered(modifier) {
            EmptyState(
                title = "No matching transactions",
                message = "Clear filters or try a different keyword.",
            )
        }
        else -> SearchResultList(
            results = uiState.results,
            onAction = onAction,
            modifier = modifier,
        )
    }
}

@Composable
private fun SearchResultList(
    results: List<SearchResultUiModel>,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .testTag("SearchResultsList")
            .semantics { testTagsAsResourceId = true },
        verticalArrangement = Arrangement.spacedBy(PocketLedgerThemeDefaults.spacing.none),
    ) {
        itemsIndexed(
            items = results,
            key = { _, result -> result.transactionId },
        ) { index, result ->
            SearchResultRow(
                result = result,
                onClick = { onAction(SearchAction.ResultClicked(result.transactionId)) },
                showDivider = index < results.lastIndex,
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    result: SearchResultUiModel,
    onClick: () -> Unit,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = PocketLedgerThemeDefaults.spacing
    val subtitle = remember(result) {
        listOfNotNull(result.typeLabel, result.dateLabel, result.notePreview)
            .joinToString(separator = " - ")
    }
    Column(modifier = modifier.fillMaxWidth()) {
        TransactionRow(
            title = result.title,
            amount = result.amount,
            subtitle = subtitle,
            category = result.categoryLabel,
            onClick = onClick,
            showDivider = showDivider && result.tagLabels.isEmpty(),
            onClickLabel = "Open transaction details",
            contentDescription = result.contentDescription,
        )
        if (result.tagLabels.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                contentPadding = PaddingValues(bottom = spacing.small),
                modifier = Modifier.padding(horizontal = spacing.medium),
            ) {
                items(result.tagLabels, key = { tag -> tag }) { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag) },
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = "Tag $tag"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Centered(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private data class DateFilterOption(
    val label: String,
    val range: SearchDateRange?,
) {
    companion object {
        fun defaults(
            today: LocalDate = LocalDate.now(),
            zoneId: ZoneId = ZoneId.systemDefault(),
        ): List<DateFilterOption> {
            val monthStart = today.withDayOfMonth(1)
            return listOf(
                DateFilterOption("All time", null),
                DateFilterOption(
                    "This month",
                    SearchDateRange(
                        startMillis = monthStart.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                        endMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1,
                    ),
                ),
                DateFilterOption(
                    "Last 30 days",
                    SearchDateRange(
                        startMillis = today.minusDays(29).atStartOfDay(zoneId).toInstant().toEpochMilli(),
                        endMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1,
                    ),
                ),
            )
        }
    }
}

private data class AmountFilterOption(
    val label: String,
    val range: SearchAmountRange?,
) {
    companion object {
        fun defaults(): List<AmountFilterOption> = listOf(
            AmountFilterOption("Any amount", null),
            AmountFilterOption("Under ${'$'}25", SearchAmountRange(maxMinor = 2_499)),
            AmountFilterOption("${'$'}25-${'$'}100", SearchAmountRange(minMinor = 2_500, maxMinor = 10_000)),
            AmountFilterOption("Over ${'$'}100", SearchAmountRange(minMinor = 10_001)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    PocketLedgerPreviewTheme {
        SearchScreen(
            uiState = SearchPreviewFixtures.contentState,
            onAction = {},
        )
    }
}
