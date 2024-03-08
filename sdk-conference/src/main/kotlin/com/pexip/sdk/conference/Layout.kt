/*
 * Copyright 2024 Pexip AS
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

/**
 * The layout of this [Conference].
 *
 * @property layout a currently visible layout (may be absent from [layouts] in some cases)
 * @property layouts a set of all available layouts
 * @property requestedPrimaryScreenHostLayout a requested layout visible on primary screen for hosts, may be null in direct media call
 * @property requestedPrimaryScreenGuestLayout a requested layout visible on primary screen for guests, may be null in direct media call
 * @property overlayTextEnabled true if overlay text is enabled, false otherwise
 */
public data class Layout(
    val layout: LayoutId,
    val layouts: Set<LayoutId>,
    val requestedPrimaryScreenHostLayout: LayoutId?,
    val requestedPrimaryScreenGuestLayout: LayoutId?,
    val overlayTextEnabled: Boolean,
    val layoutSvgs: Map<LayoutId, String>,
)
