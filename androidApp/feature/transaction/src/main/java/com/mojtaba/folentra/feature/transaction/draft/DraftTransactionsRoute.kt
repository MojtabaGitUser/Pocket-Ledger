package com.mojtaba.folentra.feature.transaction.draft

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.DraftTransactionRepository
import com.mojtaba.folentra.core.designsystem.component.AdaptiveContainer
import com.mojtaba.folentra.feature.transaction.presentation.components.CategorySelector
import com.mojtaba.folentra.feature.transaction.presentation.components.DateSelector
import com.mojtaba.folentra.feature.transaction.presentation.editor.TransactionCategoryOption

@Composable
fun DraftTransactionsRoute(repository: DraftTransactionRepository, categories: CategoryRepository, onBack: () -> Unit) {
    val model: DraftTransactionsViewModel = viewModel(factory = viewModelFactory {
        initializer { DraftTransactionsViewModel(repository, categories) }
    })
    val state by model.state.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    BackHandler(state.selected != null) { if (!state.busy) model.saveAndClose() }
    AdaptiveContainer(modifier = Modifier.fillMaxSize()) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
            item {
                TextButton(onClick = { if (state.selected == null) onBack() else model.saveAndClose() }, enabled = !state.busy) {
                    Text(if (state.selected == null) "Back" else "Save draft and close")
                }
                Text(if (state.selected == null) "Transaction drafts" else "Review transaction",
                    style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Text("Drafts do not affect your balance or exports. Enter receipt details manually for now; photo scanning is not available yet.")
            }
            state.message?.let { message -> item { Text(message) } }
            if (state.loading || state.busy) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            val draft = state.selected
            if (draft == null) {
                item { Button(onClick = model::create, enabled = !state.busy && !state.loading) { Text("New receipt draft") } }
                if (!state.loading && state.drafts.isEmpty()) item { Text("No pending drafts.") }
                items(state.drafts, key = { it.id }) { row ->
                    OutlinedButton(onClick = { model.select(row) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        Text("${row.merchant.ifBlank { "Receipt draft" }} · ${row.amountInput.ifBlank { "No amount" }} ${row.currencyCode}")
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("expense", "income").forEach { type ->
                            FilterChip(selected = draft.type == type, onClick = { model.edit { copy(type = type, categoryId = null) } },
                                enabled = !state.busy, label = { Text(type.replaceFirstChar { it.uppercase() }) })
                        }
                    }
                }
                item { DraftField("Amount", draft.amountInput, !state.busy) { value -> model.edit { copy(amountInput = value.take(100)) } } }
                item { DraftField("Currency (e.g. USD)", draft.currencyCode, !state.busy) { value -> model.edit { copy(currencyCode = value.take(20)) } } }
                item { DraftField("Merchant", draft.merchant, !state.busy) { value -> model.edit { copy(merchant = value.take(120)) } } }
                item { DraftField("Note", draft.note, !state.busy) { value -> model.edit { copy(note = value.take(500)) } } }
                if (!state.busy) {
                    item { DateSelector(draft.occurredAt, { value -> model.edit { copy(occurredAt = value) } }, null) }
                    item { CategorySelector(state.categories.filter { it.type == draft.type }.map { TransactionCategoryOption(it.id, it.name, it.type) },
                        draft.categoryId, { value -> model.edit { copy(categoryId = value) } },
                        if (draft.type == "expense" && draft.categoryId == null) "Select an expense category before confirming." else null) }
                }
                item {
                    Text("You can save incomplete details as a draft. Confirmation requires a positive amount, a three-letter currency, a valid date and an expense category.")
                    Button(onClick = { confirm = true }, enabled = !state.busy && state.validation?.isValid == true) {
                        Text("Confirm and add transaction")
                    }
                    OutlinedButton(onClick = model::saveAndClose, enabled = !state.busy) { Text("Save draft") }
                    TextButton(onClick = { discard = true }, enabled = !state.busy) { Text("Discard draft") }
                }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Add to your ledger?") },
        text = { Text("This will add the reviewed transaction to your balance, reports and exports.") },
        confirmButton = { TextButton(onClick = { confirm = false; model.confirm() }) { Text("Confirm transaction") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Keep reviewing") } })
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Discard this draft?") },
        text = { Text("This removes the draft without adding a transaction.") },
        confirmButton = { TextButton(onClick = { discard = false; model.discard() }) { Text("Discard") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Cancel") } })
}

@Composable
private fun DraftField(label: String, value: String, enabled: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, enabled = enabled,
        label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}
