package com.mojtaba.folentra.draft

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.data.model.DraftTransaction
import com.mojtaba.folentra.core.data.repository.local.LocalCategoryRepository
import com.mojtaba.folentra.core.data.repository.local.LocalDraftTransactionRepository
import com.mojtaba.folentra.core.database.FolentraDatabase
import com.mojtaba.folentra.core.database.buildFolentraDatabase
import com.mojtaba.folentra.core.designsystem.theme.FolentraTheme
import com.mojtaba.folentra.feature.transaction.draft.DraftTransactionsRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.mojtaba.folentra.core.data.receipt.MlKitReceiptTextRecognizer
import com.mojtaba.folentra.core.data.receipt.ReceiptImages
import com.mojtaba.folentra.feature.transaction.draft.DraftTransactionsViewModel
import kotlinx.coroutines.withTimeout

class DraftTransactionDeviceTest {
    @get:Rule val compose = createComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "draft-device-test-${UUID.randomUUID()}.db"
    private lateinit var database: FolentraDatabase
    private lateinit var repository: LocalDraftTransactionRepository
    private val models = ViewModelStore()

    private fun open() {
        database = buildFolentraDatabase(Room.databaseBuilder<FolentraDatabase>(context, name))
        repository = LocalDraftTransactionRepository(database.draftTransactionDao())
    }

    @Before fun setup() = open()
    @After fun cleanup() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { models.clear() }
        database.close()
        context.deleteDatabase(name)
    }

    @Test fun reviewSaveEditConfirmAndDiscard() {
        compose.setContent {
            FolentraTheme {
                DraftTransactionsRoute(repository, LocalCategoryRepository(database.categoryDao()), {})
            }
        }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("No pending drafts.").fetchSemanticsNodes().isNotEmpty()
        }
        click("New receipt draft")
        compose.onNodeWithText("Confirm and add transaction").performScrollTo().assertIsNotEnabled()
        click("Save draft")
        compose.waitUntil(10_000) { pending().size == 1 }
        assertEquals(0, ledgerCount())
        click("Receipt draft · No amount USD")
        compose.onNodeWithText("Amount").performScrollTo().performTextReplacement("12.34")
        compose.onNodeWithText("Merchant").performScrollTo().performTextReplacement("DEVICE TEST")
        click("Income")
        click("Confirm and add transaction")
        assertEquals(0, ledgerCount())
        compose.onNodeWithText("Keep reviewing").performClick()
        assertEquals(0, ledgerCount())
        click("Confirm and add transaction")
        compose.onNodeWithText("Confirm transaction").performClick()
        compose.waitUntil(10_000) { ledgerCount() == 1 && pending().isEmpty() }
        click("New receipt draft")
        click("Discard draft")
        compose.onNodeWithText("Discard", substring = false).performClick()
        compose.waitUntil(10_000) { pending().isEmpty() }
        assertEquals(1, ledgerCount())
    }

    @Test fun incompleteDraftSurvivesDatabaseReopenAndConfirmationRetry() = runBlocking {
        val now = System.currentTimeMillis()
        val draft = DraftTransaction(id = "device-reopen", createdAt = now, updatedAt = now)
        repository.save(draft)
        database.close()
        open()
        assertEquals("", pending().single().amountInput)
        assertEquals(0, ledgerCount())
        val valid = draft.copy(amountInput = "8.50", type = "income", occurredAt = now)
        val id = repository.confirm(valid)
        assertEquals(id, repository.confirm(valid))
        assertEquals(1, ledgerCount())
        assertTrue(pending().isEmpty())
    }

    @Test fun bundledOcrToEditableDraftThenSingleConfirmedTransaction() {
        val images = ReceiptImages(context)
        val uri = runBlocking { images.createCameraUri() }
        val bitmap = Bitmap.createBitmap(1000, 900, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 48f; typeface = Typeface.MONOSPACE }
            listOf("MAPLE MARKET", "2026-10-26", "SUBTOTAL 10.00", "GST 0.50", "PST 0.70", "TOTAL CAD 11.20").forEachIndexed { index, text ->
                canvas.drawText(text, 60f, 100f + index * 100, paint)
            }
            context.contentResolver.openOutputStream(Uri.parse(uri))!!.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            lateinit var model: DraftTransactionsViewModel
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                model = DraftTransactionsViewModel(repository, LocalCategoryRepository(database.categoryDao()),
                    recognizer = MlKitReceiptTextRecognizer(context))
                models.put("scanner", model)
                model.prepareImage {}
            }
            runBlocking { withTimeout(15_000) { model.state.first { !it.busy && it.selected != null } } }
            InstrumentationRegistry.getInstrumentation().runOnMainSync { model.scan(uri) }
            runBlocking { withTimeout(30_000) { model.state.first { !it.busy } } }
            val draft = pending().single()
            assertEquals("11.20", draft.amountInput)
            assertEquals("MAPLE MARKET", draft.merchant)
            assertEquals("CAD", draft.currencyCode)
            assertNotNull(draft.occurredAt)
            assertEquals(0, ledgerCount())
            compose.setContent { FolentraTheme { DraftTransactionsRoute(repository, LocalCategoryRepository(database.categoryDao()), {}) } }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("MAPLE MARKET · 11.20 CAD").fetchSemanticsNodes().isNotEmpty() }
            click("MAPLE MARKET · 11.20 CAD")
            compose.onNodeWithText("Amount").performScrollTo().performTextReplacement("11.25")
            click("Income")
            click("Confirm and add transaction")
            assertEquals(0, ledgerCount())
            compose.onNodeWithText("Confirm transaction").performClick()
            compose.waitUntil(10_000) { ledgerCount() == 1 }
            runBlocking {
                assertEquals(1125L, database.transactionDao().observeRecentTransactions(1).first().single().amountMinor)
                repository.confirm(draft) // Replay old pre-edit input: idempotency must still win.
            }
            assertEquals(1, ledgerCount())
        } finally {
            bitmap.recycle()
            runBlocking { images.deleteCameraUri(uri) }
        }
    }

    private fun click(text: String) {
        compose.onNodeWithText(text).performScrollTo().performClick()
        compose.waitForIdle()
    }
    private fun pending() = runBlocking { repository.observePending().first() }
    private fun ledgerCount() = runBlocking { database.transactionDao().observeRecentTransactions(100).first().size }
}
