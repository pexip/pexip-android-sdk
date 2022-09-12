package com.pexip.sdk.media

/**
 * A quality profile used to capture the video.
 *
 * @property width a frame width in pixels
 * @property height a frame height in pixels
 * @property fps a FPS value used to capture the video
 */
public data class QualityProfile(
    public val width: Int,
    public val height: Int,
    public val fps: Int,
) {

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
