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
