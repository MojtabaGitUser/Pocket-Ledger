package com.mojtaba.pocketledger.core.ai

import com.mojtaba.pocketledger.shared.domain.search.SharedSearchDocument
import com.mojtaba.pocketledger.shared.domain.search.SharedSearchRanker
import java.util.Locale
import kotlin.math.abs

object RuleBasedAiProvider : AiProvider {
    override val type: AiProviderType = AiProviderType.RuleBased
    override val capabilities: AiProviderCapabilities = AiProviderCapabilities.LocalFinanceFeatures

    override fun availability(): AiProviderAvailability = AiProviderAvailability.Available

    override suspend fun generateSummary(request: AiSummaryRequest): AiInferenceResult<AiSummaryResult> {
        val facts = request.facts.mapNotNull { it.cleanOrNull() }
        val text = if (facts.isEmpty()) {
            "No local activity was available for ${request.periodLabel}."
        } else {
            facts.take(request.maxSentences.coerceAtLeast(1)).joinToString(separator = " ")
        }
        return AiInferenceResult.Success(
            value = AiSummaryResult(text),
            providerType = type,
        )
    }

    override suspend fun generateMonthlySummary(
        request: MonthlySummaryRequest,
    ): AiInferenceResult<MonthlySummaryResult> {
        val expense = abs(request.totalExpenseMinor)
        val net = request.totalIncomeMinor - expense
        val topCategories = request.categorySummaries
            .filter { it.totalExpenseMinor > 0L }
            .sortedWith(compareByDescending<AiCategorySummary> { it.totalExpenseMinor }.thenBy { it.displayName.orEmpty() })
            .take(3)
        val title = "${request.periodLabel} private summary"
        val summary = when {
            request.transactionCount == 0 -> "No local transactions were found for ${request.periodLabel}."
            request.totalIncomeMinor == 0L && expense > 0L ->
                "${request.periodLabel} included ${request.transactionCount} transactions and recorded expenses with no income in this period."
            expense == 0L && request.totalIncomeMinor > 0L ->
                "${request.periodLabel} included ${request.transactionCount} transactions and recorded income with no expenses in this period."
            else -> "${request.periodLabel} included ${request.transactionCount} transactions with ${net.netDirection()} net cash flow."
        }
        val insights = buildList {
            add("Income total: ${request.totalIncomeMinor.toMajorUnits()} ${request.currencyCode}.")
            add("Expense total: ${expense.toMajorUnits()} ${request.currencyCode}.")
            if (topCategories.isNotEmpty()) {
                add("Top spending category: ${topCategories.first().safeName()}.")
            }
            request.recurringHints.firstOrNull()?.let { hint ->
                add("Frequent local pattern: ${hint.label} appeared ${hint.transactionCount} times.")
            }
            request.budgetComparisons.firstOrNull { it.budgetMinor > 0L && it.spentMinor >= it.budgetMinor }?.let { budget ->
                add("One budget is at or above its configured limit: ${budget.displayName}.")
            }
        }
        val warnings = buildList {
            if (expense > request.totalIncomeMinor && expense > 0L) {
                add("Expenses were higher than income for this period.")
            }
            val top = topCategories.firstOrNull()
            if (top != null && expense > 0L && top.totalExpenseMinor * 100L >= expense * 50L) {
                add("Spending was concentrated in ${top.safeName()}.")
            }
        }
        val actions = buildList {
            if (request.transactionCount == 0) {
                add("Add transactions to generate more useful local insights.")
            } else {
                add("Review top categories for unusual local activity.")
            }
            if (warnings.isNotEmpty()) {
                add("Compare this month with your budget settings.")
            }
        }
        return AiInferenceResult.Success(
            value = MonthlySummaryResult(
                title = title,
                summaryText = summary,
                insights = insights,
                warnings = warnings,
                suggestedActions = actions,
                providerType = type,
                quality = if (request.transactionCount == 0) AiResultQuality.Low else AiResultQuality.Medium,
                privacyMode = request.privacyMode,
            ),
            providerType = type,
        )
    }

