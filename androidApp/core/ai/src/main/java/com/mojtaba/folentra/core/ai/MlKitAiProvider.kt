package com.mojtaba.folentra.core.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

class MlKitAiProvider(
    private val runtime: OnDeviceGenAiRuntime = MlKitGenAiRuntime(),
    private val inferenceTimeoutMillis: Long = 30_000,
) : AiProvider {
    override val type: AiProviderType = AiProviderType.MlKit
    override val capabilities: AiProviderCapabilities = AiProviderCapabilities(
        monthlySummaries = true,
        semanticSearch = false,
        smartAutofill = false,
    )

    // Actual device/model capability is checked immediately before every inference.
    override fun availability(): AiProviderAvailability = AiProviderAvailability.Available

    override suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult> =
        infer(buildSummaryPrompt(request)) { output -> AiSummaryResult(output) }

    override suspend fun generateMonthlySummary(
        request: MonthlySummaryRequest,
    ): AiInferenceResult<MonthlySummaryResult> = infer(buildMonthlyPrompt(request)) { output ->
        val parsed = parseMonthlyOutput(output)
        MonthlySummaryResult(
            title = parsed.title.ifBlank { request.periodLabel },
            summaryText = parsed.summary.ifBlank { output },
            insights = parsed.insights,
            warnings = parsed.warnings,
            providerType = type,
            quality = AiResultQuality.High,
            privacyMode = request.privacyMode,
        )
    }

    override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> =
        AiInferenceResult.Unavailable(type, "ML Kit semantic search is not implemented by issue #92.")

    override suspend fun smartAutofill(request: SmartAutofillRequest): AiInferenceResult<SmartAutofillResult> =
        AiInferenceResult.Unavailable(type, "ML Kit smart autofill is not implemented by issue #92.")

    private suspend fun <T> infer(prompt: String, transform: (String) -> T): AiInferenceResult<T> = try {
        when (runtime.prepare()) {
            OnDeviceModelStatus.Available -> {
                val output = withTimeout(inferenceTimeoutMillis) { runtime.generate(prompt) }
                if (output.isBlank()) {
                    AiInferenceResult.Failure(type, "Gemini Nano returned an empty response.")
                } else {
                    AiInferenceResult.Success(transform(output), type)
                }
            }
            OnDeviceModelStatus.Downloadable -> AiInferenceResult.Unavailable(type, "Gemini Nano must be downloaded.")
            OnDeviceModelStatus.Downloading -> AiInferenceResult.Unavailable(type, "Gemini Nano is downloading.")
            OnDeviceModelStatus.Unavailable -> AiInferenceResult.Unavailable(type, "Gemini Nano is unsupported on this device.")
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (throwable: Throwable) {
        AiInferenceResult.Failure(type, throwable.message ?: "On-device inference failed.")
    }

    private fun buildSummaryPrompt(request: AiSummaryRequest): String = buildString {
        appendLine("## Task")
        appendLine("Write a factual personal-finance summary in at most ${request.maxSentences} sentences.")
        appendLine("Do not invent facts or provide investment advice. Return plain text only.")
        appendLine("## Period")
        appendLine(request.periodLabel.sanitized())
        appendLine("## Aggregate facts")
        request.facts.forEach { appendLine("- ${it.sanitized()}") }
    }

    private fun buildMonthlyPrompt(request: MonthlySummaryRequest): String = buildString {
        appendLine("## Task")
        appendLine("Summarize the supplied aggregate personal-finance facts. Do not invent causes, amounts, or transactions.")
        appendLine("Return exactly these line prefixes: TITLE:, SUMMARY:, INSIGHT:, WARNING:. Use multiple INSIGHT/WARNING lines if needed.")
        appendLine("Keep SUMMARY under 80 words. Do not give investment advice.")
        appendLine("## Aggregate data")
        appendLine("period=${request.periodLabel.sanitized()}")
        appendLine("currency=${request.currencyCode.sanitized()}")
        appendLine("incomeMinor=${request.totalIncomeMinor}")
        appendLine("expenseMinor=${request.totalExpenseMinor}")
        appendLine("transactionCount=${request.transactionCount}")
        request.categorySummaries.take(10).forEach {
            appendLine("category=${it.displayName?.sanitized() ?: "unnamed"}; expenseMinor=${it.totalExpenseMinor}; count=${it.transactionCount}")
        }
        request.budgetComparisons.take(10).forEach {
            appendLine("budget=${it.displayName.sanitized()}; spentMinor=${it.spentMinor}; budgetMinor=${it.budgetMinor}")
        }
        request.recurringHints.take(10).forEach {
            appendLine("recurring=${it.label.sanitized()}; count=${it.transactionCount}")
        }
    }

    private fun String.sanitized(): String = replace(Regex("[\\r\\n]+"), " ").take(160)

    private fun parseMonthlyOutput(output: String): ParsedMonthlyOutput {
        var title = ""
        var summary = ""
        val insights = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        output.lineSequence().map(String::trim).forEach { line ->
            when {
                line.startsWith("TITLE:", true) -> title = line.substringAfter(':').trim()
                line.startsWith("SUMMARY:", true) -> summary = line.substringAfter(':').trim()
                line.startsWith("INSIGHT:", true) -> line.substringAfter(':').trim().takeIf(String::isNotBlank)?.let(insights::add)
                line.startsWith("WARNING:", true) -> line.substringAfter(':').trim().takeIf(String::isNotBlank)?.let(warnings::add)
            }
        }
        return ParsedMonthlyOutput(title, summary, insights, warnings)
    }

    private data class ParsedMonthlyOutput(
        val title: String,
        val summary: String,
        val insights: List<String>,
        val warnings: List<String>,
    )
}
