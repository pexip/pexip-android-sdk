package com.pexip.sdk.sample

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SampleTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = remember(context, darkTheme) {
        when {
            Build.VERSION.SDK_INT >= 31 -> when (darkTheme) {
                true -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }
            else -> if (darkTheme) darkColorScheme() else lightColorScheme()
        }
    }
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController, darkTheme) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )
        onDispose { }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}