package com.mojtaba.pocketledger.core.designsystem.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

fun Modifier.pocketLedgerHeading(): Modifier =
    semantics { heading() }

fun Modifier.pocketLedgerSelectedState(
    selected: Boolean,
    selectedDescription: String = "Selected",
    unselectedDescription: String = "Not selected",
): Modifier =
    semantics {
        stateDescription = if (selected) selectedDescription else unselectedDescription
    }

fun Modifier.pocketLedgerCheckedState(
    checked: Boolean,
    checkedDescription: String = "On",
    uncheckedDescription: String = "Off",
): Modifier =
    semantics {
        stateDescription = if (checked) checkedDescription else uncheckedDescription
    }

fun Modifier.pocketLedgerProgressState(description: String): Modifier =
    semantics {
        stateDescription = description
    }
