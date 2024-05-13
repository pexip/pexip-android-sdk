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
package com.pexip.sdk.infinity

/**
 * A class that can resolve node addresses.
 */
public interface NodeResolver {

    /**
     * Resolves the node address for the provided [host]. Clients should consult with
     * [documentation](https://docs.pexip.com/clients/configuring_dns_pexip_app.htm#next_gen_mobile)
     * for the recommended flow.
     *
     * @param host a host to use to resolve node addresses
     * @return a list of nodes
     */
    public suspend fun resolve(host: String): List<Node>

    public companion object
}

/**
 * Creates a new instance of [NodeResolver].
 *
 * @param dnssec whether to use DNSSEC or not
 */
public expect fun NodeResolver.Companion.create(dnssec: Boolean = false): NodeResolver
