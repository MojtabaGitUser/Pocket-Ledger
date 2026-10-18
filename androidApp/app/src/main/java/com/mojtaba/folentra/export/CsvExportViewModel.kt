package com.mojtaba.folentra.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mojtaba.folentra.core.data.export.CsvExportRequest
import com.mojtaba.folentra.core.data.export.TransactionCsv
import com.mojtaba.folentra.core.data.model.LedgerTransaction
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import java.io.OutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CsvExportState(
    val busy: Boolean = false,
    val count: Int? = null,
    val message: String? = null,
    val pickingDestination: Boolean = false,
)

class CsvExportViewModel(
    private val repository: TransactionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CsvExportState())
    val state = mutableState.asStateFlow()
    private var snapshot: Pair<CsvExportRequest, List<LedgerTransaction>>? = null

    fun invalidate() {
        if (state.value.busy || state.value.pickingDestination) return
        snapshot = null
        mutableState.value = CsvExportState()
    }

    fun preview(request: CsvExportRequest) {
        if (state.value.busy || state.value.pickingDestination) return
        snapshot = null
        mutableState.value = CsvExportState(busy = true)
        viewModelScope.launch {
            try {
                val rows = withContext(ioDispatcher) {
                    repository.observeTransactionsByDateRange(request.startMillis, request.endMillis).first()
                }
                snapshot = request to rows
                mutableState.value = CsvExportState(count = rows.size,
                    message = if (rows.isEmpty()) "No transactions in this date range." else null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = CsvExportState(message = "Could not load transactions. Try again.")
            }
        }
    }

    fun beginSave(): Boolean {
        if (snapshot == null || state.value.busy || state.value.pickingDestination || state.value.count == 0) return false
        mutableState.value = state.value.copy(pickingDestination = true)
        return true
    }

    fun cancelSave() {
        mutableState.value = state.value.copy(pickingDestination = false, message = "Save cancelled.")
    }

    fun save(openStream: () -> OutputStream?) {
        val prepared = snapshot
        if (prepared == null) {
            mutableState.value = CsvExportState(message = "Preview again before saving. No data was written.")
            return
        }
        mutableState.value = state.value.copy(busy = true, pickingDestination = false, message = null)
        viewModelScope.launch {
            try {
                withContext(ioDispatcher) {
                    checkNotNull(openStream()).bufferedWriter(Charsets.UTF_8).use { writer ->
                        TransactionCsv.write(writer, prepared.second, prepared.first)
                    }
                }
                snapshot = null
                mutableState.value = CsvExportState(message = "CSV saved: ${prepared.second.size} transactions.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = state.value.copy(busy = false,
                    message = "Could not save CSV. The destination may contain an incomplete file. Remove it and try again.")
            }
        }
    }
}
