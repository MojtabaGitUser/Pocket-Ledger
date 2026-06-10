package com.mojtaba.pocketledger.core.data.repository

import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.data.repository.contract.OfflineFirstRepository
import kotlinx.coroutines.flow.Flow

interface TagRepository : OfflineFirstRepository {
    suspend fun insert(tag: LedgerTag)

    suspend fun insertAll(tags: List<LedgerTag>)

    suspend fun upsert(tag: LedgerTag)

    suspend fun upsertAll(tags: List<LedgerTag>)

    suspend fun delete(tag: LedgerTag)

    suspend fun deleteById(id: String): Boolean

    suspend fun getById(id: String): LedgerTag?

    fun observeById(id: String): Flow<LedgerTag?>

    fun observeTags(): Flow<List<LedgerTag>>

    suspend fun addTagToTransaction(link: TransactionTagLink)

    suspend fun removeTagFromTransaction(transactionId: String, tagId: String): Boolean

    fun observeTagsForTransaction(transactionId: String): Flow<List<LedgerTag>>
}
