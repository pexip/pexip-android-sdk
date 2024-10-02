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
import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasCause
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.BackgroundResponse
import com.pexip.sdk.api.infinity.ElementResponse
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.LayoutEvent
import com.pexip.sdk.api.infinity.SplashScreenEvent
import com.pexip.sdk.api.infinity.SplashScreenResponse
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.TransformLayoutRequest
import com.pexip.sdk.conference.Element
import com.pexip.sdk.conference.Layout
import com.pexip.sdk.conference.SplashScreen
import com.pexip.sdk.conference.TransformLayoutException
import com.pexip.sdk.core.awaitSubscriptionCountAtLeast
import com.pexip.sdk.infinity.LayoutId
import com.pexip.sdk.infinity.test.nextLayoutId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import com.pexip.sdk.api.infinity.RequestedLayout as ApiRequestedLayout
import com.pexip.sdk.api.infinity.Screen as ApiScreen

class ThemeImplTest {

    private lateinit var event: MutableSharedFlow<Event>
    private lateinit var store: TokenStore

    // Can't use tableOf since forAll is not inline, meaning no calls to suspend functions
    private val transformLayoutPermutations by lazy {
        listOf(
            Triple(Random.nextLayoutId(), Random.nextLayoutId(), true),
            Triple(Random.nextLayoutId(), Random.nextLayoutId(), false),
            Triple(Random.nextLayoutId(), Random.nextLayoutId(), null),
            Triple(Random.nextLayoutId(), null, true),
            Triple(Random.nextLayoutId(), null, false),
            Triple(Random.nextLayoutId(), null, null),
            Triple(null, Random.nextLayoutId(), true),
            Triple(null, Random.nextLayoutId(), false),
            Triple(null, Random.nextLayoutId(), null),
            Triple(null, null, true),
            Triple(null, null, false),
            Triple(null, null, null),
        )
    }

    @BeforeTest
    fun setUp() {
        event = MutableSharedFlow(extraBufferCapacity = 1)
        store = TokenStore(Random.nextToken())
    }

