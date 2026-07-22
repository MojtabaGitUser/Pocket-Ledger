package com.mojtaba.folentra.core.background

@JvmInline
value class BackgroundTaskId(val value: String) {
    init {
        require(value.isNotBlank()) { "Background task id must not be blank." }
    }

    override fun toString(): String = value
}
