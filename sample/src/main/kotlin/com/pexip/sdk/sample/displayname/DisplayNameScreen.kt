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
import androidx.compose.runtime.DisposableEffect
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Introduce yourself",
                    style = MaterialTheme.typography.titleLarge
                )
                val keyboardOptions = remember {
                    KeyboardOptions(
                        autoCorrect = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Go
                    )
                }
                val currentOnNextClick by rememberUpdatedState(rendering.onNextClick)
                val keyboardActions = remember {
                    KeyboardActions(onGo = { currentOnNextClick() })
                }
                val focusRequester = remember { FocusRequester() }
                DisposableEffect(focusRequester) {
                    focusRequester.requestFocus()
                    onDispose { focusRequester.freeFocus() }
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
                        .focusRequester(focusRequester)
                )
                Button(
                    onClick = rendering.onNextClick,
                    enabled = rendering.displayName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Next")
                }
            }
        }
    }
}
