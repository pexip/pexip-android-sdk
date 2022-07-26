package com.pexip.sdk.sample

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SampleTheme(colors: Colors = LightColors, content: @Composable () -> Unit) {
    MaterialTheme(colors = colors, content = content)
}

private val LightColors = lightColors(
    primary = Color(0xff0a2136),
    primaryVariant = Color(0xff0a2136),
    secondary = Color(0xff0ebec7),
    secondaryVariant = Color(0xff0ebec7),
    error = Color(0xffc50508)
)