    @Test
    fun `emits the current layout`() = runTest {
        val layouts = generateSequence { Random.nextLayoutId() }
            .take(10)
            .toSet()
        val layoutSvgs = layouts.associateWith { Random.nextString() }
        val step = object : InfinityService.ConferenceStep {

            override fun availableLayouts(token: Token): Call<Set<LayoutId>> {
                assertThat(token).isEqualTo(store.token.value)
                return object : TestCall<Set<LayoutId>> {

                    override fun enqueue(callback: Callback<Set<LayoutId>>) {
                        callback.onSuccess(this, layouts)
                    }
                }
            }

            override fun layoutSvgs(token: Token): Call<Map<LayoutId, String>> {
                assertThat(token).isEqualTo(store.token.value)
                return object : TestCall<Map<LayoutId, String>> {

                    override fun enqueue(callback: Callback<Map<LayoutId, String>>) {
                        callback.onSuccess(this, layoutSvgs)
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
            event.awaitSubscriptionCountAtLeast(2)
            assertThat(awaitItem(), "layout").isNull()
            assertThat(awaitItem(), "layout")
                .isNotNull()
                .all {
                    prop(Layout::layout).isEqualTo(LayoutId(""))
                    prop(Layout::layouts).isEqualTo(layouts)
                    prop(Layout::requestedPrimaryScreenHostLayout).isNull()
                    prop(Layout::requestedPrimaryScreenGuestLayout).isNull()
                    prop(Layout::overlayTextEnabled).isFalse()
                    prop(Layout::layoutSvgs).isEqualTo(layoutSvgs)
                }
            repeat(10) {
                val e = LayoutEvent(
                    layout = layouts.random(),
                    requestedLayout = ApiRequestedLayout(
                        primaryScreen = ApiScreen(
                            hostLayout = layouts.random(),
                            guestLayout = layouts.random(),
                        ),
                    ),
                    overlayTextEnabled = Random.nextBoolean(),
                )
                event.emit(e)
                assertThat(awaitItem(), "layout")
                    .isNotNull()
                    .all {
                        prop(Layout::layout).isEqualTo(e.layout)
                        prop(Layout::layouts).isEqualTo(layouts)
                        prop(Layout::requestedPrimaryScreenHostLayout)
                            .isNotNull()
                            .isEqualTo(e.requestedLayout?.primaryScreen?.hostLayout)
                        prop(Layout::requestedPrimaryScreenGuestLayout)
                            .isNotNull()
                            .isEqualTo(e.requestedLayout?.primaryScreen?.guestLayout)
                        prop(Layout::overlayTextEnabled).isEqualTo(e.overlayTextEnabled)
                        prop(Layout::layoutSvgs).isEqualTo(layoutSvgs)
                    }
            }
            expectNoEvents()
        }
    }

    @Test
    fun `emits the current splash screen`() = runTest {
        fun pathToUrl(path: String) = "https://$path.com"

        val response = generateSequence { Random.nextString() }
            .take(10)
            .associateWith {
                SplashScreenResponse(
                    background = BackgroundResponse(Random.nextString()),
                    elements = List(10) {
                        ElementResponse.Text(
                            color = Random.nextLong(Long.MAX_VALUE),
                            text = Random.nextString(),
                        )
                    },
                )
            }
        val step = object : InfinityService.ConferenceStep {

            override fun theme(token: Token): Call<Map<String, SplashScreenResponse>> {
                assertThat(token).isEqualTo(store.token.value)
                return object : TestCall<Map<String, SplashScreenResponse>> {

                    override fun enqueue(callback: Callback<Map<String, SplashScreenResponse>>) {
                        callback.onSuccess(this, response)
                    }
                }
            }

            override fun theme(path: String, token: Token): String {
                assertThat(token).isEqualTo(store.token.value)
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
            event.awaitSubscriptionCountAtLeast(2)
            assertThat(awaitItem()).isNull()
            event.emit(SplashScreenEvent(Random.nextString()))
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

    @Test
    fun `transformLayout() throws`() = runTest {
        val t = List(transformLayoutPermutations.size) { Throwable() }
        transformLayoutPermutations
            .forEachIndexed { i, (layout, guestLayout, enableOverlayText) ->
                val step = object : InfinityService.ConferenceStep {

                    override fun transformLayout(
                        request: TransformLayoutRequest,
                        token: Token,
                    ): Call<Boolean> {
                        assertThat(request::layout).isEqualTo(layout)
                        assertThat(request::guestLayout).isEqualTo(guestLayout)
                        assertThat(request::enableOverlayText).isEqualTo(enableOverlayText)
                        return object : TestCall<Boolean> {

                            override fun enqueue(callback: Callback<Boolean>) =
                                callback.onFailure(this, t[i])
                        }
                    }
                }
                val theme = ThemeImpl(
                    scope = backgroundScope,
                    event = event,
                    step = step,
                    store = store,
                )
                assertFailure { theme.transformLayout(layout, guestLayout, enableOverlayText) }
                    .isInstanceOf<TransformLayoutException>()
                    .hasCause(t[i])
            }
    }

    @Test
    fun `transformLayout() succeeds`() = runTest {
        transformLayoutPermutations.forEach { (layout, guestLayout, enableOverlayText) ->
            val step = object : InfinityService.ConferenceStep {

                override fun transformLayout(
                    request: TransformLayoutRequest,
                    token: Token,
                ): Call<Boolean> {
                    assertThat(request::layout).isEqualTo(layout)
                    assertThat(request::guestLayout).isEqualTo(guestLayout)
                    assertThat(request::enableOverlayText).isEqualTo(enableOverlayText)
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            }
            val theme = ThemeImpl(
                scope = backgroundScope,
                event = event,
                step = step,
                store = store,
            )
            theme.transformLayout(layout, guestLayout, enableOverlayText)
        }
    }
}
