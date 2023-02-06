/*
 * Copyright 2022 Pexip AS
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
package com.pexip.sdk.media.webrtc.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import org.webrtc.EglBase

/**
 * CompositionLocal containing the global [EglBase] instance.
 *
 * This [EglBase] is used to provide hardware rendering capabilities to your [VideoTrackRenderer].
 *
 * Must be explicitly set. It may be set to `null` if software rendering is used.
 */
public val LocalEglBase: ProvidableCompositionLocal<EglBase?> =
    staticCompositionLocalOf { error("Must be set explicitly.") }

/**
 * CompositionLocal containing the config used to create [EglBase] instance.
 */
public val LocalEglBaseConfigAttributes: ProvidableCompositionLocal<IntArray> =
    staticCompositionLocalOf { EglBase.CONFIG_PLAIN }
