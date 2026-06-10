package com.mojtaba.pocketledger.core.security.logging

interface AppLogger {
    fun debug(message: String)

    fun info(message: String)

    fun warning(message: String)

    fun error(
        throwable: Throwable? = null,
        message: String,
    )
}
