package com.mojtaba.pocketledger.core.security.logging

interface LogSink {
    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    )
}
