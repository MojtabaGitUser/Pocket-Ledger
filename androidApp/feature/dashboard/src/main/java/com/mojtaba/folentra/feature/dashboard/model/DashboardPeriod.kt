package com.mojtaba.folentra.feature.dashboard.model

data class DashboardPeriod(
    val startMillis: Long,
    val endMillis: Long,
    val label: String,
) {
    init {
        require(startMillis <= endMillis) { "DashboardPeriod startMillis must be before or equal to endMillis." }
    }

    fun contains(epochMillis: Long): Boolean = epochMillis in startMillis..endMillis
}
