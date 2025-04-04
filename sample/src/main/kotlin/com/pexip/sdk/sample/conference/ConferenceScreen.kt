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
package com.pexip.sdk.sample.conference

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.ScreenShare
import androidx.compose.material.icons.automirrored.rounded.StopScreenShare
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import coil.compose.AsyncImage
import com.pexip.sdk.conference.Element
import com.pexip.sdk.conference.SplashScreen
import com.pexip.sdk.media.AudioDevice
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.media.webrtc.compose.VideoTrackEmbeddedRenderer
import com.pexip.sdk.media.webrtc.compose.VideoTrackRenderer
import com.pexip.sdk.sample.CameraIconButton
import com.pexip.sdk.sample.IconButton
import com.pexip.sdk.sample.IconButtonDefaults
import com.pexip.sdk.sample.IconToggleButton
import com.pexip.sdk.sample.MicrophoneIconButton
import com.pexip.sdk.sample.SampleTheme
import com.pexip.sdk.sample.audio.AudioDeviceIcon
import com.pexip.sdk.sample.audio.AudioDeviceScreen
import com.pexip.sdk.sample.bandwidth.BandwidthScreen
import com.pexip.sdk.sample.dtmf.DtmfScreen
import com.pexip.sdk.sample.media.LocalMediaTrackRendering
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import org.webrtc.RendererCommon

