package com.mojtaba.folentra.feature.transaction.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.mojtaba.folentra.core.designsystem.accessibility.folentraSelectedState
import com.mojtaba.folentra.feature.transaction.form.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTypeSegmentedButton(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.semantics { contentDescription = "Transaction type" },
    ) {
        options.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                modifier = Modifier
                    .semantics {
                        contentDescription = "Transaction type ${type.label}"
                        this.selected = selectedType == type
                    }
                    .folentraSelectedState(selectedType == type),
            ) {
                Text(type.label)
            }
        }
    }
}

private val TransactionType.label: String
    get() = when (this) {
        TransactionType.EXPENSE -> "Expense"
        TransactionType.INCOME -> "Income"
    }
