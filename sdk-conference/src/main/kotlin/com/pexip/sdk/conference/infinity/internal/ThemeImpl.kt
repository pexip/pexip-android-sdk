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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.ElementResponse
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.LayoutEvent
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.SplashScreenEvent
import com.pexip.sdk.api.infinity.SplashScreenResponse
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.Element
import com.pexip.sdk.conference.Layout
import com.pexip.sdk.conference.LayoutId
import com.pexip.sdk.conference.SplashScreen
import com.pexip.sdk.conference.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds
import com.pexip.sdk.api.infinity.LayoutId as ApiLayoutId

internal class ThemeImpl(
    scope: CoroutineScope,
    event: Flow<Event>,
    private val step: InfinityService.ConferenceStep,
    private val store: TokenStore,
) : Theme {

    override val layout: StateFlow<Layout?> = combine(
        flow = event.filterIsInstance<LayoutEvent>(),
        flow2 = step.availableLayouts(store),
        flow3 = step.layoutSvgs(store),
        transform = ::toLayout,
    ).stateIn(scope, SharingStarted.Eagerly, null)

    override val splashScreen: StateFlow<SplashScreen?> = event
        .filterIsInstance<SplashScreenEvent>()
        .combine(step.theme(store), ::toSplashScreen)
        .stateIn(scope, SharingStarted.Eagerly, null)

    private fun toLayout(
        event: LayoutEvent,
        layoutIds: Set<ApiLayoutId>,
        layoutSvgs: Map<ApiLayoutId, String>,
    ) = Layout(
        layout = LayoutId(event.layout),
        layouts = layoutIds.asSequence().map(::LayoutId).toSet(),
        requestedPrimaryScreenHostLayout = LayoutId(event.requestedLayout.primaryScreen.hostLayout),
        requestedPrimaryScreenGuestLayout = LayoutId(event.requestedLayout.primaryScreen.guestLayout),
        overlayTextEnabled = event.overlayTextEnabled,
        layoutSvgs = layoutSvgs.mapKeys { LayoutId(it.key) },
    )

    private fun toSplashScreen(
        event: SplashScreenEvent,
        responses: Map<String, SplashScreenResponse>,
    ): SplashScreen? {
        val key = event.screenKey ?: return null
        val response = responses[key] ?: return null
        return SplashScreen(
            key = key,
            elements = response.elements.mapNotNull(::toElement),
            backgroundUrl = step.theme(response.background.path, store.get()),
        )
    }

    private fun toElement(response: ElementResponse) = when (response) {
        is ElementResponse.Text -> Element.Text(response.color, response.text)
        is ElementResponse.Unknown -> null
    }

    private fun InfinityService.ConferenceStep.availableLayouts(store: TokenStore) = store.asFlow()
        .map { availableLayouts(it).await() }
        .retryOrDefault(::emptySet)

    private fun InfinityService.ConferenceStep.layoutSvgs(store: TokenStore) = store.asFlow()
        .map { layoutSvgs(it).await() }
        .retryOrDefault(::emptyMap)

    private fun InfinityService.ConferenceStep.theme(store: TokenStore) = store.asFlow()
        .map { theme(it).await() }
        .retryOrDefault(::emptyMap)

    private fun TokenStore.asFlow() = flow { emit(get()) }

    private fun LayoutId(id: ApiLayoutId): LayoutId = LayoutId(id.value)

    private fun <T> Flow<T>.retryOrDefault(value: () -> T) = retryWhen { cause, attempt ->
        when (cause) {
            // In a rare case when the method doesn't exist Infinity will return 404 which maps
            // to this exception
            is NoSuchConferenceException -> false
            else -> {
                delay(attempt.seconds.coerceAtMost(5.seconds))
                true
            }
        }
    }.catch { emit(value()) }
}
