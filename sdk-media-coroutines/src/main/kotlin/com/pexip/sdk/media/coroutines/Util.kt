package com.pexip.sdk.media.coroutines

import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.VideoTrack
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public fun LocalVideoTrack.getCapturing(): Flow<Boolean> = callbackFlow {
    val listener = LocalVideoTrack.CapturingListener(::trySend)
    registerCapturingListener(listener)
    awaitClose { unregisterCapturingListener(listener) }
}

public fun MediaConnection.getMainRemoteVideoTrack(): Flow<VideoTrack?> = callbackFlow {
    val listener = MediaConnection.RemoteVideoTrackListener(::trySend)
    registerMainRemoteVideoTrackListener(listener)
    awaitClose { unregisterMainRemoteVideoTrackListener(listener) }
}

public fun MediaConnection.getPresentationRemoteVideoTrack(): Flow<VideoTrack?> = callbackFlow {
    val listener = MediaConnection.RemoteVideoTrackListener(::trySend)
    registerPresentationRemoteVideoTrackListener(listener)
    awaitClose { unregisterPresentationRemoteVideoTrackListener(listener) }
}
