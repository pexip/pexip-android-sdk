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
package com.pexip.sdk.infinity

import com.pexip.sdk.infinity.Nodes.A
import com.pexip.sdk.infinity.Nodes.Srv

/**
 * A record or records resolved for a host.
 */
public sealed interface Nodes {

    /**
     * If the host has at least one valid SRV record.
     *
     * @property nodes a sorted list of nodes
     */
    @JvmInline
    public value class Srv(public val nodes: List<Node>) : Nodes

    /**
     * If the host has at least one A/AAAA record.
     *
     * @property node a node
     */
    @JvmInline
    public value class A(public val node: Node) : Nodes
}

/**
 * Converts this [Nodes] to a sequence of [Node]s.
 *
 * @return a sequence of nodes
 */
public fun Nodes.asSequence(): Sequence<Node> = when (this) {
    is Srv -> nodes.asSequence()
    is A -> sequenceOf(node)
}
