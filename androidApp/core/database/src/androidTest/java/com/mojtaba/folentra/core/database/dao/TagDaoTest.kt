package com.mojtaba.folentra.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.database.FolentraDatabase
import com.mojtaba.folentra.core.database.testCategory
import com.mojtaba.folentra.core.database.testTag
import com.mojtaba.folentra.core.database.testTransaction
import com.mojtaba.folentra.core.database.testTransactionTagCrossRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TagDaoTest {
    private lateinit var database: FolentraDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var tagDao: TagDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FolentraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = database.categoryDao()
        tagDao = database.tagDao()
        transactionDao = database.transactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertGetDelete_roundTripsTag() = runTest {
        tagDao.insert(testTag())

        assertEquals("Weekend", tagDao.getById("tag-weekend")?.name)

        tagDao.deleteById("tag-weekend")

        assertNull(tagDao.getById("tag-weekend"))
    }

    @Test
    fun observeTags_ordersByNameCaseInsensitively() = runTest {
        tagDao.insert(testTag(id = "tag-weekend", name = "weekend"))
        tagDao.insert(testTag(id = "tag-essential", name = "Essential"))

        val observedIds = tagDao.observeTags().first().map { it.id }

        assertEquals(listOf("tag-essential", "tag-weekend"), observedIds)
    }

    @Test
    fun observeTagsForTransaction_emitsRelationshipChanges() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction())
        tagDao.insert(testTag(id = "tag-essential", name = "Essential"))

        val emissions = mutableListOf<List<com.mojtaba.folentra.core.database.model.TagEntity>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            tagDao.observeTagsForTransaction("transaction-1")
                .take(3)
                .toList(emissions)
        }
        advanceUntilIdle()

        tagDao.addTagToTransaction(testTransactionTagCrossRef(tagId = "tag-essential"))
        advanceUntilIdle()
        tagDao.removeTagFromTransaction("transaction-1", "tag-essential")
        advanceUntilIdle()

        assertEquals(
            listOf(emptyList(), listOf("tag-essential"), emptyList()),
            emissions.map { tags -> tags.map { it.id } },
        )
    }

    @Test
    fun deletingTransaction_cascadesTransactionTagLinks() = runTest {
        categoryDao.insert(testCategory())
        transactionDao.insert(testTransaction())
        tagDao.insert(testTag(id = "tag-essential", name = "Essential"))
        tagDao.addTagToTransaction(testTransactionTagCrossRef(tagId = "tag-essential"))

        transactionDao.deleteById("transaction-1")

        assertEquals(emptyList<String>(), tagDao.observeTagsForTransaction("transaction-1").first().map { it.id })
    }
}
