package com.mojtaba.pocketledger.feature.dashboard.presentation.model

import com.mojtaba.pocketledger.core.designsystem.component.AmountDisplay
import com.mojtaba.pocketledger.core.designsystem.component.AmountTone
import com.mojtaba.pocketledger.feature.dashboard.model.BudgetProgressStatus
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardInsight
import com.mojtaba.pocketledger.feature.dashboard.model.DashboardTransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

object DashboardFormatters {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    fun amount(
        amountMinor: Long,
        currencyCode: String,
        includeSign: Boolean = false,
        tone: AmountTone = AmountTone.Neutral,
    ): AmountDisplay {
        val text = formatAmountMinor(
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            includeSign = includeSign,
        )
        return AmountDisplay(
            text = text,
            tone = tone,
            contentDescription = text,
        )
    }

    fun formatAmountMinor(
        amountMinor: Long,
        currencyCode: String,
        includeSign: Boolean = false,
    ): String {
        val amount = BigDecimal.valueOf(abs(amountMinor))
            .movePointLeft(2)
            .setScale(2, RoundingMode.HALF_UP)
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        runCatching {
            formatter.currency = Currency.getInstance(currencyCode.uppercase(Locale.US))
        }
        val formatted = formatter.format(amount)
        return when {
            !includeSign || amountMinor == 0L -> formatted
            amountMinor > 0L -> "+$formatted"
            else -> "-$formatted"
        }
    }

    fun percent(value: Double): String = "${value.roundToInt()}%"

    fun date(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).format(dateFormatter)

    fun budgetStatusLabel(status: BudgetProgressStatus): String =
        when (status) {
            BudgetProgressStatus.NoLimit -> "No limit"
            BudgetProgressStatus.OnTrack -> "On track"
            BudgetProgressStatus.NearLimit -> "Near limit"
            BudgetProgressStatus.Exceeded -> "Exceeded"
        }

    fun transactionTypeLabel(type: DashboardTransactionType): String =
        when (type) {
            DashboardTransactionType.Income -> "Income"
            DashboardTransactionType.Expense -> "Expense"
            DashboardTransactionType.Unknown -> "Unknown"
        }

    fun insightTitle(insight: DashboardInsight): String =
        when (insight) {
            DashboardInsight.NoData -> "No data yet"
            is DashboardInsight.AiMonthlySummary -> "Monthly summary"
            is DashboardInsight.PositiveCashFlow -> "Positive cash flow"
            is DashboardInsight.NegativeCashFlow -> "Negative cash flow"
            is DashboardInsight.OverspendingCategory -> "Concentrated spending"
            is DashboardInsight.BudgetNearLimit -> "Budget near limit"
            is DashboardInsight.BudgetExceeded -> "Budget exceeded"
        }

    fun insightMessage(insight: DashboardInsight): String =
        when (insight) {
            DashboardInsight.NoData -> "Add transactions to start seeing dashboard insights."
            is DashboardInsight.AiMonthlySummary -> insight.text
            is DashboardInsight.PositiveCashFlow ->
                "Net cash flow is ${formatAmountMinor(insight.netMinor, insight.currencyCode, includeSign = true)}."
            is DashboardInsight.NegativeCashFlow ->
                "Net cash flow is ${formatAmountMinor(insight.netMinor, insight.currencyCode, includeSign = true)}."
            is DashboardInsight.OverspendingCategory ->
                "${insight.categoryName} accounts for ${percent(insight.percentageOfExpense)} of expenses."
            is DashboardInsight.BudgetNearLimit ->
                "${insight.budgetName} is at ${percent(insight.progressPercent)} of its limit."
            is DashboardInsight.BudgetExceeded ->
                "${insight.budgetName} is over budget at ${percent(insight.progressPercent)}."
        }
}
