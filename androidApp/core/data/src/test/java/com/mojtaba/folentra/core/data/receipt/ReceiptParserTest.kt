package com.mojtaba.folentra.core.data.receipt

import com.mojtaba.folentra.core.data.model.DraftTransaction
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant

class ReceiptParserTest {
    private val today = LocalDate.of(2026, 11, 1)
    private fun parse(vararg lines: String) = ReceiptParser().parse(ReceiptText(lines.map { ReceiptLine(it) }), today)

    @Test fun simpleCanadianReceiptExcludesTaxesAndSubtotal() {
        val r = parse("MAPLE MARKET", "2026-10-26", "SUBTOTAL 10.00", "GST 0.50", "PST 0.70", "TOTAL CAD 11.20")
        assertEquals("MAPLE MARKET", r.merchant.value)
        assertEquals(1120L, r.amountMinor.value)
        assertEquals(LocalDate.of(2026, 10, 26), r.date.value)
        assertEquals("CAD", r.currency.value)
    }
    @Test fun genericTransactionHeaderIsNotUsedAsMerchant() {
        val r = parse(
            "TRANSACTION RECORD",
            "ESSO SMART STOP 3787",
            "3905 NORTH RD",
            "BURNABY BC V3J 1S2",
            "DATE: 2026-10-29 TIME: 17:00:24",
            "TOTAL CAD $ 55.00",
            "GST INCLUDED IN FUEL $ 2.62",
        )
        assertEquals("ESSO SMART STOP 3787", r.merchant.value)
        assertEquals(5500L, r.amountMinor.value)
        assertEquals(LocalDate.of(2026, 10, 29), r.date.value)
        assertEquals("CAD", r.currency.value)
    }
    @Test fun frenchReceiptSupportsDecimalCommaAndAccents() {
        val r = parse("CAFÉ DU PARC", "26 octobre 2026", "SOUS-TOTAL 10,00", "TPS 0,50", "TVQ 1,00", "TOTAL À PAYER CAD 11,50")
        assertEquals(1150L, r.amountMinor.value)
        assertEquals(LocalDate.of(2026, 10, 26), r.date.value)
    }
    @Test fun cashTenderedChangeAndLoyaltyAreNotTotals() {
        assertEquals(1250L, parse("TOTAL 12.50", "CASH TENDERED 20.00", "CHANGE 7.50", "LOYALTY BALANCE 80.00").amountMinor.value)
    }
    @Test fun amountPaidWithoutTotalIsSupported() {
        assertEquals(1250L, parse("AMOUNT PAID 12.50", "CHANGE 7.50").amountMinor.value)
    }
    @Test fun conflictingTotalsRequireManualEntry() {
        val r = parse("TOTAL 12.50", "TOTAL 14.50")
        assertNull(r.amountMinor.value)
        assertEquals(ReceiptConfidence.UNCERTAIN, r.amountMinor.confidence)
    }
    @Test fun tipsDiscountsAndSuggestedTotalsAreExcluded() {
        assertEquals(1300L, parse("SUBTOTAL 15.00", "DISCOUNT -5.00", "TIP 3.00", "GRAND TOTAL 13.00", "SUGGESTED TOTAL 16.00").amountMinor.value)
    }
    @Test fun missingFieldsAreNotInvented() {
        val r = parse("RECEIPT", "123 Main Street", "VISA 1234", "ITEM 10.00")
        assertNull(r.merchant.value); assertNull(r.amountMinor.value); assertNull(r.date.value); assertNull(r.currency.value)
    }
    @Test fun ambiguousNumericDateIsNotGuessedFromPhoneLocale() {
        val r = parse("01/02/2026")
        assertNull(r.date.value)
        assertEquals(ReceiptConfidence.UNCERTAIN, r.date.confidence)
    }
    @Test fun commonUnambiguousDatesAgree() {
        listOf("2026-10-26", "10/26/2026", "10/26/26", "26/10/2026", "26/10/26", "Oct 26, 2026", "26 Oct 2026", "26 oct. 2026").forEach {
            assertEquals(it, LocalDate.of(2026, 10, 26), parse(it).date.value)
        }
    }
    @Test fun oldReceiptWithTwoDigitYearIgnoresPromotionalDateRanges() {
        val r = parse(
            "Walmart",
            "TOTAL 90.32",
            "07/29/14 13:57:53",
            "Teacher Appreciation 7/25 - 7/31",
            "redeem by 8/15",
            "07/29/14 13:57:55",
        )
        assertEquals(LocalDate.of(2014, 7, 29), r.date.value)
        assertEquals(9032L, r.amountMinor.value)
    }
    @Test fun impossibleFutureAndConflictingDatesNeedReview() {
        assertNull(parse("2026-02-30").date.value)
        assertNull(parse("2027-01-01").date.value)
        assertNull(parse("2026-10-26", "2026-10-27").date.value)
    }
    @Test fun differentSeparatorsAndWhitespaceUseExactMinorUnits() {
        listOf("1,234.56", "1.234,56", "1 234,56", "1\u00a0234,56", "1234.56").forEach {
            assertEquals(it, 123456L, parse("TOTAL $it").amountMinor.value)
        }
    }
    @Test fun fragmentedLabelAndSpatiallyAlignedValue() {
        val lines = listOf(ReceiptLine("TOTAL", ReceiptBounds(10, 100, 70, 120)),
            ReceiptLine("42.10", ReceiptBounds(200, 101, 270, 121)),
            ReceiptLine("TAX 9.00", ReceiptBounds(10, 130, 270, 150)))
        assertEquals(4210L, ReceiptParser().parse(ReceiptText(lines), today).amountMinor.value)
        assertEquals(4210L, parse("TOTAL", " 42.10 ").amountMinor.value)
    }
    @Test fun refundsAndNegativeTotalsAreNotConvertedToExpenses() {
        assertNull(parse("REFUND", "TOTAL 10.00").amountMinor.value)
        assertNull(parse("TOTAL -10.00").amountMinor.value)
    }
    @Test fun dollarSignDoesNotGuessCountryAndConflictingCurrenciesAreRejected() {
        assertNull(parse("TOTAL $10.00").currency.value)
        assertNull(parse("USD CAD", "TOTAL 10.00").currency.value)
    }
    @Test fun mergeKeepsCorrectionsAndConvertsCalendarDateInLocalZone() {
        val parsed = parse("MAPLE MARKET", "TOTAL CAD 12.50", "2026-10-26")
        val original = DraftTransaction("one", amountInput = "9.00", merchant = "My correction", currencyCode = "USD", createdAt = 0, updatedAt = 0)
        val zone = ZoneId.of("America/Vancouver")
        val merged = parsed.fillMissing(original, zone)
        assertEquals("9.00", merged.amountInput); assertEquals("My correction", merged.merchant); assertEquals("USD", merged.currencyCode)
        assertEquals(LocalDate.of(2026, 10, 26), Instant.ofEpochMilli(merged.occurredAt!!).atZone(zone).toLocalDate())
        assertNull(parse("nothing").fillMissing(original, zone).occurredAt)
    }
}
