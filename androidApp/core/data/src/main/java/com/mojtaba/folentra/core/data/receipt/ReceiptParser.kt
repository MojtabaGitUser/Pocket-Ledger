package com.mojtaba.folentra.core.data.receipt

import java.math.BigDecimal
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.ResolverStyle
import java.util.Locale
import kotlin.math.abs

/** Conservative rules: evidence ranking, never a largest-number or first-number fallback. */
class ReceiptParser {
    private data class Amount(val minor: Long, val rank: Int, val evidence: String)

    fun parse(text: ReceiptText, today: LocalDate = LocalDate.now()): ParsedReceipt {
        val lines = text.lines.filter { it.text.isNotBlank() }.take(1000)
            .sortedWith(compareBy<ReceiptLine> { it.bounds?.top ?: 0 }.thenBy { it.bounds?.left ?: 0 })
        val amounts = lines.flatMapIndexed { index, line ->
            val label = normalize(line.text)
            val rank = totalRank(label)
            if (rank == 0) emptyList() else {
                var candidates = money(line.text)
                var evidence = line.text
                if (candidates.isEmpty()) {
                    // ML Kit can split the label and right-aligned value into separate blocks.
                    val nearby = lines.filter { other ->
                        other !== line && line.bounds != null && other.bounds != null &&
                            other.bounds.left >= line.bounds.right &&
                            abs(other.bounds.top - line.bounds.top) <= maxOf(12, line.bounds.bottom - line.bounds.top) &&
                            normalize(other.text).none { it.isLetter() }
                    }
                    val next = lines.getOrNull(index + 1)?.takeIf {
                        normalize(it.text).none(Char::isLetter) &&
                            (line.bounds == null || it.bounds == null ||
                                it.bounds.top - line.bounds.bottom in 0..(2 * (line.bounds.bottom - line.bounds.top)))
                    }
                    val adjacent = (nearby + listOfNotNull(next)).distinct()
                    candidates = adjacent.flatMap { money(it.text) }
                    evidence += " " + adjacent.joinToString(" ") { it.text }
                }
                candidates.map { Amount(it, rank, evidence.take(160)) }
            }
        }
        val bestRank = amounts.maxOfOrNull { it.rank }
        val best = amounts.filter { it.rank == bestRank }
        val distinct = best.map { it.minor }.distinct()
        val hasRefund = lines.any { Regex("\\b(refund|remboursement|return)\\b").containsMatchIn(normalize(it.text)) }
        val amount = when {
            hasRefund -> ReceiptField<Long>(null, ReceiptConfidence.UNCERTAIN, "Refund/return: enter the signed transaction manually")
            distinct.size == 1 && distinct.single() > 0 -> ReceiptField(distinct.single(), ReceiptConfidence.SUPPORTED, best.first().evidence)
            amounts.isNotEmpty() -> ReceiptField(null, ReceiptConfidence.UNCERTAIN, "Conflicting or non-positive totals; enter the final amount")
            else -> ReceiptField<Long>(null, ReceiptConfidence.MISSING)
        }
        val merchant = lines.take(8).firstOrNull { line ->
            val value = normalize(line.text)
            val letters = value.count(Char::isLetter)
            val digits = value.count(Char::isDigit)
            letters >= 3 && digits <= 6 && digits < letters && value.length in 3..120 &&
                !Regex("\\b(transaction (record|receipt)|sales receipt|merchant copy|receipt|recu|invoice|facture|welcome|bienvenue|thank|merci|customer|copy|duplicate|total|date|tel|phone|street|avenue|road|rue|boulevard|www|http|cashier|terminal|visa|mastercard)\\b").containsMatchIn(value)
        }?.let { ReceiptField(it.text.trim(), ReceiptConfidence.UNCERTAIN, "Business name near the top; please check") }
            ?: ReceiptField(null, ReceiptConfidence.MISSING)
        val joined = lines.joinToString("\n") { it.text }
        val currencies = buildSet {
            if (Regex("(?i)\\bCAD\\b|CA\\$|C\\$").containsMatchIn(joined)) add("CAD")
            if (Regex("(?i)\\bUSD\\b|US\\$").containsMatchIn(joined)) add("USD")
        }
        val currency = if (currencies.size == 1) ReceiptField(currencies.single(), ReceiptConfidence.SUPPORTED, "Explicit currency code")
            else ReceiptField<String>(null, if (currencies.isEmpty()) ReceiptConfidence.MISSING else ReceiptConfidence.UNCERTAIN,
                "A dollar sign alone does not distinguish CAD from USD")
        return ParsedReceipt(merchant, amount, parseDate(lines, today), currency)
    }

