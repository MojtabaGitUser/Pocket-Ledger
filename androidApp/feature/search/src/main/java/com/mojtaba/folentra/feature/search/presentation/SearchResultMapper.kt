package com.mojtaba.folentra.feature.search.presentation

import com.mojtaba.folentra.core.data.model.LedgerCategory
import com.mojtaba.folentra.core.data.model.LedgerTag
import com.mojtaba.folentra.core.data.model.LedgerTransaction
import com.mojtaba.folentra.core.designsystem.component.AmountDisplay
import com.mojtaba.folentra.core.designsystem.component.AmountTone
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

internal object SearchResultMapper {
    private const val NOTE_PREVIEW_LIMIT = 72
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

    fun map(
        transaction: LedgerTransaction,
        category: LedgerCategory?,
        tags: List<LedgerTag>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): SearchResultUiModel {
        val tone = transaction.amountTone()
        val categoryLabel = category?.name.cleanOrNull() ?: "Uncategorized"
        val typeLabel = transaction.typeLabel()
        val notePreview = transaction.note.previewOrNull()
        val title = transaction.merchant.cleanOrNull() ?: categoryLabel
        val amount = transaction.amountDisplay(tone)
        val tagLabels = tags.mapNotNull { it.name.cleanOrNull() }
        val dateLabel = formatDate(transaction.occurredAt, zoneId)
        return SearchResultUiModel(
            transactionId = transaction.id,
            title = title,
            amount = amount,
            typeLabel = typeLabel,
            categoryLabel = categoryLabel,
            dateLabel = dateLabel,
            notePreview = notePreview,
            tagLabels = tagLabels,
            contentDescription = listOfNotNull(
                "Transaction $title",
                typeLabel,
                categoryLabel,
                dateLabel,
                notePreview,
                amount.contentDescription,
                tagLabels.takeIf { it.isNotEmpty() }?.joinToString(prefix = "Tags "),
            ).joinToString(separator = ", "),
        )
    }

    fun formatDate(
        epochMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .format(dateFormatter)

    private fun LedgerTransaction.amountDisplay(tone: AmountTone): AmountDisplay {
        val text = formatAmountMinor(amountMinor, currencyCode)
        return AmountDisplay(
            text = text,
            tone = tone,
            contentDescription = "$text ${typeLabel().lowercase(Locale.US)}",
        )
    }

    private fun formatAmountMinor(
        amountMinor: Long,
        currencyCode: String,
        includeSign: Boolean = true,
    ): String {
        val amount = BigDecimal.valueOf(abs(amountMinor))
            .movePointLeft(2)
            .setScale(2, RoundingMode.HALF_UP)
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        runCatching {
            formatter.currency = Currency.getInstance(currencyCode.uppercase(Locale.US))
        }
        val formatted = formatter.format(amount)
        return when {
            !includeSign || amountMinor == 0L -> formatted
            amountMinor > 0 -> "+$formatted"
            else -> "-$formatted"
        }
    }

    private fun LedgerTransaction.amountTone(): AmountTone =
        when {
            type.equals("income", ignoreCase = true) || amountMinor > 0 -> AmountTone.Positive
            type.equals("expense", ignoreCase = true) || amountMinor < 0 -> AmountTone.Negative
            else -> AmountTone.Neutral
        }

    private fun LedgerTransaction.typeLabel(): String =
        if (type.equals("income", ignoreCase = true)) "Income" else "Expense"

    private fun String?.cleanOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String?.previewOrNull(): String? {
        val cleaned = cleanOrNull() ?: return null
        return if (cleaned.length <= NOTE_PREVIEW_LIMIT) cleaned else cleaned.take(NOTE_PREVIEW_LIMIT).trimEnd() + "..."
    }
}
