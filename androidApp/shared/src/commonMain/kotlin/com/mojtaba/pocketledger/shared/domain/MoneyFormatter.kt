package com.mojtaba.pocketledger.shared.domain

import kotlin.math.absoluteValue

class MoneyFormatter {
    fun formatCents(amountCents: Long): String {
        val sign = if (amountCents < 0) "-" else ""
        val absoluteCents = amountCents.absoluteValue
        val dollars = absoluteCents / CENTS_PER_DOLLAR
        val cents = absoluteCents % CENTS_PER_DOLLAR

        return "$sign\$$dollars.${cents.toString().padStart(2, '0')}"
    }

    private companion object {
        const val CENTS_PER_DOLLAR = 100
    }
}
