package com.mojtaba.pocketledger.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductEventMapperTest {
    private val mapper = ProductEventMapper()

    @Test
    fun mapsScreenViewedToProviderSafeNameAndParameters() {
        val mapped = mapper.map(
            ProductEvent.ScreenViewed(
                screenName = ProductScreen.Dashboard,
                source = EventSource.Navigation,
            ),
        )

        assertEquals("screen_viewed", mapped.name)
        assertEquals(
            mapOf(
                "screen_name" to "dashboard",
                "source" to "navigation",
            ),
            mapped.parameters,
        )
    }

    @Test
    fun mapsTransactionCreatedWithoutExactFinancialValues() {
        val mapped = mapper.map(
            ProductEvent.TransactionCreated(
                amountBucket = AmountBucket.Medium,
                currencyPresent = true,
                isRecurring = false,
            ),
        )

        assertEquals("transaction_created", mapped.name)
        assertEquals("medium", mapped.parameters["amount_bucket"])
        assertEquals("true", mapped.parameters["currency_present"])
        assertEquals("false", mapped.parameters["is_recurring"])
        assertFalse(mapped.parameters.containsKey("amount"))
        assertFalse(mapped.parameters.containsKey("merchant"))
        assertFalse(mapped.parameters.containsKey("note"))
        assertFalse(mapped.parameters.containsKey("category_name"))
    }

    @Test
    fun approvedTaxonomyNamesAndKeysAreProviderCompatible() {
        ProductEventName.entries.forEach { eventName ->
            assertTrue("Invalid event name ${eventName.value}", eventName.value.matches(Regex("^[a-z][a-z0-9_]{1,39}$")))
        }
        ProductEventParameterKey.entries.forEach { parameterKey ->
            assertTrue("Invalid parameter key ${parameterKey.value}", parameterKey.value.matches(Regex("^[a-z][a-z0-9_]{1,39}$")))
        }
        assertEquals(ProductEventName.entries.size, ProductEventName.entries.map { it.value }.toSet().size)
        assertEquals(ProductEventParameterKey.entries.size, ProductEventParameterKey.entries.map { it.value }.toSet().size)
    }

    @Test
    fun unsafeAppVersionIsRejected() {
        val exception = runCatching {
            AppVersion("1.0 debug build")
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun noOpLoggerAcceptsTypedEventsWithoutCrashing() {
        NoOpProductAnalyticsLogger().log(ProductEvent.AppForegrounded)
    }

    @Test
    fun debugLoggerMapsOnlySanitizedEventPayload() {
        val events = mutableListOf<MappedProductEvent>()
        val logger = DebugProductAnalyticsLogger(sink = events::add)

        logger.log(
            ProductEvent.SearchPerformed(
                result = EventResult.Success,
                countBucket = CountBucket.TwoToFive,
                source = EventSource.Search,
            ),
        )

        assertEquals(1, events.size)
        assertEquals("search_performed", events.single().name)
        assertEquals(
            mapOf(
                "result" to "success",
                "count_bucket" to "2_5",
                "source" to "search",
            ),
            events.single().parameters,
        )
    }
}