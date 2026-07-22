package com.mojtaba.folentra.core.background

sealed interface SchedulerResult {
    data object Success : SchedulerResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null,
    ) : SchedulerResult
}
