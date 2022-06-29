package com.pexip.sdk.media.coroutines

import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.VideoTrack
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
