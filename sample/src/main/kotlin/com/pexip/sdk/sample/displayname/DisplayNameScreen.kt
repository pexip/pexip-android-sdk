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
package com.pexip.sdk.sample.displayname

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayNameScreen(rendering: DisplayNameRendering, modifier: Modifier = Modifier) {
    Dialog(onDismissRequest = rendering.onBackClick) {
        Surface(shape = MaterialTheme.shapes.large, modifier = modifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Introduce yourself",
                    style = MaterialTheme.typography.titleLarge,
                )
                val keyboardOptions = remember {
                    KeyboardOptions(
                        autoCorrect = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Go,
                    )
                }
                val currentOnNextClick by rememberUpdatedState(rendering.onNextClick)
                val keyboardActions = remember {
                    KeyboardActions(onGo = { currentOnNextClick() })
                }
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(focusRequester) {
                    focusRequester.requestFocus()
                }
                TextField(
                    value = rendering.displayName,
                    onValueChange = rendering.onDisplayNameChange,
                    label = {
                        Text(text = "Display name")
                    },
                    placeholder = {
                        Text(text = "Joe")
                    },
                    maxLines = 1,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
                Button(
                    onClick = rendering.onNextClick,
                    enabled = rendering.displayName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Next")
                }
            }
        }
    }
}
