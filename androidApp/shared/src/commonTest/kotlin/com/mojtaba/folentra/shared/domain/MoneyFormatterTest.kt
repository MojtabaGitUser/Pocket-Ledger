package com.mojtaba.folentra.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {
    private val formatter = MoneyFormatter()

    @Test
    fun formatsPositiveAmount() {
        assertEquals("\$12.34", formatter.formatCents(1_234))
    }

    @Test
    fun formatsNegativeAmount() {
        assertEquals("-\$5.67", formatter.formatCents(-567))
    }

    @Test
    fun padsSingleDigitCents() {
        assertEquals("\$10.05", formatter.formatCents(1_005))
    }
}
