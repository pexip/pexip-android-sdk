package com.pexip.sdk.media.webrtc.coroutines

import com.pexip.sdk.media.webrtc.WebRtcMediaConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.webrtc.VideoTrack

@Deprecated("Use WebRtcMediaConnectionFactory.createCameraVideoTrack().", level = DeprecationLevel.ERROR)
public fun WebRtcMediaConnection.getMainLocalVideoTrack(): Flow<VideoTrack?> = flow { }

@Deprecated("Use MediaConnection.getMainRemoteVideoTrack().", level = DeprecationLevel.ERROR)
public fun WebRtcMediaConnection.getMainRemoteVideoTrack(): Flow<VideoTrack?> = flow { }

@Deprecated("Use MediaConnection.getPresentationRemoteVideoTrack().", level = DeprecationLevel.ERROR)
public fun WebRtcMediaConnection.getPresentationRemoteVideoTrack(): Flow<VideoTrack?> = flow { }
