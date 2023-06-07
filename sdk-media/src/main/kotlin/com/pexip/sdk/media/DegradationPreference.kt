/*
 * Copyright 2023 Pexip AS
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
 * A strategy on how to approach low bandwidth conditions for video streams.
 */
public enum class DegradationPreference {

    /**
     * Degrades both framerate and resolution.
     */
    BALANCED,

    /**
     * Degrades resolution while trying to preserve the framerate.
     */
    MAINTAIN_FRAMERATE,

    /**
     * Degrades framerate while trying to preserve the resolution.
     */
    MAINTAIN_RESOLUTION,

    /**
     * Degrades neither framerate nor resolution.
     */
    DISABLED,
}
