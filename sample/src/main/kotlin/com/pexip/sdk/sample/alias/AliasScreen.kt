package com.pexip.sdk.sample.alias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasScreen(rendering: AliasRendering, modifier: Modifier = Modifier) {
    BackHandler(onBack = rendering.onBackClick)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Pexip Video SDK")
                },
                navigationIcon = {
                    IconButton(onClick = rendering.onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                val aliasKeyboardOptions = remember {
                    KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                }
                val focusRequester = remember { FocusRequester() }
                DisposableEffect(focusRequester) {
                    focusRequester.requestFocus()
                    onDispose { focusRequester.freeFocus() }
                }
                TextField(
                    value = rendering.alias,
                    onValueChange = rendering.onAliasChange,
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
                        .focusRequester(focusRequester)
                )
                val hostKeyboardOptions = remember {
                    KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    )
                }
                val currentOnResolveClick by rememberUpdatedState(rendering.onResolveClick)
                val hostKeyboardActions = remember {
                    KeyboardActions(onGo = { currentOnResolveClick() })
                }
                TextField(
                    value = rendering.host,
                    onValueChange = rendering.onHostChange,
                    label = {
                        Text(text = "Host")
                    },
                    placeholder = {
                        Text(text = "example.com")
                    },
                    maxLines = 1,
                    keyboardOptions = hostKeyboardOptions,
                    keyboardActions = hostKeyboardActions,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Presentation in main")
                    Switch(
                        checked = rendering.presentationInMain,
                        onCheckedChange = rendering.onPresentationInMainChange
                    )
                }
            }
            Button(
                onClick = rendering.onResolveClick,
                enabled = rendering.resolveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = "Join")
            }
        }
    }
}
