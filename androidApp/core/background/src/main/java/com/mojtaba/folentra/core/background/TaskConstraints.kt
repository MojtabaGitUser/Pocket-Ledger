package com.mojtaba.folentra.core.background

data class TaskConstraints(
    val requiresNetwork: Boolean = false,
    val requiresCharging: Boolean = false,
    val requiresBatteryNotLow: Boolean = false,
    val requiresDeviceIdle: Boolean = false,
)
