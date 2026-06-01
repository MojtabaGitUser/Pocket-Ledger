package com.mojtaba.pocketledger.core.data.repository.local

import com.mojtaba.pocketledger.core.data.mapper.asEntity
import com.mojtaba.pocketledger.core.data.mapper.asExternalModel
import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.database.dao.TagDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalTagRepository(
    private val tagDao: TagDao,
) : TagRepository {
    override suspend fun insert(tag: LedgerTag) {
        tagDao.insert(tag.asEntity())
    }

    override suspend fun insertAll(tags: List<LedgerTag>) {
        tagDao.insertAll(tags.map { it.asEntity() })
    }

    override suspend fun upsert(tag: LedgerTag) {
        tagDao.upsert(tag.asEntity())
    }

    override suspend fun upsertAll(tags: List<LedgerTag>) {
        tagDao.upsertAll(tags.map { it.asEntity() })
    }

    override suspend fun delete(tag: LedgerTag) {
        tagDao.delete(tag.asEntity())
    }

    override suspend fun deleteById(id: String): Boolean = tagDao.deleteById(id) > 0

    override suspend fun getById(id: String): LedgerTag? =
        tagDao.getById(id)?.asExternalModel()

    override fun observeById(id: String): Flow<LedgerTag?> =
        tagDao.observeById(id).map { it?.asExternalModel() }

    override fun observeTags(): Flow<List<LedgerTag>> =
        tagDao.observeTags().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun addTagToTransaction(link: TransactionTagLink) {
        tagDao.addTagToTransaction(link.asEntity())
    }

    override suspend fun removeTagFromTransaction(transactionId: String, tagId: String): Boolean =
        tagDao.removeTagFromTransaction(transactionId, tagId) > 0

    override fun observeTagsForTransaction(transactionId: String): Flow<List<LedgerTag>> =
        tagDao.observeTagsForTransaction(transactionId).map { entities ->
            entities.map { it.asExternalModel() }
        }
}
