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
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.hasCause
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.key
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
import com.pexip.sdk.api.infinity.TransformLayoutRequest
import com.pexip.sdk.conference.Element
import com.pexip.sdk.conference.Layout
import com.pexip.sdk.conference.LayoutId
import com.pexip.sdk.conference.SplashScreen
import com.pexip.sdk.conference.TransformLayoutException
import kotlinx.coroutines.flow.MutableSharedFlow
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
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `emits the current layout`() = runTest {
        val layouts = generateSequence { Random.nextString(8) }
            .map(::ApiLayoutId)
            .take(10)
            .toSet()
        val layoutSvgs = layouts.associateWith { Random.nextString(8) }
        val step = object : TestConferenceStep() {

            override fun availableLayouts(token: String): Call<Set<ApiLayoutId>> {
                assertThat(token).isEqualTo(store.get().token)
                return object : TestCall<Set<ApiLayoutId>> {

                    override fun enqueue(callback: Callback<Set<ApiLayoutId>>) {
                        callback.onSuccess(this, layouts)
                    }
                }
            }

            override fun layoutSvgs(token: String): Call<Map<ApiLayoutId, String>> {
                assertThat(token).isEqualTo(store.get().token)
                return object : TestCall<Map<ApiLayoutId, String>> {

                    override fun enqueue(callback: Callback<Map<ApiLayoutId, String>>) {
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
                        prop(Layout::layouts)
                            .extracting(LayoutId::value)
                            .containsExactly(*layouts.map(ApiLayoutId::value).toTypedArray())
                        prop(Layout::requestedPrimaryScreenHostLayout)
                            .isNotNull()
                            .isEqualTo(e.requestedLayout?.primaryScreen?.hostLayout)
                        prop(Layout::requestedPrimaryScreenGuestLayout)
                            .isNotNull()
                            .isEqualTo(e.requestedLayout?.primaryScreen?.guestLayout)
                        prop(Layout::overlayTextEnabled).isEqualTo(e.overlayTextEnabled)
                        prop(Layout::layoutSvgs).all {
                            layoutSvgs.forEach { key(LayoutId(it.key.value)).isEqualTo(it.value) }
                        }
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
            event.awaitSubscriptionCountAtLeast(2)
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

    @Test
    fun `transformLayout() throws`() = runTest {
        val t = List(transformLayoutPermutations.size) { Throwable() }
        transformLayoutPermutations
            .forEachIndexed { i, (layout, guestLayout, enableOverlayText) ->
                val step = object : TestConferenceStep() {

                    override fun transformLayout(
                        request: TransformLayoutRequest,
                        token: String,
                    ): Call<Boolean> {
                        assertThat(request::layout)
                            .isEqualTo(layout?.let { ApiLayoutId(it.value) })
                        assertThat(request::guestLayout)
                            .isEqualTo(guestLayout?.let { ApiLayoutId(it.value) })
                        assertThat(request::enableOverlayText)
                            .isEqualTo(enableOverlayText)
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
            val step = object : TestConferenceStep() {

                override fun transformLayout(
                    request: TransformLayoutRequest,
                    token: String,
                ): Call<Boolean> {
                    assertThat(request::layout)
                        .isEqualTo(layout?.let { ApiLayoutId(it.value) })
                    assertThat(request::guestLayout)
                        .isEqualTo(guestLayout?.let { ApiLayoutId(it.value) })
                    assertThat(request::enableOverlayText)
                        .isEqualTo(enableOverlayText)
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

    private fun Assert<LayoutId>.isEqualTo(id: ApiLayoutId?) =
        prop(LayoutId::value).isEqualTo(id?.value)

    private fun Random.nextLayoutId() = LayoutId(nextString(8))
}
