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
package com.pexip.sdk.sample.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.pexip.sdk.conference.Message
import com.pexip.sdk.sample.asMutableState
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.TextController
import kotlinx.datetime.toJavaInstant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class ChatScreen(
    val payload: TextController,
    val messages: List<Message>,
    val submitEnabled: Boolean,
    val onSubmitClick: () -> Unit,
    val onBackClick: () -> Unit,
) : Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(screen: ChatScreen, modifier: Modifier = Modifier) {
    BackHandler(onBack = screen.onBackClick)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Chat")
                },
                navigationIcon = {
                    IconButton(onClick = screen.onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier,
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val state = rememberLazyListState()
            LaunchedEffect(state, screen.messages.size) {
                state.animateScrollToItem(0)
            }
            val reversedMessages = remember(screen.messages) { screen.messages.asReversed() }
            LazyColumn(
                state = state,
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(reversedMessages, { it.at.toEpochMilliseconds() }) { message ->
                    Message(message = message)
                }
            }
            HorizontalDivider()
            val (value, onValueChange) = screen.payload.asMutableState()
            Composer(
                value = value,
                onValueChange = onValueChange,
                submitEnabled = screen.submitEnabled,
                onSubmitClick = screen.onSubmitClick,
            )
        }
    }
}

@Composable
private fun Message(message: Message, modifier: Modifier = Modifier) {
    ListItem(
        overlineContent = {
            Text(text = message.participantName)
        },
        headlineContent = {
            Text(text = message.payload)
        },
        trailingContent = {
            val at = remember(message.at) {
                LocalDateTime.ofInstant(message.at.toJavaInstant(), ZoneId.systemDefault())
            }
            Text(text = TimeFormatter.format(at))
        },
        modifier = modifier,
    )
}

@Composable
fun Composer(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    submitEnabled: Boolean,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.Bottom, modifier = modifier) {
        ComposerTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
        )
        ComposerButton(
            enabled = submitEnabled,
            onClick = onSubmitClick,
        )
    }
}

@Composable
private fun ComposerTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val textColor by animateColorAsState(targetValue = colors.onSurface, label = "textColor")
    val mergedTextStyle = LocalTextStyle.current.merge(TextStyle(color = textColor))
    val cursorColor by animateColorAsState(
        targetValue = colors.onSurface.copy(alpha = 0.5f),
        label = "cursorColor",
    )
    val cursorBrush = remember(cursorColor) { SolidColor(cursorColor) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = mergedTextStyle,
        cursorBrush = cursorBrush,
        decorationBox = {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.padding(16.dp),
            ) {
                if (value.text.isEmpty()) {
                    Text(
                        text = "Message",
                        color = colors.onSurface.copy(alpha = 0.5f),
                    )
                }
                it()
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ComposerButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button,
            ),
    ) {
        Text(
            text = "Send",
            color = when (enabled) {
                true -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            },
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}

private val TimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
