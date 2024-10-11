/*
 * Copyright 2022-2024 Pexip AS
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
package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InfinityService.RequestBuilder
import com.pexip.sdk.infinity.Node
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

internal class InfinityServiceImpl(
    override val client: OkHttpClient,
    override val json: Json,
) : InfinityService,
    InfinityServiceImplScope {

    override fun newRequest(node: Node): RequestBuilder = RequestBuilderImpl(this, node)
}
