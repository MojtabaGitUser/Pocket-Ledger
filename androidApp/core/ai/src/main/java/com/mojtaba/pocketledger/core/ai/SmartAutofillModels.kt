package com.mojtaba.pocketledger.core.ai

data class SmartAutofillRequest(
    val partialInput: SmartAutofillInput,
    val candidates: SmartAutofillCandidates,
    val history: List<SmartAutofillHistoryItem>,
    val occurredAtMillis: Long,
    val privacyMode: AiPrivacyMode = AiPrivacyMode.LocalRawAllowed,
)

data class SmartAutofillInput(
    val description: String,
    val note: String? = null,
    val transactionType: String,
    val categoryId: String? = null,
    val accountId: String? = null,
    val amountMinor: Long? = null,
)

data class SmartAutofillCandidates(
    val categories: List<SmartAutofillCategory> = emptyList(),
    val accounts: List<SmartAutofillAccount> = emptyList(),
)

data class SmartAutofillCategory(
    val id: String,
    val displayName: String,
    val type: String,
)

data class SmartAutofillAccount(
    val id: String,
    val displayName: String,
)

data class SmartAutofillHistoryItem(
    val transactionId: String,
    val description: String?,
    val note: String? = null,
    val transactionType: String,
    val categoryId: String? = null,
    val accountId: String? = null,
    val amountMinor: Long? = null,
    val isRecurring: Boolean = false,
    val occurredAtMillis: Long,
)

data class SmartAutofillResult(
    val suggestion: SmartAutofillSuggestion?,
    val providerType: AiProviderType,
    val confidence: AiResultQuality,
    val fallbackReason: String? = null,
)

data class SmartAutofillSuggestion(
    val categoryId: String? = null,
    val accountId: String? = null,
    val amountMinor: Long? = null,
    val recurring: Boolean? = null,
    val note: String? = null,
    val reason: String,
)
