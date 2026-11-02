package com.mojtaba.folentra.core.data.repository

import com.mojtaba.folentra.core.data.model.DraftTransaction
import kotlinx.coroutines.flow.Flow

interface DraftTransactionRepository {
    fun observePending(): Flow<List<DraftTransaction>>
    suspend fun save(draft: DraftTransaction)
    suspend fun confirm(draft: DraftTransaction): String
    suspend fun discard(id: String)
}
