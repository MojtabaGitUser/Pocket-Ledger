package com.mojtaba.pocketledger.core.designsystem.preview

import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay
import com.mojtaba.pocketledger.core.designsystem.component.AmountTone

object PreviewAmounts {
    val positive = AmountDisplay(
        text = "$2,450.00",
        tone = AmountTone.Positive,
        contentDescription = "2,450 dollars income",
    )

    val negative = AmountDisplay(
        text = "-$86.42",
        tone = AmountTone.Negative,
        contentDescription = "86 dollars and 42 cents expense",
    )

    val zero = AmountDisplay(
        text = "$0.00",
        tone = AmountTone.Neutral,
        contentDescription = "zero dollars",
    )

    val large = AmountDisplay(
        text = "$18,920.75",
        tone = AmountTone.Positive,
        contentDescription = "18,920 dollars and 75 cents",
    )

    val smallCents = AmountDisplay(
        text = "-$0.99",
        tone = AmountTone.Negative,
        contentDescription = "99 cents expense",
    )

    val pending = AmountDisplay(
        text = "$42.00",
        tone = AmountTone.Neutral,
        contentDescription = "42 dollars pending",
    )

    val all = listOf(positive, negative, zero, large, smallCents, pending)
}
