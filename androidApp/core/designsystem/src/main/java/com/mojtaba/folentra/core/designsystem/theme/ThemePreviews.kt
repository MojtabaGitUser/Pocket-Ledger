package com.mojtaba.folentra.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light",
    group = "Theme",
    showBackground = true,
)
@Preview(
    name = "Dark",
    group = "Theme",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
annotation class FolentraThemePreview

@Composable
fun FolentraPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    FolentraTheme(
        darkTheme = darkTheme,
        dynamicColor = false,
        content = content,
    )
}
