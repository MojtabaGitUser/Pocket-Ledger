package com.mojtaba.folentra.core.security.logging

import android.util.Log

object AndroidLogSink : LogSink {
    override fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        when (level) {
            LogLevel.Debug -> Log.d(tag, message, throwable)
            LogLevel.Info -> Log.i(tag, message, throwable)
            LogLevel.Warning -> Log.w(tag, message, throwable)
            LogLevel.Error -> Log.e(tag, message, throwable)
        }
    }
}
