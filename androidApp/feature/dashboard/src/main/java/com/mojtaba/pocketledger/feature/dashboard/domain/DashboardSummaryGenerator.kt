package com.mojtaba.pocketledger.feature.dashboard.domain

import com.mojtaba.pocketledger.core.ai.AiFallbackStrategy
import com.mojtaba.pocketledger.core.ai.AiInferenceResult
import com.mojtaba.pocketledger.core.ai.AiSummaryRequest
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardInsight
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardSummary
import java.util.Locale
import kotlin.math.abs

class DashboardSummaryGenerator(
    private val aiFallbackStrategy: AiFallbackStrategy,
) {
    suspend fun generate(input: DashboardSummaryInput): DashboardSummary {
        val summary = DashboardSummaryCalculator.calculate(input)
        val aiSummary = aiFallbackStrategy.generateSummary(summary.toAiSummaryRequest())
        return when (aiSummary) {
            is AiInferenceResult.Success -> {
                val text = aiSummary.value.text.trim()
                if (text.isEmpty()) {
                    summary
                } else {
                    summary.copy(insights = summary.insights + DashboardInsight.AiMonthlySummary(text))
                }
            }
            is AiInferenceResult.Unavailable,
            is AiInferenceResult.Failure,
            -> summary
        }
    }

    private fun DashboardSummary.toAiSummaryRequest(): AiSummaryRequest =
        AiSummaryRequest(
            periodLabel = period.label,
            facts = buildList {
                val netDirection = when {
                    cashFlow.netMinor > 0L -> "positive"
                    cashFlow.netMinor < 0L -> "negative"
                    else -> "flat"
                }
                add(
                    "${period.label} cash flow was $netDirection at " +
                        "${abs(cashFlow.netMinor).toMajorUnits()} ${cashFlow.currencyCode}.",
                )
                topCategories.firstOrNull()?.let { category ->
                    add(
                        "Top spending was ${category.categoryName} at " +
                            "${category.amountMinor.toMajorUnits()} ${category.currencyCode}.",
                    )
                }
                budgetProgress.firstOrNull { it.progressPercent >= 100.0 }?.let { budget ->
                    add(
                        "${budget.budgetName} exceeded its monthly budget at " +
                            "${String.format(Locale.US, "%.0f", budget.progressPercent)} percent.",
                    )
                }
            },
        )

    private fun Long.toMajorUnits(): String =
        String.format(Locale.US, "%.2f", this / 100.0)
}
