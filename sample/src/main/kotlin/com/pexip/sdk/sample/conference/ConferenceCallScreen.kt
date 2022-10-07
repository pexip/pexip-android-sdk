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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.StopScreenShare
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pexip.sdk.media.AudioDevice
import com.pexip.sdk.media.webrtc.compose.VideoTrackRenderer
import com.pexip.sdk.sample.CameraIconButton
import com.pexip.sdk.sample.IconButton
import com.pexip.sdk.sample.IconButtonDefaults
import com.pexip.sdk.sample.IconToggleButton
import com.pexip.sdk.sample.MicrophoneIconButton
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
    Surface(color = Color.Black, modifier = modifier) {
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
                    visible = rendering.cameraVideoTrackRendering?.capturing == true,
                    enter = slideInHorizontally { it * 2 },
                    exit = slideOutHorizontally { it * 2 },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxWidth(0.25f)
                        .aspectRatio(aspectRatio)
                ) {
                    VideoTrackRenderer(
                        videoTrack = rendering.cameraVideoTrack,
                        mirror = true
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    MoreIconButton(rendering = rendering)
                    ScreenShareIconButton(rendering = rendering)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    CameraIconButton(rendering = rendering.cameraVideoTrackRendering)
                    MicrophoneIconButton(rendering = rendering.microphoneAudioTrackRendering)
                    AudioDevicesIconButton(rendering)
                    Spacer(Modifier.weight(1f))
                    EndCallIconButton(rendering)
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
private fun EndCallIconButton(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
    IconButton(
        onClick = rendering.onBackClick,
        colors = IconButtonDefaults.iconButtonColors(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = modifier
    ) {
        Icon(imageVector = Icons.Rounded.CallEnd, contentDescription = null)
    }
}

@Composable
private fun AudioDevicesIconButton(
    rendering: ConferenceCallRendering,
    modifier: Modifier = Modifier,
) {
    IconToggleButton(
        checked = rendering.audioDeviceRendering.visible,
        onCheckedChange = rendering.onAudioDevicesChange,
        modifier = modifier
    ) {
        val type = rendering.audioDeviceRendering.selectedAudioDevice?.type
            ?: AudioDevice.Type.BUILTIN_SPEAKER
        AudioDeviceIcon(type = type)
    }
}

@Composable
private fun ScreenShareIconButton(
    rendering: ConferenceCallRendering,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier
    ) {
        Icon(
            imageVector = when (rendering.screenCapturing) {
                true -> Icons.Rounded.StopScreenShare
                false -> Icons.Rounded.ScreenShare
            },
            contentDescription = null
        )
    }
}

@Composable
private fun MoreIconButton(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
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
            rendering.onDtmfChange(true)
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
