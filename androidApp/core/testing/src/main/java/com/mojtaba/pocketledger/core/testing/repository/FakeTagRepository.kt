package com.mojtaba.pocketledger.core.testing.repository

import com.mojtaba.pocketledger.core.data.model.LedgerTag
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.contract.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTagRepository(
    initialTags: List<LedgerTag> = emptyList(),
    initialLinks: List<TransactionTagLink> = emptyList(),
) : TagRepository {
    private val tags = MutableStateFlow(initialTags.associateBy { it.id })
    private val transactionTagIds = MutableStateFlow(
        initialLinks
            .groupBy { it.transactionId }
            .mapValues { (_, links) -> links.map { it.tagId }.toSet() },
    )

    override val repositoryName: String = "fake-tags"

    override fun observeSyncState(): Flow<SyncState> = flowOf(SyncState.localOnly())

    override suspend fun insert(tag: LedgerTag) = upsert(tag)

    override suspend fun insertAll(tags: List<LedgerTag>) = upsertAll(tags)

    override suspend fun upsert(tag: LedgerTag) {
        tags.update { it + (tag.id to tag) }
    }

    override suspend fun upsertAll(tags: List<LedgerTag>) {
        this.tags.update { current -> current + tags.associateBy { it.id } }
    }

    override suspend fun delete(tag: LedgerTag) {
        tags.update { it - tag.id }
    }

    override suspend fun deleteById(id: String): Boolean {
        val existed = id in tags.value
        tags.update { it - id }
        return existed
    }

    override suspend fun getById(id: String): LedgerTag? = tags.value[id]

    override fun observeById(id: String): Flow<LedgerTag?> = tags.map { it[id] }

    override fun observeTags(): Flow<List<LedgerTag>> = tags.map { it.values.sortedForDisplay() }

    override suspend fun addTagToTransaction(link: TransactionTagLink) {
        transactionTagIds.update { current ->
            current + (link.transactionId to (current[link.transactionId].orEmpty() + link.tagId))
        }
    }

    override suspend fun removeTagFromTransaction(transactionId: String, tagId: String): Boolean {
        val existing = transactionTagIds.value[transactionId].orEmpty()
        transactionTagIds.update { current ->
            current + (transactionId to (existing - tagId))
        }
        return tagId in existing
    }

    override fun observeTagsForTransaction(transactionId: String): Flow<List<LedgerTag>> =
        combine(tags, transactionTagIds) { tagMap, linkMap ->
            linkMap[transactionId].orEmpty()
                .mapNotNull(tagMap::get)
                .sortedForDisplay()
        }

    fun tagIdsForTransaction(transactionId: String): Set<String> =
        transactionTagIds.value[transactionId].orEmpty()

    fun snapshot(): List<LedgerTag> = tags.value.values.sortedForDisplay()

    private fun Iterable<LedgerTag>.sortedForDisplay(): List<LedgerTag> =
        sortedWith(compareBy<LedgerTag> { it.name.lowercase() }.thenBy { it.id })
}
