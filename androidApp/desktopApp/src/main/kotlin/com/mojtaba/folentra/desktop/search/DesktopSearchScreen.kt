package com.mojtaba.folentra.desktop.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DesktopSearchScreen(
    records: List<DesktopSearchRecord>,
    modifier: Modifier = Modifier,
    mapper: DesktopSearchMapper = remember { DesktopSearchMapper() },
) {
    var queryText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(DesktopSearchMode.Keyword) }
    var selectedTypes by remember { mutableStateOf(emptySet<DesktopSearchTransactionType>()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val query = DesktopSearchQuery(
        text = queryText,
        mode = mode,
        transactionTypes = selectedTypes,
    )
    val state = mapper.map(records, query, selectedId)

    DesktopSearchContent(
        state = state,
        queryText = queryText,
        onQueryTextChange = { queryText = it },
        onModeChange = { mode = it },
        onToggleType = { type ->
            selectedTypes = if (type in selectedTypes) {
                selectedTypes - type
            } else {
                selectedTypes + type
            }
        },
        onClear = {
            queryText = ""
            mode = DesktopSearchMode.Keyword
            selectedTypes = emptySet()
            selectedId = null
        },
        onSelectResult = { selectedId = it },
        modifier = modifier,
    )
}

@Composable
private fun DesktopSearchContent(
    state: DesktopSearchUiState,
    queryText: String,
    onQueryTextChange: (String) -> Unit,
    onModeChange: (DesktopSearchMode) -> Unit,
    onToggleType: (DesktopSearchTransactionType) -> Unit,
    onClear: () -> Unit,
    onSelectResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        val twoPane = maxWidth >= 1040.dp
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchHeader()
            SearchControls(
                state = state,
                queryText = queryText,
                onQueryTextChange = onQueryTextChange,
                onModeChange = onModeChange,
                onToggleType = onToggleType,
                onClear = onClear,
            )
            if (state.isEmptyLedger) {
                CenteredState(
                    title = "No sample transactions",
                    message = "Add desktop demo records to preview search results.",
                    stateDescription = "Empty",
                    modifier = Modifier.weight(1f),
                )
            } else if (state.hasNoResults) {
                CenteredState(
                    title = "No matching results",
                    message = "Try a different keyword or clear the selected filters.",
                    stateDescription = "No results",
                    modifier = Modifier.weight(1f),
                )
            } else if (twoPane) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SearchResultsList(
                        results = state.results,
                        selectedId = state.selectedResult?.id,
                        onSelectResult = onSelectResult,
                        modifier = Modifier.weight(0.95f).fillMaxSize(),
                    )
                    SearchDetailPanel(
                        result = state.selectedResult,
                        modifier = Modifier.weight(1.05f).fillMaxSize(),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.results, key = { it.id }) { result ->
                        SearchResultCard(
                            result = result,
                            selected = result.id == state.selectedResult?.id,
                            onClick = { onSelectResult(result.id) },
                        )
                    }
                    state.selectedResult?.let { result ->
                        item { SearchDetailPanel(result = result, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = "Search deterministic local demo transactions",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchControls(
    state: DesktopSearchUiState,
    queryText: String,
    onQueryTextChange: (String) -> Unit,
    onModeChange: (DesktopSearchMode) -> Unit,
    onToggleType: (DesktopSearchTransactionType) -> Unit,
    onClear: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = queryText,
                onValueChange = onQueryTextChange,
                label = { Text("Keyword") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = "Search keyword input"
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = state.query.mode == DesktopSearchMode.Keyword,
                    onClick = { onModeChange(DesktopSearchMode.Keyword) },
                    label = { Text("Keyword") },
                    modifier = Modifier.semantics { stateDescription = if (state.query.mode == DesktopSearchMode.Keyword) "Selected" else "Not selected" },
                )
                FilterChip(
                    selected = state.query.mode == DesktopSearchMode.Semantic,
                    onClick = { onModeChange(DesktopSearchMode.Semantic) },
                    label = { Text("Local semantic") },
                    modifier = Modifier.semantics { stateDescription = if (state.query.mode == DesktopSearchMode.Semantic) "Selected" else "Not selected" },
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            when (state.providerStatus) {
                                DesktopSearchProviderStatus.KeywordOnly -> "Keyword search"
                                DesktopSearchProviderStatus.LocalSemanticFallback -> "Local fallback"
                            },
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Search provider status"
                        stateDescription = when (state.providerStatus) {
                            DesktopSearchProviderStatus.KeywordOnly -> "Keyword search"
                            DesktopSearchProviderStatus.LocalSemanticFallback -> "Local semantic fallback"
                        }
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DesktopSearchTransactionType.entries.forEach { type ->
                    val selected = type in state.query.transactionTypes
                    FilterChip(
                        selected = selected,
                        onClick = { onToggleType(type) },
                        label = { Text(type.label) },
                        modifier = Modifier.semantics {
                            contentDescription = "${type.label} filter"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        },
                    )
                }
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<DesktopSearchResult>,
    selectedId: String?,
    onSelectResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.semantics { contentDescription = "Search results list" }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(results, key = { it.id }) { result ->
                SearchResultCard(
                    result = result,
                    selected = result.id == selectedId,
                    onClick = { onSelectResult(result.id) },
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: DesktopSearchResult,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Preview sample transaction") { onClick() }
            .semantics {
                contentDescription = result.contentDescription
                stateDescription = if (selected) "Selected" else "Not selected"
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(result.amountText, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = listOf(result.typeLabel, result.categoryLabel, result.dateLabel).joinToString(" | "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            result.notePreview?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(
                text = result.matchReason,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SearchDetailPanel(
    result: DesktopSearchResult?,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.semantics { contentDescription = "Search result detail preview" }) {
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Select a result to preview details.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Detail preview", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text(result.title, style = MaterialTheme.typography.headlineSmall)
                DetailLine("Amount", result.amountDescription)
                DetailLine("Type", result.typeLabel)
                DetailLine("Category", result.categoryLabel)
                DetailLine("Date", result.dateLabel)
                result.notePreview?.let { DetailLine("Note", it) }
                if (result.tags.isNotEmpty()) {
                    DetailLine("Tags", result.tags.joinToString())
                }
                result.recurringLabel?.let { DetailLine("Schedule", it) }
                DetailLine("Match", result.matchReason)
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CenteredState(
    title: String,
    message: String,
    stateDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp).semantics {
                contentDescription = "$title. $message"
                this.stateDescription = stateDescription
                liveRegion = LiveRegionMode.Polite
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

