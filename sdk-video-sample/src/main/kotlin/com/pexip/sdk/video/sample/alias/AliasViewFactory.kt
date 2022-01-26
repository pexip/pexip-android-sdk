package com.pexip.sdk.video.sample.alias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.compose.composeViewFactory

val AliasViewFactory = composeViewFactory<AliasRendering> { rendering, _ ->
    AliasScreen(
        alias = rendering.alias,
        onAliasChange = rendering.onAliasChange,
        resolveEnabled = rendering.resolveEnabled,
        onResolveClick = rendering.onResolveClick,
        onBackClick = rendering.onBackClick
    )
}

@Composable
private fun AliasScreen(
    alias: String,
    onAliasChange: (String) -> Unit,
    resolveEnabled: Boolean,
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
            val keyboardOptions = remember {
                KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Email
                )
            }
            OutlinedTextField(
                value = alias,
                onValueChange = onAliasChange,
                label = {
                    Text(text = "URI")
                },
                placeholder = {
                    Text(text = "conference@example.com")
                },
                maxLines = 1,
                keyboardOptions = keyboardOptions,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
            Button(
                onClick = onResolveClick,
                enabled = resolveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = "Join")
            }
        }
    }
}
