package com.pexip.sdk.video.sample.conference

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
internal fun Composer(rendering: ConferenceEventsRendering, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        ComposerTextField(rendering = rendering, modifier = Modifier.weight(1f))
        ComposerButton(rendering = rendering)
    }
}

@Composable
private fun ComposerTextField(rendering: ConferenceEventsRendering, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colors
    val textColor by animateColorAsState(colors.onSurface)
    val mergedTextStyle = LocalTextStyle.current.merge(TextStyle(color = textColor))
    val cursorColor by animateColorAsState(colors.onSurface.copy(alpha = 0.5f))
    val cursorBrush = remember(cursorColor) { SolidColor(cursorColor) }
    BasicTextField(
        value = rendering.message,
        onValueChange = rendering.onMessageChange,
        textStyle = mergedTextStyle,
        cursorBrush = cursorBrush,
        decorationBox = {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.padding(16.dp)
            ) {
                if (rendering.message.isEmpty()) {
                    Text(
                        text = "Message",
                        color = colors.onSurface.copy(alpha = 0.5f),
                    )
                }
                it()
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ComposerButton(rendering: ConferenceEventsRendering, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(
                enabled = rendering.submitEnabled,
                onClick = rendering.onSubmitClick,
                role = Role.Button
            )
    ) {
        Text(
            text = "Send",
            color = when (rendering.submitEnabled) {
                true -> MaterialTheme.colors.primary
                else -> MaterialTheme.colors.primary.copy(alpha = 0.5f)
            },
            style = MaterialTheme.typography.button,
            modifier = Modifier.padding(16.dp)
        )
    }
}
