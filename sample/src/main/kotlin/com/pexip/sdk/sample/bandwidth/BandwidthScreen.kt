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
package com.pexip.sdk.sample.bandwidth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.squareup.workflow1.ui.Screen

data class BandwidthScreen(
    val visible: Boolean,
    val bandwidth: Bandwidth,
    val onBandwidthClick: (Bandwidth) -> Unit,
    val onBackClick: () -> Unit,
) : Screen

@Composable
internal fun BandwidthDialog(screen: BandwidthScreen) {
    if (screen.visible) {
        Dialog(
            onDismissRequest = screen.onBackClick,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(shape = Shape, modifier = Modifier.fillMaxWidth(0.8f)) {
                BandwidthList(
                    bandwidth = screen.bandwidth,
                    onBandwidthClick = screen.onBandwidthClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BandwidthList(
    bandwidth: Bandwidth,
    onBandwidthClick: (Bandwidth) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(text = "Select bandwidth")
            },
        )
        LazyColumn(contentPadding = ContentPadding, modifier = Modifier.selectableGroup()) {
            items(Bandwidth.entries) {
                Bandwidth(
                    bandwidth = it,
                    selected = it == bandwidth,
                    onBandwidthClick = onBandwidthClick,
                )
            }
        }
    }
}

@Composable
private fun Bandwidth(
    bandwidth: Bandwidth,
    selected: Boolean,
    onBandwidthClick: (Bandwidth) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            val text = remember(bandwidth) {
                when (bandwidth) {
                    Bandwidth.AUTO -> "Auto"
                    Bandwidth.LOW -> "Low"
                    Bandwidth.MEDIUM -> "Medium"
                    Bandwidth.HIGH -> "High"
                }
            }
            Text(text = text)
        },
        trailingContent = when (selected) {
            true -> {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                    )
                }
            }
            else -> null
        },
        modifier = modifier.selectable(selected) { onBandwidthClick(bandwidth) },
    )
}

private val Shape = RoundedCornerShape(8.dp)
private val ContentPadding = PaddingValues(vertical = 8.dp)
