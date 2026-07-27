package com.mojtaba.folentra.core.ai

import kotlinx.coroutines.test.runTest
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
            failure?.let { throw it }
            return """
                TITLE: June overview
                SUMMARY: Spending stayed within income.
                INSIGHT: Food was the largest category.
                WARNING: Transport exceeded its budget.
            """.trimIndent()
        }
    }
}
