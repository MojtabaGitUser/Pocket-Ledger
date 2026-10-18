package com.mojtaba.folentra.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mojtaba.folentra.core.data.export.CsvExportRequest
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun CsvExportSection(repository: TransactionRepository) {
    val model: CsvExportViewModel = viewModel(factory = viewModelFactory {
        initializer { CsvExportViewModel(repository) }
    })
    val state by model.state.collectAsStateWithLifecycle()
    var start by rememberSaveable { mutableStateOf(LocalDate.now().withDayOfMonth(1).toString()) }
    var end by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var merchant by rememberSaveable { mutableStateOf(false) }
    var notes by rememberSaveable { mutableStateOf(false) }
    val request = runCatching {
        CsvExportRequest(LocalDate.parse(start), LocalDate.parse(end), ZoneId.systemDefault(), merchant, notes)
    }.getOrNull()
    val resolver = LocalContext.current.applicationContext.contentResolver
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) model.cancelSave()
        else model.save { resolver.openOutputStream(uri, "wt") }
    }
    val editable = !state.busy && !state.pickingDestination
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Export transactions", style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() })
        Text("Create a CSV with dates, transaction types, amounts and currencies. Dates use your device time zone. Merchant names and notes are optional.")
        OutlinedTextField(value = start, onValueChange = { start = it; model.invalidate() },
            label = { Text("Start date (YYYY-MM-DD)") }, singleLine = true,
            enabled = editable, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = end, onValueChange = { end = it; model.invalidate() },
            label = { Text("End date (YYYY-MM-DD)") }, singleLine = true,
            enabled = editable, modifier = Modifier.fillMaxWidth())
        if (request == null) Text("Enter valid dates between 1900 and 9998. End date must be on or after start date.",
            color = MaterialTheme.colorScheme.error)
        ListItem(headlineContent = { Text("Include merchant names") },
            modifier = Modifier.toggleable(merchant, enabled = editable, role = Role.Checkbox) {
                merchant = it; model.invalidate()
            }, trailingContent = { Checkbox(merchant, onCheckedChange = null, enabled = editable) })
        ListItem(headlineContent = { Text("Include notes") },
            modifier = Modifier.toggleable(notes, enabled = editable, role = Role.Checkbox) {
                notes = it; model.invalidate()
            }, trailingContent = { Checkbox(notes, onCheckedChange = null, enabled = editable) })
        Text("The file contains financial data and is not encrypted. Choose a trusted destination. A cloud storage provider may upload the file you save.")
        Button(onClick = { request?.let(model::preview) }, enabled = editable && request != null) {
            Text(if (state.busy) "Working…" else "Preview export")
        }
        state.count?.let { Text("$it transactions in this snapshot") }
        Button(onClick = {
            if (model.beginSave()) {
                try { launcher.launch("folentra-transactions.csv") }
                catch (_: Exception) { model.cancelSave() }
            }
        }, enabled = editable && (state.count ?: 0) > 0) { Text("Save CSV") }
        state.message?.let { Text(it) }
    }
}
