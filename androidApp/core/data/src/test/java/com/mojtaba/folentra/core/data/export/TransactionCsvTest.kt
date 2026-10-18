package com.mojtaba.folentra.core.data.export

import com.mojtaba.folentra.core.data.model.LedgerTransaction
import java.io.StringWriter
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class TransactionCsvTest {
    private val request = CsvExportRequest(LocalDate.of(2026, 3, 8), LocalDate.of(2026, 3, 8), ZoneId.of("America/Vancouver"))
    private fun transaction(note: String = "private") = LedgerTransaction(
        id = "hidden-id", amountMinor = -12345, currencyCode = "CAD", type = "expense",
        occurredAt = request.startMillis, merchant = "فروشگاه, \"الف\"", note = note,
        createdAt = 0, updatedAt = 0)
    private fun csv(options: CsvExportRequest = request, note: String = "private"): String =
        StringWriter().also { TransactionCsv.write(it, listOf(transaction(note)), options) }.toString()

    @Test fun excludesPrivateFieldsByDefaultAndPreservesAmounts() {
        assertEquals("\uFEFF\"date\",\"type\",\"amount\",\"currency\"\r\n\"2026-03-08\",\"expense\",\"-123.45\",\"CAD\"\r\n", csv())
        assertFalse(csv().contains("private"))
        assertFalse(csv().contains("hidden-id"))
    }
    @Test fun escapesPersianQuotesCommasAndMultilineNotes() {
        val text = csv(request.copy(includeMerchant = true, includeNotes = true), "line1\nline2")
        assertTrue(text.contains("\"فروشگاه, \"\"الف\"\"\""))
        assertTrue(text.contains("\"line1\nline2\""))
    }
    @Test fun neutralizesFormulaPrefixesIncludingWhitespace() {
        listOf("=1+1", " +SUM(A1)", "-1+2", "@SUM(A1)", "\t=1", "\n=1").forEach {
            assertTrue(csv(request.copy(includeNotes = true), it).contains("\"'$it\""))
        }
    }
    @Test fun dateRangeIncludesWholeDayAcrossDaylightSaving() {
        assertEquals(23 * 60 * 60 * 1000L, request.endMillis - request.startMillis + 1)
        val writer = StringWriter()
        TransactionCsv.write(writer, listOf(transaction().copy(occurredAt = request.endMillis)), request)
        assertTrue(writer.toString().contains("2026-03-08"))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsReversedRange() {
        request.copy(end = request.start.minusDays(1))
    }
    @Test fun handlesLargeSnapshotWithoutBuildingOneCsvString() {
        var count = 0L
        val writer = object : java.io.Writer() {
            override fun write(buffer: CharArray, offset: Int, length: Int) { count += length }
            override fun flush() = Unit
            override fun close() = Unit
        }
        TransactionCsv.write(writer, List(10000) { transaction() }, request)
        assertTrue(count > 400000)
    }
}
