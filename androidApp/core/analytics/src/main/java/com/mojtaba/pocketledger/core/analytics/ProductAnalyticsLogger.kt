package com.mojtaba.pocketledger.core.analytics

interface ProductAnalyticsLogger {
    fun log(event: ProductEvent)
}

class NoOpProductAnalyticsLogger : ProductAnalyticsLogger {
    override fun log(event: ProductEvent) = Unit
}

class DebugProductAnalyticsLogger(
    private val mapper: ProductEventMapper = ProductEventMapper(),
    private val sink: (MappedProductEvent) -> Unit = {},
) : ProductAnalyticsLogger {
    override fun log(event: ProductEvent) {
        sink(mapper.map(event))
    }
}

enum class ProductAnalyticsProviderState(val value: String) {
    NoOp("no_op"),
    DebugSink("debug_sink"),
    FirebaseNotWired("firebase_not_wired"),
}