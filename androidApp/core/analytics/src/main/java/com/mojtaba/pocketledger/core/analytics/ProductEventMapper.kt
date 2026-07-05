package com.mojtaba.pocketledger.core.analytics

class ProductEventMapper {
    fun map(event: ProductEvent): MappedProductEvent {
        val name = event.name.value
        require(name.matches(EVENT_NAME_REGEX)) { "Invalid product event name: $name" }

        val parameters = event.parameters.associate { parameter ->
            val key = parameter.key.value
            val value = parameter.value
            require(key.matches(PARAMETER_KEY_REGEX)) { "Invalid product event parameter key: $key" }
            require(value.matches(PARAMETER_VALUE_REGEX)) { "Invalid product event parameter value for $key." }
            key to value
        }

        return MappedProductEvent(
            name = name,
            parameters = parameters,
        )
    }

    private companion object {
        val EVENT_NAME_REGEX = Regex("^[a-z][a-z0-9_]{1,39}$")
        val PARAMETER_KEY_REGEX = Regex("^[a-z][a-z0-9_]{1,39}$")
        val PARAMETER_VALUE_REGEX = Regex("^[A-Za-z0-9._+-]{1,64}$")
    }
}

data class MappedProductEvent(
    val name: String,
    val parameters: Map<String, String>,
)