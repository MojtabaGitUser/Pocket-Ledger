package com.mojtaba.pocketledger.core.data.model

data class LedgerCategory(
    val id: String,
    val name: String,
    val type: String,
    val colorHex: String? = null,
    val iconName: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
)
