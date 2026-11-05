package com.mojtaba.folentra.feature.transaction.draft

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.lifecycle.createSavedStateHandle
import android.net.Uri
import com.mojtaba.folentra.core.data.receipt.ReceiptTextRecognizer
import com.mojtaba.folentra.core.data.receipt.ReceiptImages
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
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
fun DraftTransactionsRoute(repository: DraftTransactionRepository, categories: CategoryRepository, onBack: () -> Unit,
    recognizer: ReceiptTextRecognizer? = null) {
    val model: DraftTransactionsViewModel = viewModel(factory = viewModelFactory {
        initializer { DraftTransactionsViewModel(repository, categories, recognizer = recognizer, saved = createSavedStateHandle()) }
    })
    val state by model.state.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    val context = LocalContext.current.applicationContext
    val images = remember(context) { ReceiptImages(context) }
    val scope = rememberCoroutineScope()
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var choosing by rememberSaveable { mutableStateOf(false) }
    var imageDraftId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(images) { images.cleanup() }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        choosing = false
        val uri = cameraUri
        cameraUri = null
        if (success && uri != null) model.scan(uri, imageDraftId) { images.deleteCameraUri(uri) }
        else {
            scope.launch { images.deleteCameraUri(uri) }
            model.report("Photo cancelled. Your draft is kept; choose another image or continue manually.")
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        choosing = false
        if (uri != null) model.scan(uri.toString(), imageDraftId)
        else model.report("No image selected. Continue manually or choose another image.")
    }
    val takePhoto: () -> Unit = {
        model.prepareImage {
            imageDraftId = model.state.value.selected?.id
            choosing = true
            scope.launch {
                try {
                    cameraUri = images.createCameraUri()
                    camera.launch(Uri.parse(cameraUri))
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) {
                    images.deleteCameraUri(cameraUri); cameraUri = null; choosing = false
                    model.report("Camera unavailable or access denied. Choose a photo or continue manually.")
                }
            }
        }
    }
    val choosePhoto: () -> Unit = {
        model.prepareImage {
            imageDraftId = model.state.value.selected?.id
            try {
                choosing = true
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (_: Exception) {
                choosing = false
                model.report("Photo picker unavailable. Try again or continue manually.")
            }
        }
    }
    BackHandler(state.selected != null) { if (!state.busy) model.saveAndClose() }
    AdaptiveContainer(modifier = Modifier.fillMaxSize()) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
            item {
                TextButton(onClick = { if (state.selected == null) onBack() else model.saveAndClose() }, enabled = !state.busy) {
                    Text(if (state.selected == null) "Back" else "Save draft and close")
                }
                Text(if (state.selected == null) "Transaction drafts" else "Review transaction",
                    style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Text("Read a receipt on your device, then check and correct every field. Nothing is added to your balance or exports until you confirm.")
            }
            state.message?.let { message -> item { Text(message, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) } }
            if (recognizer != null) item {
                Text("Scan a receipt", style = MaterialTheme.typography.titleMedium)
                Text("Rescanning fills blank fields only. Clear a field first if you want a new suggestion. A dollar sign alone does not identify the currency.")
                OutlinedButton(onClick = takePhoto, enabled = !state.busy && !state.loading && !choosing) { Text("Take photo") }
                OutlinedButton(onClick = choosePhoto, enabled = !state.busy && !state.loading && !choosing) { Text("Choose from gallery") }
                if (choosing) TextButton(onClick = { choosing = false; model.report("Continue manually, or choose another photo.") }) { Text("Continue manually") }
                if (state.scanning) Text("Reading receipt on this device…", modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            if (state.scanSummary.isNotEmpty()) item {
                Text("OCR suggestions — verify against your receipt", style = MaterialTheme.typography.titleMedium)
                state.scanSummary.forEach { Text(it) }
                Text("Missing or uncertain fields need your review. Retained values may differ from OCR suggestions.")
            }
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
                item { DraftField("Amount", draft.amountInput, !state.busy, KeyboardType.Decimal,
                    if (state.validation?.errors?.amount != null) "Enter a valid positive amount." else null) { value -> model.edit { copy(amountInput = value.take(100)) } } }
                item { DraftField("Currency (e.g. CAD or USD)", draft.currencyCode, !state.busy, KeyboardType.Ascii,
                    if (state.validation?.errors?.currencyCode != null) "Enter a three-letter currency code." else null) { value -> model.edit { copy(currencyCode = value.take(20)) } } }
                item { DraftField("Merchant", draft.merchant, !state.busy) { value -> model.edit { copy(merchant = value.take(120)) } } }
                item { DraftField("Note", draft.note, !state.busy) { value -> model.edit { copy(note = value.take(500)) } } }
                if (!state.busy) {
                    item { DateSelector(draft.occurredAt, { value -> model.edit { copy(occurredAt = value) } },
                        if (state.validation?.errors?.occurredAt != null) "Select the receipt date; future dates are not allowed." else null)
                        if (draft.occurredAt != null) TextButton(onClick = { model.edit { copy(occurredAt = null) } }) { Text("Clear date") }
                    }
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
private fun DraftField(label: String, value: String, enabled: Boolean, keyboard: KeyboardType = KeyboardType.Text,
    errorText: String? = null, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard), isError = errorText != null,
        supportingText = { errorText?.let { Text(it) } },
        label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}
