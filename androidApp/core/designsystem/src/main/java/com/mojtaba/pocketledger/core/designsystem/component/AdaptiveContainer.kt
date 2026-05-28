package com.mojtaba.pocketledger.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerThemeDefaults

@Composable
fun AdaptiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 840.dp,
    horizontalPadding: Dp = PocketLedgerThemeDefaults.spacing.medium,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .padding(horizontal = horizontalPadding),
            content = content,
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun AdaptiveContainerPreview() {
    PocketLedgerPreviewTheme {
        AdaptiveContainer {
            SectionHeader(
                title = "Dashboard",
                subtitle = "Adaptive content width with screen padding",
            )
        }
    }
}
