package com.pexip.sdk.media.android

import com.pexip.sdk.media.MediaConnectionFactory

/**
 * Android variant of [MediaConnectionFactory].
 */
@Deprecated(
    message = "Use MediaConnectionFactory, MediaProjectionVideoTrackFactory directly instead.",
    level = DeprecationLevel.ERROR
)
public interface AndroidMediaConnectionFactory :
    MediaConnectionFactory, MediaProjectionVideoTrackFactory
