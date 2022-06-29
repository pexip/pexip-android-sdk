package com.pexip.sdk.media

/**
 * A quality profile used to capture the video.
 *
 * @property width a frame width in pixels (must be in the range of 640..1920)
 * @property height a frame height in pixels (must be in the range of 360..1080)
 * @property fps a FPS value used to capture the video (must be in the range of 1..60)
 */
public data class QualityProfile(
    public val width: Int,
    public val height: Int,
    public val fps: Int,
) {

    init {
        require(width in 640..1920)
        require(height in 360..1080)
        require(fps in 1..60)
    }

    public companion object {

        @JvmField
        public val VeryHigh: QualityProfile = QualityProfile(
            width = 1920,
            height = 1080,
            fps = 30
        )

        @JvmField
        public val High: QualityProfile = QualityProfile(
            width = 1280,
            height = 720,
            fps = 25
        )

        @JvmField
        public val Medium: QualityProfile = QualityProfile(
            width = 720,
            height = 480,
            fps = 25
        )

        @JvmField
        public val Low: QualityProfile = QualityProfile(
            width = 640,
            height = 360,
            fps = 15
        )
    }
}
