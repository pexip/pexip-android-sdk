/*
 * Copyright 2022-2025 Pexip AS
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
package com.pexip.sdk.sample.dtmf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.squareup.workflow1.ui.Screen

data class DtmfScreen(
    val visible: Boolean,
    val onToneClick: (String) -> Unit,
    val onBackClick: () -> Unit,
) : Screen

@Composable
fun DtmfDialog(screen: DtmfScreen) {
    if (screen.visible) {
        Dialog(onDismissRequest = screen.onBackClick) {
            Surface(shape = TonePadShape) {
                TonePad(rendering = screen)
            }
        }
    }
}

@Composable
private fun TonePad(rendering: DtmfScreen, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier,
    ) {
        items(Tones) {
            ToneButton(
                tone = it,
                onClick = { rendering.onToneClick(it) },
            )
        }
    }
}

@Composable
private fun ToneButton(tone: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = ToneButtonShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = tone)
        }
    }
}

private val Tones = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
private val TonePadShape = RoundedCornerShape(8.dp)
private val ToneButtonShape = RoundedCornerShape(4.dp)
