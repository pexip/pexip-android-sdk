package com.pexip.sdk.sample.media

data class LocalMediaTrackRendering(
    val capturing: Boolean,
    val onCapturingChange: (Boolean) -> Unit,
)
