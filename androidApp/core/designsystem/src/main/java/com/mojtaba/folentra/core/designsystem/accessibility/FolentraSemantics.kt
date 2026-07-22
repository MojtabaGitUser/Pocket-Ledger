package com.mojtaba.folentra.core.designsystem.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

fun Modifier.folentraHeading(): Modifier =
    semantics { heading() }

fun Modifier.folentraSelectedState(
    selected: Boolean,
    selectedDescription: String = "Selected",
    unselectedDescription: String = "Not selected",
): Modifier =
    semantics {
        stateDescription = if (selected) selectedDescription else unselectedDescription
    }

fun Modifier.folentraCheckedState(
    checked: Boolean,
    checkedDescription: String = "On",
    uncheckedDescription: String = "Off",
): Modifier =
    semantics {
        stateDescription = if (checked) checkedDescription else uncheckedDescription
    }

fun Modifier.folentraProgressState(description: String): Modifier =
    semantics {
        stateDescription = description
    }
