package com.mojtaba.pocketledger.core.background

data class RegisteredBackgroundTask(
    val id: BackgroundTaskId,
    val description: String,
)
