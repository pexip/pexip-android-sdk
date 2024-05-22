/*
 * Copyright 2023-2024 Pexip AS
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
package com.pexip.sdk.conference

import com.pexip.sdk.infinity.LayoutId
import kotlinx.coroutines.flow.StateFlow

/**
 * Handles theme-related functionality of the [Conference].
 */
public interface Theme {

    /**
     * A [StateFlow] of the logo URL
     */
    public val avatar: StateFlow<String>
        get() = throw NotImplementedError()

    /**
     * A [StateFlow] of current [Layout] state.
     */
    public val layout: StateFlow<Layout?>
        get() = throw NotImplementedError()

    /**
     * A [StateFlow] of [SplashScreen]s that should be displayed.
     */
    public val splashScreen: StateFlow<SplashScreen?>
        get() = throw NotImplementedError()

    /**
     * Transforms the current conference layout given sufficient privileges.
     *
     * Calling this method with the default parameters resets any prior transformations applied
     * to the conference layout.
     *
     * Changes are applied cumulatively.
     *
     * @param layout a layout visible to hosts and guests
     * @param guestLayout a layout visible to guests in Virtual Auditoriums
     * @param enableOverlayText true if per-participant overlay text is enabled, false otherwise
     * @throws TransformLayoutException if layout transformation was not applied
     */
    public suspend fun transformLayout(
        layout: LayoutId? = null,
        guestLayout: LayoutId? = null,
        enableOverlayText: Boolean? = null,
    ) {
        throw NotImplementedError()
    }
}
