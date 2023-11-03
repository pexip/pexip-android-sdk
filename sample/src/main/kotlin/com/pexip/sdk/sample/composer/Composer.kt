/*
 * Copyright 2022-2023 Pexip AS
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
package com.pexip.sdk.sample.composer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.compose.asMutableState

@Composable
fun Composer(rendering: ComposerRendering, modifier: Modifier = Modifier) {
    val paddingValues = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues()
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.padding(paddingValues),
    ) {
        var value by rendering.message.asMutableState()
        val enabled by remember { derivedStateOf { value.isNotBlank() } }
        ComposerTextField(
            value = value,
            onValueChange = { value = it },
            modifier = Modifier.weight(1f),
        )
        ComposerButton(
            enabled = enabled,
            onClick = rendering.onSubmitClick,
        )
    }
}

@Composable
private fun ComposerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val textColor by animateColorAsState(targetValue = colors.onSurface, label = "textColor")
    val mergedTextStyle = LocalTextStyle.current.merge(TextStyle(color = textColor))
    val cursorColor by animateColorAsState(
        targetValue = colors.onSurface.copy(alpha = 0.5f),
        label = "cursorColor",
    )
    val cursorBrush = remember(cursorColor) { SolidColor(cursorColor) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = mergedTextStyle,
        cursorBrush = cursorBrush,
        decorationBox = {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.padding(16.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Message",
                        color = colors.onSurface.copy(alpha = 0.5f),
                    )
                }
                it()
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ComposerButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button,
            ),
    ) {
        Text(
            text = "Send",
            color = when (enabled) {
                true -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            },
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}
