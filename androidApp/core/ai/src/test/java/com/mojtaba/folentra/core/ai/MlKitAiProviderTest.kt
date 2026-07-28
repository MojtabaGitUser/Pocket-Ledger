package com.mojtaba.folentra.core.ai

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MlKitAiProviderTest {
    @Test
    fun `unsupported runtime returns unavailable`() = runTest {
        val provider = MlKitAiProvider(FakeRuntime(OnDeviceModelStatus.Unavailable))

        val result = provider.generateMonthlySummary(request())

        assertTrue(result is AiInferenceResult.Unavailable)
    }

    @Test
    fun `downloadable runtime is prepared before inference`() = runTest {
        val runtime = FakeRuntime(OnDeviceModelStatus.Downloadable, preparedStatus = OnDeviceModelStatus.Available)
        val provider = MlKitAiProvider(runtime)

        val result = provider.generateMonthlySummary(request())

        assertTrue(result is AiInferenceResult.Success)
        assertEquals(1, runtime.prepareCalls)
        assertEquals(1, runtime.generateCalls)
    }

    @Test
    fun `monthly response is mapped to domain result`() = runTest {
        val provider = MlKitAiProvider(FakeRuntime(OnDeviceModelStatus.Available))

        val result = provider.generateMonthlySummary(request()) as AiInferenceResult.Success

        assertEquals(AiProviderType.MlKit, result.providerType)
        assertEquals("June overview", result.value.title)
        assertEquals("Spending stayed within income.", result.value.summaryText)
        assertEquals(listOf("Food was the largest category."), result.value.insights)
        assertEquals(listOf("Transport exceeded its budget."), result.value.warnings)
    }

    @Test
    fun `runtime failure is contained`() = runTest {
        val provider = MlKitAiProvider(FakeRuntime(OnDeviceModelStatus.Available, failure = IllegalStateException("quota")))

        val result = provider.generateMonthlySummary(request())

        assertTrue(result is AiInferenceResult.Failure)
        assertEquals("quota", (result as AiInferenceResult.Failure).reason)
    }

    @Test
    fun `availability reflects runtime status`() = runTest {
        val available = MlKitAiProvider(FakeRuntime(OnDeviceModelStatus.Available))
        val unsupported = MlKitAiProvider(FakeRuntime(OnDeviceModelStatus.Unavailable))

        assertEquals(AiProviderAvailability.Available, available.currentAvailability())
        assertTrue(unsupported.currentAvailability() is AiProviderAvailability.Unavailable)
    }

    @Test
    fun `semantic search validates ids and orders scores`() = runTest {
        val provider = MlKitAiProvider(
            FakeRuntime(
                OnDeviceModelStatus.Available,
                output = """
                    MATCH:groceries|72|Food shopping
                    MATCH:unknown|100|Not a supplied record
                    MATCH:coffee|91|Cafe purchase
                """.trimIndent(),
            ),
        )

        val result = provider.semanticSearch(
            SemanticSearchRequest(
                query = "coffee and groceries",
                documents = listOf(
                    SemanticSearchDocument("coffee", "Coffee", "Cafe"),
                    SemanticSearchDocument("groceries", "Market", "Food"),
                ),
            ),
        ) as AiInferenceResult.Success

        assertEquals(listOf("coffee", "groceries"), result.value.rankedIds)
        assertEquals(listOf(91, 72), result.value.matches.map { it.relevanceScore })
    }

    @Test
    fun `smart autofill accepts only allowed candidates and preserves explicit fields`() = runTest {
        val provider = MlKitAiProvider(
            FakeRuntime(
                OnDeviceModelStatus.Available,
                output = """
                    CATEGORY_ID:food
                    ACCOUNT_ID:unknown
                    AMOUNT_MINOR:4250
                    RECURRING:true
                    NOTE:Morning coffee
                    CONFIDENCE:HIGH
                    REASON:Matched prior cafe purchases.
                """.trimIndent(),
            ),
        )

        val result = provider.smartAutofill(
            SmartAutofillRequest(
                partialInput = SmartAutofillInput(
                    description = "Cafe",
                    transactionType = "expense",
                    amountMinor = 5000,
                ),
                candidates = SmartAutofillCandidates(
                    categories = listOf(SmartAutofillCategory("food", "Food", "expense")),
                ),
                history = emptyList(),
                occurredAtMillis = 1,
            ),
        ) as AiInferenceResult.Success

        assertEquals("food", result.value.suggestion?.categoryId)
        assertEquals(null, result.value.suggestion?.accountId)
        assertEquals(null, result.value.suggestion?.amountMinor)
        assertEquals(AiResultQuality.High, result.value.confidence)
    }

    @Test
    fun `inference timeout is reported as failure`() = runTest {
        val provider = MlKitAiProvider(
            runtime = FakeRuntime(OnDeviceModelStatus.Available, generationDelayMillis = 100),
            inferenceTimeoutMillis = 10,
        )

        val result = provider.generateMonthlySummary(request())

        assertTrue(result is AiInferenceResult.Failure)
        assertEquals("On-device inference timed out.", (result as AiInferenceResult.Failure).reason)
    }

    private fun request() = MonthlySummaryRequest(
        periodLabel = "June 2026",
        startMillis = 1,
        endMillis = 2,
        currencyCode = "USD",
        totalIncomeMinor = 300_000,
        totalExpenseMinor = 200_000,
        transactionCount = 12,
    )

    private class FakeRuntime(
        private val initialStatus: OnDeviceModelStatus,
        private val preparedStatus: OnDeviceModelStatus = initialStatus,
        private val failure: Throwable? = null,
        private val output: String = """
            TITLE: June overview
            SUMMARY: Spending stayed within income.
            INSIGHT: Food was the largest category.
            WARNING: Transport exceeded its budget.
        """.trimIndent(),
        private val generationDelayMillis: Long = 0,
    ) : OnDeviceGenAiRuntime {
        var prepareCalls = 0
        var generateCalls = 0

        override suspend fun status() = initialStatus

        override suspend fun prepare(): OnDeviceModelStatus {
            prepareCalls++
            return preparedStatus
        }

        override suspend fun generate(prompt: String): String {
            generateCalls++
            if (generationDelayMillis > 0) delay(generationDelayMillis)
            failure?.let { throw it }
            return output
        }
    }
}