    private fun totalRank(label: String): Int = when {
        Regex("\\b(sub\\s*total|sous.total|tax|taxes|gst|pst|hst|qst|tps|tvq|change|monnaie|tendered|cash|comptant|discount|rabais|savings|loyalty|balance|tip|pourboire|suggested|suggestion)\\b").containsMatchIn(label) -> 0
        Regex("\\b(grand total|total due|total a payer|montant total|amount due)\\b").containsMatchIn(label) -> 3
        Regex("\\b(total|amount paid|montant paye)\\b").containsMatchIn(label) -> 2
        else -> 0
    }

    private fun money(value: String): List<Long> = MONEY.findAll(value.replace('\u00a0', ' ').replace('\u202f', ' ')).mapNotNull { match ->
        val raw = match.value.trim().replace(" ", "")
        try {
            val separator = maxOf(raw.lastIndexOf('.'), raw.lastIndexOf(','))
            val normalized = raw.substring(0, separator).replace(",", "").replace(".", "") + "." + raw.substring(separator + 1)
            BigDecimal(normalized).movePointRight(2).longValueExact()
        } catch (_: ArithmeticException) { null } catch (_: NumberFormatException) { null }
    }.toList()

    private fun parseDate(lines: List<ReceiptLine>, today: LocalDate): ReceiptField<LocalDate> {
        val candidates = mutableListOf<Pair<LocalDate, String>>()
        var ambiguous = false
        lines.forEach { line ->
            val value = normalize(line.text)
            if (Regex("\\b(exp|expiry|expiration|valid|expires)\\b").containsMatchIn(value)) return@forEach
            ISO.findAll(value).forEach { m ->
                date(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())?.let { candidates += it to m.value }
            }
            NUMERIC.findAll(value).forEach { m ->
                val a = m.groupValues[1].toInt(); val b = m.groupValues[2].toInt()
                val rawYear = m.groupValues[3].toInt()
                // Receipts commonly print two-digit years. Resolve them relative to the
                // current century without ever turning a future year into a receipt date.
                val year = if (m.groupValues[3].length == 2) {
                    val currentTwoDigits = today.year % 100
                    if (rawYear <= currentTwoDigits) 2000 + rawYear else 1900 + rawYear
                } else rawYear
                if (a in 1..12 && b in 1..12 && a != b) ambiguous = true
                else date(year, if (a > 12) b else a, if (a > 12) a else b)?.let { candidates += it to m.value }
            }
            val english = MONTHS.entries.fold(value) { text, (word, replacement) ->
                text.replace(Regex("\\b$word\\b\\.?"), replacement)
            }
            listOf("MMM d uuuu", "d MMM uuuu").forEach { pattern ->
                val regex = if (pattern.startsWith("MMM")) Regex("\\b[a-z]{3} \\d{1,2} \\d{4}\\b") else Regex("\\b\\d{1,2} [a-z]{3} \\d{4}\\b")
                regex.findAll(english.replace(",", "").replace(Regex("([a-z]{3})\\."), "$1").replace(Regex("\\s+"), " ")).forEach { m ->
                    try {
                        val format = java.time.format.DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern)
                            .toFormatter(Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT)
                        candidates += LocalDate.parse(m.value, format) to m.value
                    } catch (_: java.time.DateTimeException) { /* Not a calendar date. */ }
                }
            }
        }
        val dates = candidates.map { it.first }.distinct()
        return when {
            ambiguous || dates.size > 1 || dates.any { it > today } -> ReceiptField(null, ReceiptConfidence.UNCERTAIN, "Ambiguous, conflicting or future date; select the receipt date")
            dates.size == 1 -> ReceiptField(dates.single(), ReceiptConfidence.SUPPORTED, candidates.first().second)
            else -> ReceiptField(null, ReceiptConfidence.MISSING)
        }
    }
    private fun date(year: Int, month: Int, day: Int) = try { LocalDate.of(year, month, day) } catch (_: java.time.DateTimeException) { null }
    private fun normalize(value: String) = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "").lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

    companion object {
        private val MONEY = Regex("(?<![\\d.,/-])-?(?:\\d{1,3}(?:[ ,. ]\\d{3})+|\\d+)[.,]\\d{2}(?![\\d/])")
        private val ISO = Regex("\\b(20\\d{2})[-/](\\d{1,2})[-/](\\d{1,2})\\b")
        private val NUMERIC = Regex("(?<![\\d/-])(\\d{1,2})[/-](\\d{1,2})[/-](20\\d{2}|\\d{2})\\b")
        private val MONTHS = mapOf("janvier" to "jan", "janv" to "jan", "fevrier" to "feb", "fevr" to "feb", "mars" to "mar",
            "avril" to "apr", "avr" to "apr", "mai" to "may", "juin" to "jun", "juillet" to "jul", "juil" to "jul",
            "aout" to "aug", "septembre" to "sep", "sept" to "sep", "octobre" to "oct", "novembre" to "nov", "decembre" to "dec", "dec" to "dec")
    }
}
