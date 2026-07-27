package com.mojtaba.folentra.feature.transaction.model

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

object TransactionUiFormatters {
    private const val NOTE_PREVIEW_LIMIT = 72
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    private val metadataDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.US)

    fun listItem(
        transaction: LedgerTransaction,
        category: LedgerCategory?,
        tags: List<LedgerTag>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): TransactionListItemUiModel {
        val tone = transaction.amountTone()
        val categoryLabel = category?.name.orFallback("Uncategorized")
        val typeLabel = transaction.typeLabel()
        val notePreview = transaction.note.previewOrNull()
        return TransactionListItemUiModel(
            id = transaction.id,
            amount = transaction.amountDisplay(tone),
            typeLabel = typeLabel,
            categoryLabel = categoryLabel,
            dateLabel = formatDate(transaction.occurredAt, zoneId),
            title = transaction.merchant.orFallback(categoryLabel),
            notePreview = notePreview,
            tagLabels = tags.map { it.name }.filter { it.isNotBlank() },
            tone = tone,
        )
    }

    fun detail(
        transaction: LedgerTransaction,
        category: LedgerCategory?,
        tags: List<LedgerTag>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): TransactionDetailUiModel {
        val tone = transaction.amountTone()
        return TransactionDetailUiModel(
            id = transaction.id,
            amount = transaction.amountDisplay(tone),
            typeLabel = transaction.typeLabel(),
            categoryLabel = category?.name.orFallback("Uncategorized"),
            dateLabel = formatDate(transaction.occurredAt, zoneId),
            merchantLabel = transaction.merchant.cleanOrNull(),
            noteLabel = transaction.note.cleanOrNull(),
            tagLabels = tags.map { it.name }.filter { it.isNotBlank() },
            createdAtLabel = formatMetadataDate(transaction.createdAt, zoneId),
            updatedAtLabel = formatMetadataDate(transaction.updatedAt, zoneId),
        )
    }

    fun formatAmountMinor(
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

    fun formatDate(
        epochMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .format(dateFormatter)

    fun notePreview(note: String?): String? = note.previewOrNull()

    private fun formatMetadataDate(
        epochMillis: Long,
        zoneId: ZoneId,
    ): String = Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .format(metadataDateFormatter)

    private fun LedgerTransaction.amountDisplay(tone: AmountTone): AmountDisplay {
        val text = formatAmountMinor(amountMinor, currencyCode)
        return AmountDisplay(
            text = text,
            tone = tone,
            contentDescription = "$text ${typeLabel().lowercase(Locale.US)}",
        )
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

    private fun String?.orFallback(fallback: String): String = cleanOrNull() ?: fallback

    private fun String?.previewOrNull(): String? {
        val cleaned = cleanOrNull() ?: return null
        return if (cleaned.length <= NOTE_PREVIEW_LIMIT) {
            cleaned
        } else {
            cleaned.take(NOTE_PREVIEW_LIMIT).trimEnd() + "..."
        }
    }
}
