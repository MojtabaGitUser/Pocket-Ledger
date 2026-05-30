package com.mojtaba.pocketledger.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.mojtaba.pocketledger.core.designsystem.preview.PreviewAmounts
import com.mojtaba.pocketledger.core.designsystem.theme.PocketLedgerPreviewTheme

enum class AmountTone {
    Positive,
    Negative,
    Neutral,
}

@Immutable
data class AmountDisplay(
    val text: String,
    val tone: AmountTone = AmountTone.Neutral,
    val contentDescription: String = text,
)

@Composable
fun AmountText(
    text: String,
    modifier: Modifier = Modifier,
    tone: AmountTone = AmountTone.Neutral,
    contentDescription: String = text,
    style: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Text(
        text = text,
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        },
        color = amountColor(tone),
        style = style,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun AmountText(
    amount: AmountDisplay,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
) {
    AmountText(
        text = amount.text,
        modifier = modifier,
        tone = amount.tone,
        contentDescription = amount.contentDescription,
        style = style,
    )
}

@Composable
private fun amountColor(tone: AmountTone): Color =
    when (tone) {
        AmountTone.Positive -> MaterialTheme.colorScheme.primary
        AmountTone.Negative -> MaterialTheme.colorScheme.error
        AmountTone.Neutral -> MaterialTheme.colorScheme.onSurface
    }

@Preview(showBackground = true)
@Composable
private fun AmountTextPreview() {
    PocketLedgerPreviewTheme {
        androidx.compose.foundation.layout.Column {
            AmountText(amount = PreviewAmounts.positive)
            AmountText(amount = PreviewAmounts.negative)
            AmountText(amount = PreviewAmounts.zero)
        }
    }
}
