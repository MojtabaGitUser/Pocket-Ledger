package com.mojtaba.pocketledger.core.background

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

sealed interface TaskSchedule {
    val initialDelay: Duration

    data class OneTime(
        override val initialDelay: Duration = Duration.ZERO,
    ) : TaskSchedule {
        init {
            require(!initialDelay.isNegative()) { "Initial delay must not be negative." }
        }
    }

    data class Periodic(
        val repeatInterval: Duration,
        val flexInterval: Duration? = null,
        override val initialDelay: Duration = Duration.ZERO,
    ) : TaskSchedule {
        init {
            require(repeatInterval.isPositive()) { "Repeat interval must be positive." }
            require(repeatInterval >= MIN_PERIODIC_INTERVAL) {
                "Repeat interval must be at least $MIN_PERIODIC_INTERVAL."
            }
            require(!initialDelay.isNegative()) { "Initial delay must not be negative." }
            require(flexInterval == null || flexInterval.isPositive()) {
                "Flex interval must be positive when present."
            }
            require(flexInterval == null || flexInterval >= MIN_PERIODIC_FLEX_INTERVAL) {
                "Flex interval must be at least $MIN_PERIODIC_FLEX_INTERVAL when present."
            }
            require(flexInterval == null || flexInterval <= repeatInterval) {
                "Flex interval must not be greater than repeat interval."
            }
        }

        companion object {
            val MIN_PERIODIC_INTERVAL: Duration = 15.minutes
            val MIN_PERIODIC_FLEX_INTERVAL: Duration = 5.minutes
        }
    }
}
