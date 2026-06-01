package com.mojtaba.pocketledger.core.data.model

data class LedgerTag(
    val id: String,
    val name: String,
    val colorHex: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
