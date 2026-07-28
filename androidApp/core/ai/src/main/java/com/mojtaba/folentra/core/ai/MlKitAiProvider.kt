package com.mojtaba.folentra.core.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.Locale

class MlKitAiProvider(
    private val runtime: OnDeviceGenAiRuntime = MlKitGenAiRuntime(),
    private val inferenceTimeoutMillis: Long = 30_000,
    private val availabilityTimeoutMillis: Long = 5_000,
) : AiProvider {
    override val type: AiProviderType = AiProviderType.MlKit
    override val capabilities: AiProviderCapabilities = AiProviderCapabilities(
        monthlySummaries = true,
        semanticSearch = true,
        smartAutofill = true,
    )

    override fun availability(): AiProviderAvailability =
        AiProviderAvailability.Unavailable("On-device model availability has not been checked yet.")

    override suspend fun currentAvailability(): AiProviderAvailability = try {
        when (withTimeout(availabilityTimeoutMillis) { runtime.status() }) {
            OnDeviceModelStatus.Available,
            OnDeviceModelStatus.Downloadable,
            -> AiProviderAvailability.Available
            OnDeviceModelStatus.Downloading ->
                AiProviderAvailability.Unavailable("Gemini Nano is downloading.")
            OnDeviceModelStatus.Unavailable ->
                AiProviderAvailability.Unavailable("Gemini Nano is unsupported on this device.")
        }
    } catch (timeout: TimeoutCancellationException) {
        AiProviderAvailability.Unavailable("Timed out while checking Gemini Nano availability.")
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (throwable: Throwable) {
        AiProviderAvailability.Unavailable(throwable.message ?: "Unable to check Gemini Nano availability.")
    }

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

    override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> {
        val eligibleDocuments = request.documents
            .asSequence()
            .filter { document ->
                (request.filters.categoryIds.isEmpty() || document.categoryId in request.filters.categoryIds) &&
                    (request.filters.accountIds.isEmpty() || document.accountId in request.filters.accountIds)
            }
            .take(MAX_SEMANTIC_DOCUMENTS)
            .toList()
        if (request.query.isBlank() || eligibleDocuments.isEmpty()) {
            return AiInferenceResult.Success(SemanticSearchResult(emptyList()), type)
        }
        return infer(buildSemanticSearchPrompt(request.query, eligibleDocuments)) { output ->
            parseSemanticSearchOutput(
                output = output,
                allowedIds = eligibleDocuments.mapTo(linkedSetOf()) { it.id },
                maxResults = request.maxResults,
            )
        }
    }

    override suspend fun smartAutofill(request: SmartAutofillRequest): AiInferenceResult<SmartAutofillResult> {
        if (request.partialInput.description.isBlank() && request.partialInput.note.isNullOrBlank()) {
            return AiInferenceResult.Success(
                SmartAutofillResult(null, type, AiResultQuality.Low),
                type,
            )
        }
        return infer(buildSmartAutofillPrompt(request)) { output ->
            parseSmartAutofillOutput(output, request)
        }
    }

    private suspend fun <T> infer(prompt: String, transform: (String) -> T): AiInferenceResult<T> = try {
        withTimeout(inferenceTimeoutMillis) {
            when (runtime.prepare()) {
                OnDeviceModelStatus.Available -> {
                    val output = runtime.generate(prompt)
                    if (output.isBlank()) {
                        AiInferenceResult.Failure(type, "Gemini Nano returned an empty response.")
                    } else {
                        runCatching { transform(output) }
                            .fold(
                                onSuccess = { AiInferenceResult.Success(it, type) },
                                onFailure = {
                                    AiInferenceResult.Failure(
                                        type,
                                        it.message ?: "Gemini Nano returned an invalid response.",
                                    )
                                },
                            )
                    }
                }
                OnDeviceModelStatus.Downloadable -> AiInferenceResult.Unavailable(type, "Gemini Nano must be downloaded.")
                OnDeviceModelStatus.Downloading -> AiInferenceResult.Unavailable(type, "Gemini Nano is downloading.")
                OnDeviceModelStatus.Unavailable -> AiInferenceResult.Unavailable(type, "Gemini Nano is unsupported on this device.")
            }
        }
    } catch (timeout: TimeoutCancellationException) {
        AiInferenceResult.Failure(type, "On-device inference timed out.")
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

    private fun buildSemanticSearchPrompt(
        query: String,
        documents: List<SemanticSearchDocument>,
    ): String = buildString {
        appendLine("## Task")
        appendLine("Rank only the transaction records that are semantically relevant to the query.")
        appendLine("Return at most 20 lines. Format each line exactly: MATCH:<id>|<score 1-100>|<short reason>")
        appendLine("Do not return an id that is absent from the records. Return NONE when nothing is relevant.")
        appendLine("## Query")
        appendLine(query.sanitized(MAX_QUERY_CHARS))
        appendLine("## Records")
        documents.forEach { document ->
            append("ID=").append(document.id.sanitized(MAX_ID_CHARS))
            append("; TITLE=").append(document.title.sanitized(MAX_DOCUMENT_FIELD_CHARS))
            append("; TEXT=").append(document.body.sanitized(MAX_DOCUMENT_FIELD_CHARS))
            if (document.metadata.isNotEmpty()) {
                append("; META=")
                append(
                    document.metadata.entries
                        .sortedBy { it.key }
                        .take(6)
                        .joinToString(",") { "${it.key.sanitized(40)}=${it.value.sanitized(80)}" },
                )
            }
            appendLine()
        }
    }

    private fun parseSemanticSearchOutput(
        output: String,
        allowedIds: Set<String>,
        maxResults: Int,
    ): SemanticSearchResult {
        if (output.lineSequence().any { it.trim().equals("NONE", ignoreCase = true) }) {
            return SemanticSearchResult(emptyList())
        }
        val matches = output.lineSequence()
            .map(String::trim)
            .filter { it.startsWith("MATCH:", ignoreCase = true) }
            .mapNotNull { line ->
                val parts = line.substringAfter(':').split('|', limit = 3)
                val id = parts.getOrNull(0)?.trim().orEmpty()
                val score = parts.getOrNull(1)?.trim()?.toIntOrNull()?.coerceIn(1, 100)
                if (id !in allowedIds || score == null) {
                    null
                } else {
                    SemanticSearchMatch(
                        id = id,
                        relevanceScore = score,
                        reason = parts.getOrNull(2)?.trim()?.take(120)?.takeIf(String::isNotBlank),
                    )
                }
            }
            .distinctBy { it.id }
            .sortedByDescending { it.relevanceScore }
            .take(maxResults.coerceAtLeast(0))
            .toList()
        require(matches.isNotEmpty()) { "Gemini Nano returned no valid semantic matches." }
        return SemanticSearchResult(matches.map { it.id }, matches)
    }

    private fun buildSmartAutofillPrompt(request: SmartAutofillRequest): String = buildString {
        appendLine("## Task")
        appendLine("Suggest transaction fields using only the candidates and local history.")
        appendLine("Never replace fields already supplied by the user.")
        appendLine("Return exactly these lines: CATEGORY_ID:, ACCOUNT_ID:, AMOUNT_MINOR:, RECURRING:, NOTE:, CONFIDENCE:, REASON:")
        appendLine("Use NONE for fields without a safe suggestion. CONFIDENCE must be LOW, MEDIUM, or HIGH.")
        appendLine("## Current input")
        appendLine("description=${request.partialInput.description.sanitized(MAX_DOCUMENT_FIELD_CHARS)}")
        appendLine("note=${request.partialInput.note.orEmpty().sanitized(MAX_DOCUMENT_FIELD_CHARS)}")
        appendLine("type=${request.partialInput.transactionType.sanitized(40)}")
        appendLine("categoryId=${request.partialInput.categoryId ?: "NONE"}")
        appendLine("accountId=${request.partialInput.accountId ?: "NONE"}")
        appendLine("amountMinor=${request.partialInput.amountMinor ?: "NONE"}")
        appendLine("## Allowed categories")
        request.candidates.categories.take(30).forEach {
            appendLine("${it.id.sanitized(MAX_ID_CHARS)}|${it.displayName.sanitized(80)}|${it.type.sanitized(30)}")
        }
        appendLine("## Allowed accounts")
        request.candidates.accounts.take(20).forEach {
            appendLine("${it.id.sanitized(MAX_ID_CHARS)}|${it.displayName.sanitized(80)}")
        }
        appendLine("## Similar local history")
        request.history.take(30).forEach {
            appendLine(
                listOf(
                    it.description.orEmpty().sanitized(100),
                    it.note.orEmpty().sanitized(100),
                    it.transactionType.sanitized(30),
                    it.categoryId ?: "NONE",
                    it.accountId ?: "NONE",
                    it.amountMinor?.toString() ?: "NONE",
                    it.isRecurring.toString(),
                ).joinToString("|"),
            )
        }
    }

    private fun parseSmartAutofillOutput(
        output: String,
        request: SmartAutofillRequest,
    ): SmartAutofillResult {
        val fields = output.lineSequence()
            .map(String::trim)
            .filter { ':' in it }
            .associate { it.substringBefore(':').uppercase(Locale.US) to it.substringAfter(':').trim() }
        val categoryId = fields.optional("CATEGORY_ID")
            ?.takeIf { id ->
                request.partialInput.categoryId == null &&
                    request.candidates.categories.any {
                        it.id == id && it.type.equals(request.partialInput.transactionType, ignoreCase = true)
                    }
            }
        val accountId = fields.optional("ACCOUNT_ID")
            ?.takeIf { id ->
                request.partialInput.accountId == null && request.candidates.accounts.any { it.id == id }
            }
        val amountMinor = fields.optional("AMOUNT_MINOR")
            ?.toLongOrNull()
            ?.takeIf { it > 0L && request.partialInput.amountMinor == null }
        val recurring = fields.optional("RECURRING")
            ?.lowercase(Locale.US)
            ?.let { value ->
                when (value) {
                    "true" -> true
                    "false" -> false
                    else -> null
                }
            }
        val note = fields.optional("NOTE")
            ?.take(MAX_SUGGESTED_NOTE_CHARS)
            ?.takeIf { request.partialInput.note.isNullOrBlank() }
        val confidence = when (fields["CONFIDENCE"]?.uppercase(Locale.US)) {
            "HIGH" -> AiResultQuality.High
            "MEDIUM" -> AiResultQuality.Medium
            else -> AiResultQuality.Low
        }
        val hasSuggestion = categoryId != null || accountId != null || amountMinor != null || recurring != null || note != null
        val suggestion = if (hasSuggestion) {
            SmartAutofillSuggestion(
                categoryId = categoryId,
                accountId = accountId,
                amountMinor = amountMinor,
                recurring = recurring,
                note = note,
                reason = fields["REASON"]?.take(160)?.takeIf(String::isNotBlank)
                    ?: "Matched local transaction context.",
            )
        } else {
            null
        }
        return SmartAutofillResult(suggestion, type, confidence)
    }

    private fun Map<String, String>.optional(key: String): String? =
        get(key)?.takeUnless { it.isBlank() || it.equals("NONE", ignoreCase = true) }

    private fun String.sanitized(maxLength: Int = 160): String =
        replace(Regex("[\\r\\n|]+"), " ").trim().take(maxLength)

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

    private companion object {
        const val MAX_SEMANTIC_DOCUMENTS = 60
        const val MAX_QUERY_CHARS = 240
        const val MAX_ID_CHARS = 80
        const val MAX_DOCUMENT_FIELD_CHARS = 240
        const val MAX_SUGGESTED_NOTE_CHARS = 240
    }
}
