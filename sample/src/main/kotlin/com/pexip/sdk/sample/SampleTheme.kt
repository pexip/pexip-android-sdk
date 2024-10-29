/*
 * Copyright 2022-2024 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.sample

// import androidx.compose.ui.graphics.Color
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun SampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    systemBars: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    if (systemBars) {
        val activity = remember(context, context::findComponentActivity)
        val parentDarkTheme = LocalDarkTheme.current
        DisposableEffect(activity, darkTheme, parentDarkTheme) {
            activity.enableEdgeToEdge(darkTheme)
            onDispose {
                if (parentDarkTheme != null) {
                    activity.enableEdgeToEdge(parentDarkTheme)
                }
            }
        }
    }
    val colorScheme = remember(context, darkTheme) {
        when {
            Build.VERSION.SDK_INT >= 31 -> when (darkTheme) {
                true -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }
            else -> if (darkTheme) darkColorScheme() else lightColorScheme()
        }
    }
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

private val LocalDarkTheme = compositionLocalOf<Boolean?> { null }

private fun ComponentActivity.enableEdgeToEdge(
    darkTheme: Boolean,
    color: Int = Color.TRANSPARENT,
) = enableEdgeToEdge(
    statusBarStyle = when (darkTheme) {
        true -> SystemBarStyle.dark(color)
        else -> SystemBarStyle.light(color, color)
    },
    navigationBarStyle = when (darkTheme) {
        true -> SystemBarStyle.dark(color)
        else -> SystemBarStyle.light(color, color)
    },
)

private tailrec fun Context.findComponentActivity(): ComponentActivity = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> error("Unable to find Activity.")
}
