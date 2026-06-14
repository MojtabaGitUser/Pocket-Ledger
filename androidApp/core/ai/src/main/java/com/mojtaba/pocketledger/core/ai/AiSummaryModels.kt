package com.mojtaba.pocketledger.core.ai

data class AiSummaryRequest(
    val periodLabel: String,
    val facts: List<String>,
    val maxSentences: Int = 3,
)

data class AiSummaryResult(
    val text: String,
)
