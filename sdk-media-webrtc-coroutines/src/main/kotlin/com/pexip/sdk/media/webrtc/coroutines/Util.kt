package com.pexip.sdk.media.webrtc.coroutines

import com.pexip.sdk.media.webrtc.VideoTrackListener
import com.pexip.sdk.media.webrtc.WebRtcMediaConnection
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.webrtc.VideoTrack

public fun WebRtcMediaConnection.getMainLocalVideoTrack(): Flow<VideoTrack?> = getVideoTrack(
    register = ::registerMainLocalVideoTrackListener,
    unregister = ::unregisterMainLocalVideoTrackListener
)

public fun WebRtcMediaConnection.getMainRemoteVideoTrack(): Flow<VideoTrack?> = getVideoTrack(
    register = ::registerMainRemoteVideoTrackListener,
    unregister = ::unregisterMainRemoteVideoTrackListener
)

public fun WebRtcMediaConnection.getPresentationLocalVideoTrack(): Flow<VideoTrack?> =
    getVideoTrack(
        register = ::registerPresentationLocalVideoTrackListener,
        unregister = ::unregisterPresentationLocalVideoTrackListener
    )

public fun WebRtcMediaConnection.getPresentationRemoteVideoTrack(): Flow<VideoTrack?> =
    getVideoTrack(
        register = ::registerPresentationRemoteVideoTrackListener,
        unregister = ::unregisterPresentationRemoteVideoTrackListener
    )

private inline fun getVideoTrack(
    crossinline register: (VideoTrackListener) -> Unit,
    crossinline unregister: (VideoTrackListener) -> Unit,
) = callbackFlow {
    val listener = object : VideoTrackListener {
        override fun onVideoTrack(videoTrack: VideoTrack?) {
            trySend(videoTrack)
        }
    }
    register(listener)
    awaitClose { unregister(listener) }
}.distinctUntilChanged()
