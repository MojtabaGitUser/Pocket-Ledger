package com.mojtaba.folentra.core.data.search

data class SearchAmountRange(
    val minMinor: Long? = null,
    val maxMinor: Long? = null,
) {
    val isEmpty: Boolean
        get() = minMinor == null && maxMinor == null

    fun isValid(): Boolean =
        hasNonNegativeBounds() && hasValidOrder()

    private fun hasNonNegativeBounds(): Boolean =
        (minMinor == null || minMinor >= 0L) &&
            (maxMinor == null || maxMinor >= 0L)

    private fun hasValidOrder(): Boolean =
        minMinor == null || maxMinor == null || minMinor <= maxMinor
}
