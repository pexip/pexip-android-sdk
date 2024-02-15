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

import app.cash.turbine.test
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.BackgroundResponse
import com.pexip.sdk.api.infinity.ElementResponse
import com.pexip.sdk.api.infinity.LayoutEvent
import com.pexip.sdk.api.infinity.SplashScreenEvent
import com.pexip.sdk.api.infinity.SplashScreenResponse
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.Element
import com.pexip.sdk.conference.Layout
import com.pexip.sdk.conference.LayoutId
import com.pexip.sdk.conference.SplashScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import com.pexip.sdk.api.infinity.LayoutId as ApiLayoutId
import com.pexip.sdk.api.infinity.RequestedLayout as ApiRequestedLayout
import com.pexip.sdk.api.infinity.Screen as ApiScreen

class ThemeImplTest {

    private lateinit var event: MutableSharedFlow<Event>
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        event = MutableSharedFlow(extraBufferCapacity = 1)
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `emits the current layout`() = runTest {
        val response = generateSequence { Random.nextString(8) }
            .map(::ApiLayoutId)
            .take(10)
            .toSet()
        val step = object : TestConferenceStep() {

            override fun availableLayouts(token: String): Call<Set<ApiLayoutId>> {
                assertThat(token).isEqualTo(store.get().token)
                return object : TestCall<Set<ApiLayoutId>> {

                    override fun enqueue(callback: Callback<Set<ApiLayoutId>>) {
                        callback.onSuccess(this, response)
                    }
                }
            }
        }
        val theme = ThemeImpl(
            scope = backgroundScope,
            event = event,
            step = step,
            store = store,
        )
        theme.layout.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "layout").isNull()
            repeat(10) {
                val e = LayoutEvent(
                    layout = response.random(),
                    requestedLayout = ApiRequestedLayout(
                        primaryScreen = ApiScreen(
                            hostLayout = response.random(),
                            guestLayout = response.random(),
                        ),
                    ),
                    overlayTextEnabled = Random.nextBoolean(),
                )
                event.emit(e)
                assertThat(awaitItem(), "layout")
                    .isNotNull()
                    .all {
                        prop(Layout::layout).isEqualTo(e.layout)
                        prop(Layout::layouts)
                            .extracting(LayoutId::value)
                            .containsExactly(*response.map(ApiLayoutId::value).toTypedArray())
                        prop(Layout::requestedPrimaryScreenHostLayout)
                            .isEqualTo(e.requestedLayout.primaryScreen.hostLayout)
                        prop(Layout::requestedPrimaryScreenGuestLayout)
                            .isEqualTo(e.requestedLayout.primaryScreen.guestLayout)
                        prop(Layout::overlayTextEnabled).isEqualTo(e.overlayTextEnabled)
                    }
            }
            expectNoEvents()
        }
    }

    @Test
    fun `emits the current splash screen`() = runTest {
        fun pathToUrl(path: String) = "https://$path.com"

        val response = generateSequence { Random.nextString(8) }
            .take(10)
            .associateWith {
                SplashScreenResponse(
                    background = BackgroundResponse(Random.nextString(8)),
                    elements = List(10) {
                        ElementResponse.Text(
                            color = Random.nextLong(Long.MAX_VALUE),
                            text = Random.nextString(8),
                        )
                    },
                )
            }
        val step = object : TestConferenceStep() {

            override fun theme(token: String): Call<Map<String, SplashScreenResponse>> {
                assertThat(token).isEqualTo(store.get().token)
                return object : TestCall<Map<String, SplashScreenResponse>> {

                    override fun enqueue(callback: Callback<Map<String, SplashScreenResponse>>) {
                        callback.onSuccess(this, response)
                    }
                }
            }

            override fun theme(path: String, token: String): String {
                assertThat(token).isEqualTo(store.get().token)
                return pathToUrl(path)
            }
        }
        val theme = ThemeImpl(
            scope = backgroundScope,
            event = event,
            step = step,
            store = store,
        )
        theme.splashScreen.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem()).isNull()
            event.emit(SplashScreenEvent(Random.nextString(8)))
            expectNoEvents()
            response.forEach { (key, response) ->
                event.emit(SplashScreenEvent(key))
                val expected = SplashScreen(
                    key = key,
                    elements = response.elements.mapNotNull {
                        when (it) {
                            is ElementResponse.Text -> Element.Text(
                                color = it.color,
                                text = it.text,
                            )
                            is ElementResponse.Unknown -> null
                        }
                    },
                    backgroundUrl = pathToUrl(response.background.path),
                )
                assertThat(awaitItem()).isEqualTo(expected)
            }
            event.emit(SplashScreenEvent())
            assertThat(awaitItem()).isNull()
        }
    }

    private fun Assert<LayoutId>.isEqualTo(id: ApiLayoutId) =
        prop(LayoutId::value).isEqualTo(id.value)
}
