@file:OptIn(ExperimentalMaterial3Api::class)

package com.pexip.sdk.sample.conference

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.WorkflowRendering
import java.util.Date

@Composable
fun ConferenceEventsScreen(
    rendering: ConferenceEventsRendering,
    environment: ViewEnvironment,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = rendering.onBackClick)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Events")
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
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(it)) {
            val state = rememberLazyListState()
            LaunchedEffect(state, rendering.conferenceEvents.size) {
                state.animateScrollToItem(0)
            }
            LazyColumn(
                state = state,
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(rendering.conferenceEvents.asReversed()) {
                    ConferenceEvent(conferenceEvent = it)
                }
            }
            Divider()
            WorkflowRendering(
                rendering = rendering.composerRendering,
                viewEnvironment = environment
            )
        }
    }
}

@Composable
private fun ConferenceEvent(conferenceEvent: ConferenceEvent, modifier: Modifier = Modifier) {
    when (conferenceEvent) {
        is MessageReceivedConferenceEvent -> MessageReceivedConferenceEvent(
            conferenceEvent = conferenceEvent,
            modifier = modifier
        )
        is PresentationStartConferenceEvent -> PresentationStartConferenceEvent(
            conferenceEvent = conferenceEvent,
            modifier = modifier
        )
        is PresentationStopConferenceEvent -> PresentationStopConferenceEvent(
            conferenceEvent = conferenceEvent,
            modifier = modifier
        )
        else -> {}
    }
}

@Composable
private fun MessageReceivedConferenceEvent(
    conferenceEvent: MessageReceivedConferenceEvent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val format = remember(context) { DateFormat.getTimeFormat(context) }
    val date = remember(conferenceEvent.at) { Date(conferenceEvent.at) }
    ListItem(
        overlineText = {
            Text(text = conferenceEvent.participantName)
        },
        headlineText = {
            Text(text = conferenceEvent.payload)
        },
        trailingContent = {
            Text(text = format.format(date))
        },
        modifier = modifier
    )
}

@Composable
private fun PresentationStartConferenceEvent(
    conferenceEvent: PresentationStartConferenceEvent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val format = remember(context) { DateFormat.getTimeFormat(context) }
    val date = remember(conferenceEvent.at) { Date(conferenceEvent.at) }
    ListItem(
        overlineText = {
            Text(text = conferenceEvent.presenterName)
        },
        headlineText = {
            Text(text = "Presentation started")
        },
        trailingContent = {
            Text(text = format.format(date))
        },
        modifier = modifier
    )
}

@Composable
private fun PresentationStopConferenceEvent(
    conferenceEvent: PresentationStopConferenceEvent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val format = remember(context) { DateFormat.getTimeFormat(context) }
    val date = remember(conferenceEvent.at) { Date(conferenceEvent.at) }
    ListItem(
        headlineText = {
            Text(text = "Presentation stopped")
        },
        trailingContent = {
            Text(text = format.format(date))
        },
        modifier = modifier
    )
}