data class ConferenceScreen(
    val splashScreen: SplashScreen?,
    val cameraVideoTrack: VideoTrack?,
    val mainRemoteVideoTrack: VideoTrack?,
    val presentationRemoteVideoTrack: VideoTrack?,
    val audioDeviceScreen: AudioDeviceScreen,
    val bandwidthScreen: BandwidthScreen,
    val dtmfScreen: DtmfScreen,
    val cameraVideoTrackRendering: LocalMediaTrackRendering?,
    val microphoneAudioTrackRendering: LocalMediaTrackRendering?,
    val screenCapturing: Boolean,
    val onScreenCapture: (Intent) -> Unit,
    val onAspectRatioChange: (Float) -> Unit,
    val onAudioDevicesChange: (Boolean) -> Unit,
    val onBandwidthChange: (Boolean) -> Unit,
    val onDtmfChange: (Boolean) -> Unit,
    val onStopScreenCapture: () -> Unit,
    val onChatClick: () -> Unit,
    val onBackClick: () -> Unit,
) : Screen

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ConferenceScreen(screen: ConferenceScreen, modifier: Modifier = Modifier) {
    BackHandler(onBack = screen.onBackClick)
    SampleTheme(darkTheme = true, systemBars = true) {
        Surface(modifier = modifier) {
            BoxWithConstraints {
                if (screen.splashScreen != null) {
                    SplashScreen(
                        splashScreen = screen.splashScreen,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                val landscape = remember(maxWidth, maxHeight) { maxWidth > maxHeight }
                if (landscape) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        MainVideoTrackRenderer(
                            rendering = screen,
                            modifier = Modifier.weight(1f, false),
                        )
                        PresentationVideoTrackRenderer(
                            rendering = screen,
                            modifier = Modifier.weight(1f, false),
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        MainVideoTrackRenderer(
                            rendering = screen,
                            modifier = Modifier.weight(1f, false),
                        )
                        PresentationVideoTrackRenderer(
                            rendering = screen,
                            modifier = Modifier.weight(1f, false),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeContentPadding(),
                ) {
                    AnimatedVisibility(
                        visible = screen.cameraVideoTrackRendering?.capturing == true,
                        enter = slideInHorizontally { it * 2 },
                        exit = slideOutHorizontally { it * 2 },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(0.25f)
                            .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.medium),
                    ) {
                        VideoTrackEmbeddedRenderer(
                            videoTrack = screen.cameraVideoTrack,
                            mirror = true,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.CenterHorizontally,
                        ),
                        modifier = Modifier.align(Alignment.TopStart),
                    ) {
                        MoreIconButton(rendering = screen)
                        ScreenShareIconButton(rendering = screen)
                        BandwidthIconButton(rendering = screen)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.CenterHorizontally,
                        ),
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        CameraIconButton(rendering = screen.cameraVideoTrackRendering)
                        MicrophoneIconButton(rendering = screen.microphoneAudioTrackRendering)
                        AudioDevicesIconButton(screen)
                        Spacer(Modifier.weight(1f))
                        EndCallIconButton(screen)
                    }
                }
            }
            WorkflowRendering(rendering = screen.dtmfScreen)
            WorkflowRendering(rendering = screen.audioDeviceScreen)
            WorkflowRendering(rendering = screen.bandwidthScreen)
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun MainVideoTrackRenderer(rendering: ConferenceScreen, modifier: Modifier = Modifier) {
    if (rendering.mainRemoteVideoTrack != null) {
        BoxWithConstraints(contentAlignment = Alignment.Center, modifier = modifier) {
            val viewportAspectRatio = remember(maxWidth, maxHeight) { maxWidth / maxHeight }
            val currentOnAspectRatioChange by rememberUpdatedState(rendering.onAspectRatioChange)
            LaunchedEffect(viewportAspectRatio) {
                currentOnAspectRatioChange(viewportAspectRatio)
            }
            VideoTrackRenderer(
                videoTrack = rendering.mainRemoteVideoTrack,
                scalingTypeMatchOrientation = RendererCommon.ScalingType.SCALE_ASPECT_FIT,
                modifier = Modifier.wrapContentSize(),
            )
        }
    }
}

@Composable
private fun PresentationVideoTrackRenderer(
    rendering: ConferenceScreen,
    modifier: Modifier = Modifier,
) {
    if (rendering.presentationRemoteVideoTrack != null) {
        VideoTrackRenderer(
            videoTrack = rendering.presentationRemoteVideoTrack,
            scalingTypeMatchOrientation = RendererCommon.ScalingType.SCALE_ASPECT_FIT,
            modifier = modifier.wrapContentSize(),
        )
    }
}

@Composable
private fun EndCallIconButton(rendering: ConferenceScreen, modifier: Modifier = Modifier) {
    IconButton(
        onClick = rendering.onBackClick,
        colors = IconButtonDefaults.iconButtonColors(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        modifier = modifier,
    ) {
        Icon(imageVector = Icons.Rounded.CallEnd, contentDescription = null)
    }
}

@Composable
private fun AudioDevicesIconButton(rendering: ConferenceScreen, modifier: Modifier = Modifier) {
    IconToggleButton(
        checked = rendering.audioDeviceScreen.visible,
        onCheckedChange = rendering.onAudioDevicesChange,
        modifier = modifier,
    ) {
        val type = rendering.audioDeviceScreen.selectedAudioDevice?.type
            ?: AudioDevice.Type.BUILTIN_SPEAKER
        AudioDeviceIcon(type = type)
    }
}

@Composable
private fun BandwidthIconButton(rendering: ConferenceScreen, modifier: Modifier = Modifier) {
    IconToggleButton(
        checked = rendering.bandwidthScreen.visible,
        onCheckedChange = rendering.onBandwidthChange,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Rounded.Speed,
            contentDescription = null,
        )
    }
}

@Composable
private fun ScreenShareIconButton(rendering: ConferenceScreen, modifier: Modifier = Modifier) {
    val manager = rememberMediaProjectionManager()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            if (it.resultCode == Activity.RESULT_OK && data != null) {
                rendering.onScreenCapture(data)
            }
        }
    val onToggleScreenCapture = when (rendering.screenCapturing) {
        true -> rendering.onStopScreenCapture
        else -> {
            { launcher.launch(manager.createScreenCaptureIntent()) }
        }
    }
    IconToggleButton(
        checked = rendering.screenCapturing,
        onCheckedChange = { onToggleScreenCapture() },
        modifier = modifier,
    ) {
        Icon(
            imageVector = when (rendering.screenCapturing) {
                true -> Icons.AutoMirrored.Rounded.StopScreenShare
                false -> Icons.AutoMirrored.Rounded.ScreenShare
            },
            contentDescription = null,
        )
    }
}

@Composable
private fun MoreIconButton(rendering: ConferenceScreen, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val onDismissRequest = { expanded = false }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
            DtmfItem(rendering = rendering, onDismissRequest = onDismissRequest)
            ConferenceEventsItem(rendering = rendering, onDismissRequest = onDismissRequest)
        }
    }
}

@Composable
private fun DtmfItem(
    rendering: ConferenceScreen,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        text = {
            Text(text = "DTMF")
        },
        onClick = {
            onDismissRequest()
            rendering.onDtmfChange(true)
        },
        leadingIcon = {
            Icon(imageVector = Icons.Rounded.Pin, contentDescription = null)
        },
        modifier = modifier,
    )
}

@Composable
private fun ConferenceEventsItem(
    rendering: ConferenceScreen,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        text = {
            Text(text = "Chat")
        },
        onClick = {
            onDismissRequest()
            rendering.onChatClick()
        },
        leadingIcon = {
            Icon(imageVector = Icons.AutoMirrored.Rounded.Message, contentDescription = null)
        },
        modifier = modifier,
    )
}

@Composable
private fun SplashScreen(splashScreen: SplashScreen, modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        AsyncImage(
            model = splashScreen.backgroundUrl,
            contentDescription = null,
        )
        LazyColumn {
            items(splashScreen.elements) {
                when (it) {
                    is Element.Text -> {
                        val color = remember(it.color) { Color(it.color) }
                        Text(
                            text = it.text,
                            color = color,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberMediaProjectionManager(): MediaProjectionManager {
    val context = LocalContext.current.applicationContext
    return remember(context) { context.getSystemService()!! }
}
