package com.mojtaba.pocketledger.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mojtaba.pocketledger.core.designsystem.component.EmptyState
import com.mojtaba.pocketledger.feature.transaction.adaptive.TransactionListDetailContent
import com.mojtaba.pocketledger.feature.transaction.presentation.detail.TransactionDetailContent
import com.mojtaba.pocketledger.feature.transaction.presentation.detail.TransactionDetailScreen
import com.mojtaba.pocketledger.feature.transaction.presentation.detail.TransactionDetailUiState
import com.mojtaba.pocketledger.feature.transaction.presentation.list.TransactionListScreen
import com.mojtaba.pocketledger.feature.transaction.presentation.list.TransactionListUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TransactionAdaptiveScreenshotTest(
    private val device: AdaptiveScreenshotDevice,
) {
    @get:Rule
    val screenshotRule = AdaptiveScreenshotRule(device)

    @Test
    fun transactionListContent() {
        screenshotRule.snapshotScreen("transactions/list", "content") {
            TransactionListScreen(
                uiState = ScreenshotTestData.transactionListContent,
                selectedTransactionId = ScreenshotTestData.selectedTransactionId,
                onAction = {},
            )
        }
    }

    @Test
    fun transactionListEmptyState() {
        screenshotRule.snapshotScreen("transactions/list", "empty_state") {
            TransactionListScreen(
                uiState = TransactionListUiState.Empty,
                onAction = {},
            )
        }
    }

    @Test
    fun transactionDetailContent() {
        screenshotRule.snapshotScreen("transactions/detail", "content") {
            TransactionDetailScreen(
                uiState = ScreenshotTestData.transactionDetail,
                onAction = {},
                onNavigateBack = {},
            )
        }
    }

    @Test
    fun transactionDetailMissingState() {
        screenshotRule.snapshotScreen("transactions/detail", "missing_state") {
            TransactionDetailScreen(
                uiState = TransactionDetailUiState.NotFound,
                onAction = {},
                onNavigateBack = {},
            )
        }
    }

    @Test
    fun transactionDetailErrorState() {
        screenshotRule.snapshotScreen("transactions/detail", "error_state") {
            TransactionDetailScreen(
                uiState = TransactionDetailUiState.Error("The local transaction could not be read."),
                onAction = {},
                onNavigateBack = {},
            )
        }
    }

    @Test
    fun transactionListDetailAdaptiveLayout() {
        screenshotRule.snapshotScreen("transactions/adaptive", "list_detail") {
            TransactionListDetailContent(
                listUiState = ScreenshotTestData.transactionListContent,
                selectedTransactionId = ScreenshotTestData.selectedTransactionId,
                onListAction = {},
            ) {
                TransactionDetailContent(
                    uiState = ScreenshotTestData.transactionDetail,
                    onAction = {},
                )
            }
        }
    }

    @Test
    fun transactionListDetailNoSelection() {
        screenshotRule.snapshotScreen("transactions/adaptive", "no_selection") {
            TransactionListDetailContent(
                listUiState = ScreenshotTestData.transactionListContent,
                selectedTransactionId = null,
                onListAction = {},
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        title = "Select a transaction",
                        message = "Choose a transaction from the list to view its details.",
                    )
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun devices(): Collection<Array<Any>> =
            AdaptiveDeviceMatrix.All.map { arrayOf(it) }
    }
}
