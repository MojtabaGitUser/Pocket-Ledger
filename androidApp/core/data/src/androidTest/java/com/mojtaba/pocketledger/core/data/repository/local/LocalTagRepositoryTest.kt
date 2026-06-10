package com.mojtaba.pocketledger.core.data.repository.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.pocketledger.core.data.model.TransactionTagLink
import com.mojtaba.pocketledger.core.database.PocketLedgerDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocalTagRepositoryTest {
    private lateinit var database: PocketLedgerDatabase
    private lateinit var categoryRepository: LocalCategoryRepository
    private lateinit var transactionRepository: LocalTransactionRepository
    private lateinit var tagRepository: LocalTagRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PocketLedgerDatabase::class.java)
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
}
