package com.pexip.sdk.video.sample.alias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AliasScreen(
    alias: String,
    host: String,
    onAliasChange: (String) -> Unit,
    onHostChange: (String) -> Unit,
    onResolveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackClick)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Pexip Video SDK")
                }
            )
        },
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
                OutlinedTextField(
                    value = alias,
                    onValueChange = onAliasChange,
                    label = {
                        Text(text = "Alias")
                    },
                    placeholder = {
                        Text(text = "alias")
                    },
                    maxLines = 1,
                    keyboardOptions = aliasKeyboardOptions,
                    modifier = Modifier.fillMaxWidth()
                )
                val hostKeyboardOptions = remember {
                    KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    )
                }
                val currentOnResolveClick by rememberUpdatedState(onResolveClick)
                val hostKeyboardActions = remember {
                    KeyboardActions(onGo = { currentOnResolveClick() })
                }
                OutlinedTextField(
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = onResolveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = "Join")
            }
        }
    }
}
