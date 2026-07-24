package com.mojtaba.folentra.feature.transaction.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.core.designsystem.component.EmptyState
import com.mojtaba.folentra.feature.transaction.presentation.detail.TransactionDetailRoute
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListAction
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListContent
import com.mojtaba.folentra.feature.transaction.presentation.list.TransactionListUiState

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionListDetailScreen(
    listUiState: TransactionListUiState,
    selectedTransactionId: String?,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    snackbarHostState: SnackbarHostState,
    onListAction: (TransactionListAction) -> Unit,
    onClearSelection: () -> Unit,
    onEditTransaction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TransactionListDetailContent(
        listUiState = listUiState,
        selectedTransactionId = selectedTransactionId,
        onListAction = onListAction,
        modifier = modifier,
    ) {
        TransactionDetailPane(
            selectedTransactionId = selectedTransactionId,
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            snackbarHostState = snackbarHostState,
            onClearSelection = onClearSelection,
            onEditTransaction = onEditTransaction,
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Transaction detail pane" },
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionListDetailContent(
    listUiState: TransactionListUiState,
    selectedTransactionId: String?,
    onListAction: (TransactionListAction) -> Unit,
    modifier: Modifier = Modifier,
    detailPane: @Composable () -> Unit,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>(
        initialDestinationHistory = buildList {
            add(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List))
            selectedTransactionId?.let { transactionId ->
                add(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, transactionId))
            }
        },
    )

    LaunchedEffect(selectedTransactionId) {
        selectedTransactionId?.let { transactionId ->
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, transactionId)
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        modifier = modifier.fillMaxSize(),
        listPane = {
            AnimatedPane {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Transactions",
                                    modifier = Modifier.semantics { heading() },
                                )
                            },
                        )
                    },
                ) { contentPadding ->
                    TransactionListContent(
                        uiState = listUiState,
                        onAction = onListAction,
                        selectedTransactionId = selectedTransactionId,
                        modifier = Modifier
                            .padding(contentPadding)
                            .fillMaxSize()
                            .semantics { contentDescription = "Transaction list pane" },
                    )
                }
            }
        },
        detailPane = {
            AnimatedPane {
                detailPane()
            }
        },
    )
}

@Composable
private fun TransactionDetailPane(
    selectedTransactionId: String?,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
    snackbarHostState: SnackbarHostState,
    onClearSelection: () -> Unit,
    onEditTransaction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selectedTransactionId == null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            EmptyState(
                title = "Select a transaction",
                message = "Choose a transaction from the list to view its details.",
                modifier = Modifier.semantics {
                    contentDescription = "No transaction selected"
                },
            )
        }
    } else {
        TransactionDetailRoute(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            tagRepository = tagRepository,
            transactionId = selectedTransactionId,
            onNavigateBack = onClearSelection,
            onEditTransaction = onEditTransaction,
            snackbarHostState = snackbarHostState,
            showBackAction = false,
            modifier = modifier,
        )
    }
}
