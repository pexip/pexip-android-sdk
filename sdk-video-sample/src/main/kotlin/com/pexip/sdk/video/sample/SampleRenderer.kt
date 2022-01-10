package com.pexip.sdk.video.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pexip.sdk.workflow.ui.renderer

val SampleRenderer = renderer<SampleRendering> {
    SampleScreen(
        value = value,
        onValueChange = onValueChange,
        resolveEnabled = resolveEnabled,
        onResolveClick = onResolveClick,
        modifier = it
    )
}

@Composable
private fun SampleScreen(
    value: String,
    onValueChange: (String) -> Unit,
    resolveEnabled: Boolean,
    onResolveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(text = "URI")
                },
                placeholder = {
                    Text(text = "conference@example.com")
                },
                maxLines = 1,
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
