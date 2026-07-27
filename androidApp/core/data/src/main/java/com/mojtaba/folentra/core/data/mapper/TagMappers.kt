package com.mojtaba.folentra.core.data.mapper

import com.mojtaba.folentra.core.data.model.LedgerTag
import com.mojtaba.folentra.core.data.model.TransactionTagLink
import com.mojtaba.folentra.core.database.model.TagEntity
import com.mojtaba.folentra.core.database.model.TransactionTagCrossRef

internal fun TagEntity.asExternalModel(): LedgerTag = LedgerTag(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun LedgerTag.asEntity(): TagEntity = TagEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun TransactionTagLink.asEntity(): TransactionTagCrossRef = TransactionTagCrossRef(
    transactionId = transactionId,
    tagId = tagId,
)
