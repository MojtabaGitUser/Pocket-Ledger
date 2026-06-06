package com.mojtaba.pocketledger.core.data.search

data class SearchDateRange(
    val startMillis: Long? = null,
    val endMillis: Long? = null,
) {
    val isEmpty: Boolean
        get() = startMillis == null && endMillis == null

    fun isValid(): Boolean =
        startMillis == null || endMillis == null || startMillis <= endMillis
}
