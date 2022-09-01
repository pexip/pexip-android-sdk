package com.pexip.sdk.sample.conference

import android.app.Activity
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.StopScreenShare
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pexip.sdk.media.AudioDevice
import com.pexip.sdk.media.webrtc.compose.VideoTrackRenderer
import com.pexip.sdk.sample.audio.AudioDeviceIcon
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.WorkflowRendering

@Composable
fun ConferenceCallScreen(
    rendering: ConferenceCallRendering,
    environment: ViewEnvironment,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = rendering.onBackClick)
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        val systemBarsBehavior = systemUiController.systemBarsBehavior
        systemUiController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        systemUiController.isSystemBarsVisible = false
        onDispose {
            systemUiController.systemBarsBehavior = systemBarsBehavior
            systemUiController.isSystemBarsVisible = true
        }
    }
    Surface(modifier = modifier) {
        BoxWithConstraints {
            val landscape = remember(maxWidth, maxHeight) { maxWidth > maxHeight }
            if (landscape) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    MainVideoTrackRenderer(
                        rendering = rendering,
                        modifier = Modifier.weight(1f, false)
                    )
                    PresentationVideoTrackRenderer(
                        rendering = rendering,
                        modifier = Modifier.weight(1f, false)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    MainVideoTrackRenderer(
                        rendering = rendering,
                        modifier = Modifier.weight(1f, false)
                    )
                    PresentationVideoTrackRenderer(
                        rendering = rendering,
                        modifier = Modifier.weight(1f, false)
                    )
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding()
            ) {
                val aspectRatio = remember(landscape) {
                    if (landscape) ASPECT_RATIO_LANDSCAPE else ASPECT_RATIO_PORTRAIT
                }
                AnimatedVisibility(
                    visible = rendering.cameraCapturing,
                    enter = slideInHorizontally { it * 2 },
                    exit = slideOutHorizontally { it * 2 },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxWidth(0.2f)
                        .aspectRatio(aspectRatio)
                ) {
                    VideoTrackRenderer(
                        videoTrack = rendering.cameraVideoTrack,
                        mirror = true
                    )
                }
                MoreButton(
                    rendering = rendering,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    ScreenShareButton(rendering)
                    CameraButton(rendering)
                    EndCallButton(rendering)
                    MicrophoneButton(rendering)
                    AudioDevicesButton(rendering)
                }
            }
        }
        WorkflowRendering(
            rendering = rendering.dtmfRendering,
            viewEnvironment = environment
        )
        WorkflowRendering(
            rendering = rendering.audioDeviceRendering,
            viewEnvironment = environment
        )
    }
}

@Composable
private fun MainVideoTrackRenderer(
    rendering: ConferenceCallRendering,
    modifier: Modifier = Modifier,
) {
    VideoTrackRenderer(
        videoTrack = rendering.mainRemoteVideoTrack,
        modifier = modifier.aspectRatio(ASPECT_RATIO_LANDSCAPE)
    )
}

@Composable
private fun PresentationVideoTrackRenderer(
    rendering: ConferenceCallRendering,
    modifier: Modifier = Modifier,
) {
    if (rendering.presentationRemoteVideoTrack != null) {
        VideoTrackRenderer(
            videoTrack = rendering.presentationRemoteVideoTrack,
            modifier = modifier.aspectRatio(ASPECT_RATIO_LANDSCAPE)
        )
    }
}

@Composable
private fun EndCallButton(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = rendering.onBackClick,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier
    ) {
        Icon(imageVector = Icons.Rounded.CallEnd, contentDescription = null)
    }
}

@Composable
private fun CameraButton(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
    SmallFloatingActionButton(
        onClick = rendering.onToggleCameraCapturing,
        modifier = modifier
    ) {
        val imageVector = remember(rendering.cameraCapturing) {
            when (rendering.cameraCapturing) {
                true -> Icons.Rounded.Videocam
                false -> Icons.Rounded.VideocamOff
            }
        }
        Icon(imageVector = imageVector, contentDescription = null)
    }
}

@Composable
private fun MicrophoneButton(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
    SmallFloatingActionButton(
        onClick = rendering.onToggleLocalAudioCapturing,
        modifier = modifier
    ) {
        val imageVector = remember(rendering.localAudioCapturing) {
            when (rendering.localAudioCapturing) {
                true -> Icons.Rounded.Mic
                false -> Icons.Rounded.MicOff
            }
        }
        Icon(imageVector = imageVector, contentDescription = null)
    }
}

@Composable
private fun AudioDevicesButton(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
    SmallFloatingActionButton(
        onClick = rendering.onToggleAudioDevicesClick,
        modifier = modifier
    ) {
        val type = rendering.audioDeviceRendering.selectedAudioDevice?.type
            ?: AudioDevice.Type.BUILTIN_SPEAKER
        AudioDeviceIcon(type = type)
    }
}

@Composable
private fun ScreenShareButton(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
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
    SmallFloatingActionButton(onClick = onToggleScreenCapture, modifier = modifier) {
        val imageVector = remember(rendering.screenCapturing) {
            when (rendering.screenCapturing) {
                true -> Icons.Rounded.StopScreenShare
                false -> Icons.Rounded.ScreenShare
            }
        }
        Icon(imageVector = imageVector, contentDescription = null)
    }
}

@Composable
private fun MoreButton(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val onDismissRequest = { expanded = false }
    Box(modifier = modifier) {
        SmallFloatingActionButton(onClick = { expanded = true }) {
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
    rendering: ConferenceCallRendering,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        text = {
            Text(text = "DTMF")
        },
        onClick = {
            onDismissRequest()
            rendering.onToggleDtmfClick()
        },
        leadingIcon = {
            Icon(imageVector = Icons.Rounded.Pin, contentDescription = null)
        },
        modifier = modifier
    )
}

@Composable
private fun ConferenceEventsItem(
    rendering: ConferenceCallRendering,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        text = {
            Text(text = "Events")
        },
        onClick = {
            onDismissRequest()
            rendering.onConferenceEventsClick()
        },
        leadingIcon = {
            Icon(imageVector = Icons.Rounded.Message, contentDescription = null)
        },
        modifier = modifier
    )
}

@Composable
private fun rememberMediaProjectionManager(): MediaProjectionManager {
    val context = LocalContext.current.applicationContext
    return remember(context) { context.getSystemService()!! }
}

private const val ASPECT_RATIO_PORTRAIT = 9 / 16f
private const val ASPECT_RATIO_LANDSCAPE = 16 / 9f
