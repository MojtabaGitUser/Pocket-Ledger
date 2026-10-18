package com.mojtaba.folentra.core.data.export

import com.mojtaba.folentra.core.data.model.LedgerTransaction
import java.io.Writer
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CsvExportRequest(
    val start: LocalDate,
    val end: LocalDate,
    val zone: ZoneId,
    val includeMerchant: Boolean = false,
    val includeNotes: Boolean = false,
) {
    init {
        require(!end.isBefore(start))
        require(start.year in 1900..9998 && end.year in 1900..9998)
    }
    val startMillis: Long get() = start.atStartOfDay(zone).toInstant().toEpochMilli()
    val endMillis: Long get() = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
}

/** Stable v1 schema. Ledger amounts use two decimal places, matching transaction entry.
 * Writes rows incrementally; does not close the caller-owned writer or log financial data.
 */
object TransactionCsv {
    fun write(writer: Writer, transactions: List<LedgerTransaction>, request: CsvExportRequest) {
        val range = request.startMillis..request.endMillis
        writer.write("\uFEFF") // UTF-8 BOM for spreadsheet applications, including Persian text.
        row(writer, buildList {
            addAll(listOf("date", "type", "amount", "currency"))
            if (request.includeMerchant) add("merchant")
            if (request.includeNotes) add("note")
        })
        transactions.forEach { transaction ->
            require(transaction.occurredAt in range)
            row(writer, buildList {
                add(Instant.ofEpochMilli(transaction.occurredAt).atZone(request.zone).toLocalDate().toString())
                add(safeText(transaction.type))
                add(BigDecimal.valueOf(transaction.amountMinor, 2).toPlainString())
                add(safeText(transaction.currencyCode))
                if (request.includeMerchant) add(safeText(transaction.merchant.orEmpty()))
                if (request.includeNotes) add(safeText(transaction.note.orEmpty()))
            })
        }
    }

    // Quoting alone does not prevent spreadsheet formula execution.
    private fun safeText(value: String): String =
        if (value.trimStart().firstOrNull() in listOf('=', '+', '-', '@') ||
            value.firstOrNull() in listOf('\t', '\r', '\n')) "'$value" else value

    private fun row(writer: Writer, fields: List<String>) {
        writer.write(fields.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
        writer.write("\r\n")
    }
}
