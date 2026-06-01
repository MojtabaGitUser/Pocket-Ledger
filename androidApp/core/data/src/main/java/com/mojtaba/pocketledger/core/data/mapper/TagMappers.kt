package com.mojtaba.pocketledger.core.data.mapper

import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.database.model.TagEntity
import com.mojtaba.pocketledger.core.database.model.TransactionTagCrossRef

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
