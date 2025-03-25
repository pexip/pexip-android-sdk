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
package com.pexip.sdk.sample.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pexip.sdk.sample.asMutableState
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.TextController

data class AliasScreen(
    val alias: TextController,
    val host: TextController,
    val presentationInMain: Boolean,
    val resolveEnabled: Boolean,
    val onPresentationInMainChange: (Boolean) -> Unit,
    val onResolveClick: () -> Unit,
    val onBackClick: () -> Unit,
) : Screen

@Composable
fun AliasScreen(screen: AliasScreen, modifier: Modifier = Modifier) {
    Dialog(onDismissRequest = screen.onBackClick) {
        Surface(shape = MaterialTheme.shapes.large, modifier = modifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Pexip Video SDK",
                    style = MaterialTheme.typography.titleLarge,
                )
                val aliasKeyboardOptions = remember {
                    KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    )
                }
                val focusRequester = remember(::FocusRequester)
                LaunchedEffect(focusRequester) {
                    focusRequester.requestFocus()
                }
                val (alias, onAliasChange) = screen.alias.asMutableState()
                TextField(
                    value = alias,
                    onValueChange = onAliasChange,
                    label = {
                        Text(text = "Alias")
                    },
                    placeholder = {
                        Text(text = "john.doe.vmr@example.com")
                    },
                    maxLines = 1,
                    keyboardOptions = aliasKeyboardOptions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
                val hostKeyboardOptions = remember {
                    KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go,
                    )
                }
                val currentOnResolveClick by rememberUpdatedState(screen.onResolveClick)
                val hostKeyboardActions = remember {
                    KeyboardActions(onGo = { currentOnResolveClick() })
                }
                val (host, onHostChange) = screen.host.asMutableState()
                TextField(
                    value = host,
                    onValueChange = onHostChange,
                    label = {
                        Text(text = "Host")
                    },
                    placeholder = {
                        Text(text = "example.com")
                    },
                    maxLines = 1,
                    keyboardOptions = hostKeyboardOptions,
                    keyboardActions = hostKeyboardActions,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Presentation in main")
                    Switch(
                        checked = screen.presentationInMain,
                        onCheckedChange = screen.onPresentationInMainChange,
                    )
                }
                Button(
                    onClick = screen.onResolveClick,
                    enabled = screen.resolveEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Join")
                }
            }
        }
    }
}
