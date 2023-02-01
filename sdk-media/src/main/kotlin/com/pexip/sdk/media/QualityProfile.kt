/*
 * Copyright 2020-2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            fps = 30,
        )

        @JvmField
        public val High: QualityProfile = QualityProfile(
            width = 1280,
            height = 720,
            fps = 25,
        )

        @JvmField
        public val Medium: QualityProfile = QualityProfile(
            width = 720,
            height = 480,
            fps = 25,
        )

        @JvmField
        public val Low: QualityProfile = QualityProfile(
            width = 640,
            height = 360,
            fps = 15,
        )
    }
}
