package com.mojtaba.folentra.export

import com.mojtaba.folentra.core.data.export.CsvExportRequest
import com.mojtaba.folentra.core.data.model.LedgerTransaction
import com.mojtaba.folentra.core.testing.coroutine.MainDispatcherRule
import com.mojtaba.folentra.core.testing.repository.FakeTransactionRepository
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CsvExportViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val request = CsvExportRequest(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 17), ZoneId.of("UTC"))
    private val row = LedgerTransaction("one", 123, "CAD", "expense", request.startMillis, createdAt = 0, updatedAt = 0)

    @Test fun savesPreviewedSnapshotEvenWhenLedgerChanges() = runTest {
        val repository = FakeTransactionRepository(listOf(row))
        val model = CsvExportViewModel(repository, main.dispatcher)
        model.preview(request)
        advanceUntilIdle()
        assertEquals(1, model.state.value.count)
        assertTrue(model.beginSave())
        assertFalse(model.beginSave())
        repository.deleteById("one")
        val output = ByteArrayOutputStream()
        model.save { output }
        advanceUntilIdle()
        assertTrue(output.toString("UTF-8").contains("1.23"))
        assertEquals("CSV saved: 1 transactions.", model.state.value.message)
        assertFalse(model.beginSave())
    }
    @Test fun emptyRangeCannotBeSaved() = runTest {
        val model = CsvExportViewModel(FakeTransactionRepository(), main.dispatcher)
        model.preview(request)
        advanceUntilIdle()
        assertEquals(0, model.state.value.count)
        assertFalse(model.beginSave())
    }
    @Test fun cancellationAllowsRetryAndEditingInvalidatesSnapshot() = runTest {
        val model = CsvExportViewModel(FakeTransactionRepository(listOf(row)), main.dispatcher)
        model.preview(request)
        advanceUntilIdle()
        assertTrue(model.beginSave())
        model.cancelSave()
        assertFalse(model.state.value.pickingDestination)
        assertTrue(model.beginSave())
        model.cancelSave()
        model.invalidate()
        assertNull(model.state.value.count)
        assertFalse(model.beginSave())
    }
    @Test fun failedDestinationIsReportedWithoutLeakingException() = runTest {
        val model = CsvExportViewModel(FakeTransactionRepository(listOf(row)), main.dispatcher)
        model.preview(request)
        advanceUntilIdle()
        model.beginSave()
        model.save { throw SecurityException("private destination") }
        advanceUntilIdle()
        assertFalse(model.state.value.busy)
        assertTrue(model.state.value.message!!.contains("incomplete"))
        assertFalse(model.state.value.message!!.contains("private destination"))
    }
    @Test fun lostSnapshotNeverWritesDestination() {
        val model = CsvExportViewModel(FakeTransactionRepository(), main.dispatcher)
        model.save { error("Must not open destination after process recreation") }
        assertTrue(model.state.value.message!!.contains("Preview again"))
    }
}
