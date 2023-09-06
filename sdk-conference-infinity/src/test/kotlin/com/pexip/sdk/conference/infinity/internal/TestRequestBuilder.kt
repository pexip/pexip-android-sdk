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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.InfinityService

internal abstract class TestRequestBuilder : InfinityService.RequestBuilder {

    override val infinityService: InfinityService get() = TODO()

    override fun status(): Call<Boolean> = TODO()

    override fun conference(conferenceAlias: String): InfinityService.ConferenceStep = TODO()

    override fun registration(deviceAlias: String): InfinityService.RegistrationStep = TODO()
}