    override suspend fun semanticSearch(request: SemanticSearchRequest): AiInferenceResult<SemanticSearchResult> {
        val matches = SharedSearchRanker.rank(
            query = request.query,
            documents = request.documents.map { it.toSharedSearchDocument() },
            maxResults = request.maxResults,
            categoryIds = request.filters.categoryIds,
            accountIds = request.filters.accountIds,
        ).map { match ->
            SemanticSearchMatch(
                id = match.id,
                relevanceScore = match.relevanceScore,
                reason = match.reason,
            )
        }
        return AiInferenceResult.Success(
            value = SemanticSearchResult(rankedIds = matches.map { it.id }, matches = matches),
            providerType = type,
        )
    }
    override suspend fun smartAutofill(request: SmartAutofillRequest): AiInferenceResult<SmartAutofillResult> {
        val tokens = request.partialInput.description.tokens() + request.partialInput.note.tokens()
        if (tokens.isEmpty() || request.history.isEmpty()) {
            return AiInferenceResult.Success(
                SmartAutofillResult(null, type, AiResultQuality.Low),
                type,
            )
        }
        val ranked = request.history
            .filter { it.transactionType.equals(request.partialInput.transactionType, ignoreCase = true) }
            .map { item -> item to item.score(tokens) }
            .filter { (_, score) -> score > 0 }
            .sortedWith(compareByDescending<Pair<SmartAutofillHistoryItem, Int>> { it.second }.thenByDescending { it.first.occurredAtMillis })
        val best = ranked.firstOrNull()?.first
        val suggestion = if (best == null) {
            null
        } else {
            val recurringCount = ranked.take(5).count { it.first.isRecurring }
            val amount = ranked
                .mapNotNull { it.first.amountMinor }
                .take(3)
                .takeIf { it.size >= 2 && it.distinct().size == 1 }
                ?.first()
            SmartAutofillSuggestion(
                categoryId = best.categoryId?.takeIf { id -> request.partialInput.categoryId == null && request.candidates.categories.any { it.id == id } },
                accountId = best.accountId?.takeIf { id -> request.partialInput.accountId == null && request.candidates.accounts.any { it.id == id } },
                amountMinor = amount?.takeIf { request.partialInput.amountMinor == null },
                recurring = (recurringCount >= 2).takeIf { request.partialInput.description.isNotBlank() },
                note = null,
                reason = "Matched similar local transaction patterns.",
            )
        }
        val confidence = when {
            ranked.firstOrNull()?.second ?: 0 >= 6 -> AiResultQuality.High
            ranked.isNotEmpty() -> AiResultQuality.Medium
            else -> AiResultQuality.Low
        }
        return AiInferenceResult.Success(
            SmartAutofillResult(suggestion, type, confidence),
            type,
        )
    }

    private fun SemanticSearchDocument.toSharedSearchDocument(): SharedSearchDocument = SharedSearchDocument(
        id = id,
        title = title,
        body = body,
        metadata = metadata,
        categoryId = categoryId,
        accountId = accountId,
    )

    private fun SmartAutofillHistoryItem.score(queryTokens: Set<String>): Int {
        val textTokens = listOfNotNull(description, note).joinToString(" ").tokens()
        return queryTokens.sumOf { token ->
            when {
                token in textTokens -> 3
                textTokens.any { it.startsWith(token) } -> 2
                textTokens.any { it.contains(token) && token.length >= 4 } -> 1
                else -> 0
            }
        }
    }

    private fun AiCategorySummary.safeName(): String = displayName?.takeIf { it.isNotBlank() } ?: "Uncategorized"

    private fun Long.netDirection(): String = when {
        this > 0L -> "positive"
        this < 0L -> "negative"
        else -> "flat"
    }

    private fun Long.toMajorUnits(): String = String.format(Locale.US, "%.2f", this / 100.0)

    private fun String?.cleanOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String?.tokens(): Set<String> =
        orEmpty()
            .lowercase(Locale.US)
            .split(Regex("[^a-z0-9]+"))
            .mapNotNull { it.cleanOrNull() }
            .toSet()
}
