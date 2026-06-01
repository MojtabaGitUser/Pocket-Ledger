package com.mojtaba.pocketledger.core.data.mapper

import com.mojtaba.pocketledger.core.data.model.LedgerCategory
import com.mojtaba.pocketledger.core.database.model.CategoryEntity

internal fun CategoryEntity.asExternalModel(): LedgerCategory = LedgerCategory(
    id = id,
    name = name,
    type = type,
    colorHex = colorHex,
    iconName = iconName,
    sortOrder = sortOrder,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun LedgerCategory.asEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    type = type,
    colorHex = colorHex,
    iconName = iconName,
    sortOrder = sortOrder,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
