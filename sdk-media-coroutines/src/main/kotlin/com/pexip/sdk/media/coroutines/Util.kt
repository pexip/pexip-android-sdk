package com.pexip.sdk.media.coroutines

import com.pexip.sdk.media.AudioDevice
import com.pexip.sdk.media.AudioDeviceManager
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Converts this [LocalMediaTrack] capturing state to a [Flow].
 *
 * @return a flow of changes to capturing state
 */
public fun LocalMediaTrack.getCapturing(): Flow<Boolean> = callbackFlow {
    val listener = LocalMediaTrack.CapturingListener(::trySend)
    registerCapturingListener(listener)
    awaitClose { unregisterCapturingListener(listener) }
}

/**
 * Attempts to switch a camera to the specified one or to the opposite one.
 *
 * @param deviceName a name of the camera to switch to, null to switch to opposite camera
 * @return true if the switched camera is front-facing, false otherwise
 * @throws IllegalStateException if device name doesn't exist
 * @throws SwitchCameraException if the operation failed
 */
public suspend fun CameraVideoTrack.switchCamera(deviceName: String? = null): Boolean =
    suspendCoroutine {
        val callback = object : CameraVideoTrack.SwitchCameraCallback {

            override fun onSuccess(front: Boolean) {
                it.resume(front)
            }

            override fun onFailure(error: String) {
                it.resumeWithException(SwitchCameraException(error))
            }
        }
        when (deviceName) {
            null -> switchCamera(callback)
            else -> switchCamera(deviceName, callback)
        }
    }

/**
 * Converts this [MediaConnection] to a main remote video track [Flow].
 *
 * @return a flow of main remote video track changes
 */
public fun MediaConnection.getMainRemoteVideoTrack(): Flow<VideoTrack?> = callbackFlow {
    val listener = MediaConnection.RemoteVideoTrackListener(::trySend)
    registerMainRemoteVideoTrackListener(listener)
    awaitClose { unregisterMainRemoteVideoTrackListener(listener) }
}

/**
 * Converts this [MediaConnection] to a presentation remote video track [Flow].
 *
 * @return a flow of presentation remote video track changes
 */
public fun MediaConnection.getPresentationRemoteVideoTrack(): Flow<VideoTrack?> = callbackFlow {
    val listener = MediaConnection.RemoteVideoTrackListener(::trySend)
    registerPresentationRemoteVideoTrackListener(listener)
    awaitClose { unregisterPresentationRemoteVideoTrackListener(listener) }
}

/**
 * Converts this [AudioDeviceManager] to available audio devices [Flow].
 *
 * @return a flow of available audio devices changes
 */
public fun AudioDeviceManager.getAvailableAudioDevices(): Flow<List<AudioDevice>> = callbackFlow {
    val listener = AudioDeviceManager.OnAvailableAudioDevicesChangedListener(::trySend)
    registerOnAvailableAudioDevicesChangedListener(listener)
    awaitClose { unregisterOnAvailableAudioDevicesChangedListener(listener) }
}

/**
 * Converts this [AudioDeviceManager] to available audio devices [StateFlow].
 *
 * @param scope the coroutine scope in which sharing is started
 * @param started the strategy that controls when sharing is started and stopped
 * @return a state flow of available audio devices
 */
public fun AudioDeviceManager.availableAudioDevicesIn(
    scope: CoroutineScope,
    started: SharingStarted,
): StateFlow<List<AudioDevice>> =
    getAvailableAudioDevices().stateIn(scope, started, availableAudioDevices)

/**
 * Converts this [AudioDeviceManager] to selected audio device [Flow].
 *
 * @return a flow of selected audio device changes
 */
public fun AudioDeviceManager.getSelectedAudioDevice(): Flow<AudioDevice?> = callbackFlow {
    val listener = AudioDeviceManager.OnSelectedAudioDeviceChangedListener(::trySend)
    registerOnSelectedAudioDeviceChanged(listener)
    awaitClose { unregisterOnSelectedAudioDeviceChanged(listener) }
}

/**
 * Converts this [AudioDeviceManager] to selected audio device [StateFlow].
 *
 * @param scope the coroutine scope in which sharing is started
 * @param started the strategy that controls when sharing is started and stopped
 * @return a state flow of selected audio device
 */
public fun AudioDeviceManager.selectedAudioDeviceIn(
    scope: CoroutineScope,
    started: SharingStarted,
): StateFlow<AudioDevice?> = getSelectedAudioDevice().stateIn(scope, started, selectedAudioDevice)
