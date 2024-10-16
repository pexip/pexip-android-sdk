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
package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Call
import org.minidns.hla.ResolverApi
import java.net.URL

/**
 * A class that can resolve node addresses.
 */
@Deprecated(
    message = "Superseded by a suspending variant.",
    replaceWith = ReplaceWith("NodeResolver", "com.pexip.sdk.infinity.NodeResolver"),
)
public fun interface NodeResolver {

    /**
     * Resolves the node address for the provided [host]. Clients should consult with
     * [documentation](https://docs.pexip.com/clients/configuring_dns_pexip_app.htm#next_gen_mobile)
     * for the recommended flow.
     *
     * @param host a host to use to resolve node addresses
     * @return a [Call]
     */
    public fun resolve(host: String): Call<List<URL>>

    public companion object {

        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        @JvmOverloads
        @Deprecated(
            message = "Superseded by a suspending variant.",
            replaceWith = ReplaceWith(
                expression = "NodeResolver.Companion.create(dnssec)",
                imports = ["com.pexip.sdk.infinity.NodeResolver", "com.pexip.sdk.infinity.create"],
            ),
            level = DeprecationLevel.ERROR,
        )
        public fun create(dnssec: Boolean = false): NodeResolver = throw NotImplementedError()

        @Suppress("UNUSED_PARAMETER")
        @JvmStatic
        @Deprecated(
            message = "Superseded by a suspending variant.",
            replaceWith = ReplaceWith(
                expression = "NodeResolver.Companion.create(api)",
                imports = ["com.pexip.sdk.infinity.NodeResolver", "com.pexip.sdk.infinity.create"],
            ),
            level = DeprecationLevel.ERROR,
        )
        public fun create(api: ResolverApi): NodeResolver = throw NotImplementedError()
    }
}
