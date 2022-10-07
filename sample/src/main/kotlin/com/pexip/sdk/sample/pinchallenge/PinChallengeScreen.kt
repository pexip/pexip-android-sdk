package com.pexip.sdk.sample.pinchallenge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinChallengeScreen(rendering: PinChallengeRendering, modifier: Modifier = Modifier) {
    Dialog(onDismissRequest = rendering.onBackClick) {
        Surface(shape = MaterialTheme.shapes.large, modifier = modifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Pexip Video SDK",
                    style = MaterialTheme.typography.titleLarge
                )
                val visualTransformation = remember { PasswordVisualTransformation() }
                val keyboardOptions = remember {
                    KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    )
                }
                val focusRequester = remember { FocusRequester() }
                DisposableEffect(focusRequester) {
                    focusRequester.requestFocus()
                    onDispose { focusRequester.freeFocus() }
                }
                TextField(
                    value = rendering.pin,
                    onValueChange = rendering.onPinChange,
                    label = {
                        Text(text = "PIN")
                    },
                    maxLines = 1,
                    isError = rendering.error,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                Button(
                    onClick = rendering.onSubmitClick,
                    enabled = rendering.submitEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Join")
                }
            }
        }
    }
}
