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
package com.pexip.sdk.sample.conference

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pexip.sdk.conference.Message
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.WorkflowRendering
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    rendering: ChatRendering,
    environment: ViewEnvironment,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = rendering.onBackClick)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Chat")
                },
                navigationIcon = {
                    IconButton(onClick = rendering.onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
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
            LaunchedEffect(state, rendering.messages.size) {
                state.animateScrollToItem(0)
            }
            val context = LocalContext.current
            val format = remember(context) { DateFormat.getTimeFormat(context) }
            val reversedMessages = remember(rendering.messages) { rendering.messages.asReversed() }
            LazyColumn(
                state = state,
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(reversedMessages, Message::at) {
                    Message(it, format::format)
                }
            }
            Divider()
            WorkflowRendering(
                rendering = rendering.composerRendering,
                viewEnvironment = environment,
            )
        }
    }
}

@Composable
private fun Message(
    message: Message,
    format: (Date) -> String,
    modifier: Modifier = Modifier,
) {
    val date = remember(message.at) { Date(message.at) }
    ListItem(
        overlineContent = {
            Text(text = message.participantName)
        },
        headlineContent = {
            Text(text = message.payload)
        },
        trailingContent = {
            Text(text = format(date))
        },
        modifier = modifier,
    )
}
