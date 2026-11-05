package com.mojtaba.folentra.core.data.receipt

import java.time.LocalDate

/** Coordinates are in the oriented, sampled image. No SDK types cross this boundary. */
data class ReceiptBounds(val left: Int, val top: Int, val right: Int, val bottom: Int)
data class ReceiptLine(val text: String, val bounds: ReceiptBounds? = null, val block: Int = 0)
data class ReceiptText(val lines: List<ReceiptLine>)

fun interface ReceiptTextRecognizer {
    suspend fun recognize(imageUri: String): ReceiptText
}

enum class ReceiptConfidence { MISSING, UNCERTAIN, SUPPORTED }
data class ReceiptField<out T>(
    val value: T?,
    val confidence: ReceiptConfidence,
    val evidence: String? = null,
    val reviewRecommended: Boolean = true,
)
data class ParsedReceipt(
    val merchant: ReceiptField<String>,
    val amountMinor: ReceiptField<Long>,
    val date: ReceiptField<LocalDate>,
    val currency: ReceiptField<String>,
)
