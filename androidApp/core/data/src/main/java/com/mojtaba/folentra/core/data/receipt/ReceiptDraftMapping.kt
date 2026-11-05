package com.mojtaba.folentra.core.data.receipt

import com.mojtaba.folentra.core.data.model.DraftTransaction
import java.math.BigDecimal
import java.time.ZoneId

/** A rescan fills blanks only. User corrections, currency, category and type always win. */
fun ParsedReceipt.fillMissing(draft: DraftTransaction, zone: ZoneId): DraftTransaction = draft.copy(
    amountInput = draft.amountInput.ifBlank { amountMinor.value?.let { BigDecimal.valueOf(it, 2).toPlainString() }.orEmpty() },
    merchant = draft.merchant.ifBlank { merchant.value.orEmpty() },
    occurredAt = draft.occurredAt ?: date.value?.atStartOfDay(zone)?.toInstant()?.toEpochMilli(),
    currencyCode = draft.currencyCode.ifBlank { currency.value.orEmpty() },
)
