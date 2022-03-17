package com.pexip.sdk.video.conference.coroutines

import com.pexip.sdk.video.conference.CallHandler
import com.pexip.sdk.video.conference.VideoTrack
import com.pexip.sdk.video.conference.VideoTrackListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

public fun CallHandler.localVideoTrack(): Flow<VideoTrack?> = callbackFlow {
    val listener = VideoTrackListener { trySend(it) }
    registerLocalVideoTrackListener(listener)
    awaitClose { unregisterLocalVideoTrackListener(listener) }
}.distinctUntilChanged()

public fun CallHandler.remoteVideoTrack(): Flow<VideoTrack?> = callbackFlow {
    val listener = VideoTrackListener { trySend(it) }
    registerRemoteVideoTrackListener(listener)
    awaitClose { unregisterRemoteVideoTrackListener(listener) }
}.distinctUntilChanged()
