package com.mojtaba.folentra.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.data.model.TransactionTagLink
import com.mojtaba.folentra.core.database.FolentraDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalTagRepositoryTest {
    private lateinit var database: FolentraDatabase
    private lateinit var categoryRepository: LocalCategoryRepository
    private lateinit var transactionRepository: LocalTransactionRepository
    private lateinit var tagRepository: LocalTagRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FolentraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryRepository = LocalCategoryRepository(database.categoryDao())
        transactionRepository = LocalTransactionRepository(database.transactionDao())
        tagRepository = LocalTagRepository(database.tagDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertGetDelete_roundTripsTag() = runTest {
        tagRepository.insert(testTag())

        assertEquals("Weekend", tagRepository.getById("tag-weekend")?.name)

        val deleted = tagRepository.deleteById("tag-weekend")

        assertEquals(true, deleted)
        assertNull(tagRepository.getById("tag-weekend"))
    }

    @Test
    fun observeTagsForTransaction_emitsLinkedTags() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction())
        tagRepository.insert(testTag(id = "tag-weekend", name = "Weekend"))
        tagRepository.insert(testTag(id = "tag-food", name = "Food"))

        tagRepository.addTagToTransaction(
            TransactionTagLink(
                transactionId = "transaction-1",
                tagId = "tag-weekend",
            ),
        )

        val observedIds = tagRepository.observeTagsForTransaction("transaction-1").first().map { it.id }

        assertEquals(listOf("tag-weekend"), observedIds)
    }

    @Test
    fun observeTagsForTransaction_reflectsRelationshipUpdates() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction())
        tagRepository.insert(testTag(id = "tag-weekend", name = "Weekend"))

        val observedTags = tagRepository.observeTagsForTransaction("transaction-1")
        assertEquals(emptyList<String>(), observedTags.first().map { it.id })

        tagRepository.addTagToTransaction(TransactionTagLink("transaction-1", "tag-weekend"))
        assertEquals(listOf("tag-weekend"), observedTags.first().map { it.id })

        tagRepository.removeTagFromTransaction("transaction-1", "tag-weekend")
        assertEquals(emptyList<String>(), observedTags.first().map { it.id })
    }

    @Test
    fun removeTagFromTransaction_removesOnlyRequestedLink() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction())
        tagRepository.insert(testTag(id = "tag-weekend", name = "Weekend"))
        tagRepository.insert(testTag(id = "tag-food", name = "Food"))
        tagRepository.addTagToTransaction(TransactionTagLink("transaction-1", "tag-weekend"))
        tagRepository.addTagToTransaction(TransactionTagLink("transaction-1", "tag-food"))

        val removed = tagRepository.removeTagFromTransaction("transaction-1", "tag-weekend")
        val observedIds = tagRepository.observeTagsForTransaction("transaction-1").first().map { it.id }

        assertEquals(true, removed)
        assertEquals(listOf("tag-food"), observedIds)
    }

    @Test
    fun deletingTransaction_cascadesTagLinksThroughRoom() = runTest {
        categoryRepository.insert(testCategory())
        transactionRepository.insert(testTransaction())
        tagRepository.insert(testTag(id = "tag-weekend", name = "Weekend"))
        tagRepository.addTagToTransaction(TransactionTagLink("transaction-1", "tag-weekend"))

        transactionRepository.deleteById("transaction-1")

        assertEquals(emptyList<String>(), tagRepository.observeTagsForTransaction("transaction-1").first().map { it.id })
    }
}
