package com.pexip.sdk.media.android

import com.pexip.sdk.media.MediaConnectionFactory

/**
 * Android variant of [MediaConnectionFactory].
 */
@Deprecated("Use MediaConnectionFactory, MediaProjectionVideoTrackFactory directly instead.")
public interface AndroidMediaConnectionFactory :
    MediaConnectionFactory, MediaProjectionVideoTrackFactory
